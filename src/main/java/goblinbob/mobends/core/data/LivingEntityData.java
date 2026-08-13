package goblinbob.mobends.core.data;

import goblinbob.mobends.core.client.event.DataUpdateHandler;
import goblinbob.mobends.lib.data.OverridableProperty;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;

public abstract class LivingEntityData<E extends LivingEntity> extends EntityData<E>
{

    protected float ticksInAir;
    protected float ticksAfterTouchdown;
    protected float ticksAfterAttack;
    protected float ticksFalling;
    protected float climbingCycle = 0F;
    protected boolean alreadyAttacked = false;
    protected boolean climbing = false;

    protected Boolean climbingOverride = null;
    protected Boolean inWaterOverride = null;
    protected Boolean ridingOverride = null;
    protected Float climbingRotationOverride = null;
    protected Float ledgeHeightOverride = null;

    public OverridableProperty<Float> limbSwing = new OverridableProperty<>(0F);
    public OverridableProperty<Float> limbSwingAmount = new OverridableProperty<>(0F);
    public OverridableProperty<Float> swingProgress = new OverridableProperty<>(0F);
    public OverridableProperty<Float> headYaw = new OverridableProperty<>(0F);
    public OverridableProperty<Float> headPitch = new OverridableProperty<>(0F);

    public LivingEntityData(E entity)
    {
        super(entity);

        this.ticksInAir = 100F;
        this.ticksAfterTouchdown = 100F;
        this.ticksAfterAttack = 100F;
        this.ticksFalling = 100F;
    }

    public void setClimbing(boolean flag)
    {
        this.climbing = flag;
    }

    public void setClimbingCycle(float cycle)
    {
        this.climbingCycle = cycle;
    }

    public void overrideClimbing(boolean climbing)
    {
        this.climbingOverride = climbing;
    }

    public void unsetClimbingOverride()
    {
        this.climbingOverride = null;
    }

    public void overrideInWater(boolean inWater)
    {
        this.inWaterOverride = inWater;
    }

    public void unsetInWaterOverride()
    {
        this.inWaterOverride = null;
    }

    public void overrideRiding(boolean riding)
    {
        this.ridingOverride = riding;
    }

    public void unsetRidingOverride()
    {
        this.ridingOverride = null;
    }

    public void overrideClimbingRotation(float rotation)
    {
        this.climbingRotationOverride = rotation;
    }

    public void unsetClimbingRotationOverride()
    {
        this.climbingRotationOverride = null;
    }

    public void overrideLedgeHeight(float ledgeHeight)
    {
        this.ledgeHeightOverride = ledgeHeight;
    }

    public void unsetLedgeHeightOverride()
    {
        this.ledgeHeightOverride = null;
    }

    public boolean isInWater()
    {
        return this.inWaterOverride != null ? this.inWaterOverride : this.entity.isInWater();
    }

    public boolean isRiding()
    {
        return this.ridingOverride != null ? this.ridingOverride : this.entity.isPassenger();
    }

    public boolean isRidingLivingEntity()
    {
        return this.ridingOverride != null ? this.ridingOverride
                : this.entity.getVehicle() instanceof net.minecraft.world.entity.LivingEntity;
    }

    public float getClimbingCycle() { return this.climbingCycle; }

    public float getTicksInAir() { return this.ticksInAir; }

    public float getTicksAfterTouchdown() { return this.ticksAfterTouchdown; }

    public float getTicksAfterAttack() { return this.ticksAfterAttack; }

    public float getTicksFalling() { return this.ticksFalling; }

    public boolean isClimbing() { return this.climbingOverride != null ? this.climbingOverride : this.climbing; }

    @Override
    public void updateClient()
    {
        super.updateClient();

        final boolean calcOnGroundResult = this.calcOnGround();
        if (calcOnGroundResult & !this.onGround)
        {
            this.onTouchdown();
            this.onGround = true;
        }

        if ((!calcOnGroundResult & this.onGround) | (this.prevMotionY <= 0 && this.motionY - this.prevMotionY > 0.4D && this.ticksInAir > 2.0F))
        {
            this.onLiftoff();
            this.onGround = false;
        }

        if (this.calcClimbing())
        {
            this.climbingCycle += this.motionY * 2.6F;
            this.climbing = true;
        }
        else
        {
            this.climbing = false;
        }

        if (this.entity.swinging)
        {
            if (!this.alreadyAttacked || this.ticksAfterAttack > 5.0F)
            {
                this.onAttack();
                this.alreadyAttacked = true;
            }
        }
        else
        {
            this.alreadyAttacked = false;
        }
    }

    @Override
    public void update(float partialTicks)
    {
        super.update(partialTicks);

        if (this.isOnGround())
        {
            this.ticksAfterTouchdown += DataUpdateHandler.ticksPerFrame;
        }
        else
        {
            this.ticksInAir += DataUpdateHandler.ticksPerFrame;

            if (this.motionY < 0.0D)
            {
                this.ticksFalling += DataUpdateHandler.ticksPerFrame;
            }
            else
            {
                this.ticksFalling = 0.0F;
            }
        }

        this.ticksAfterAttack += DataUpdateHandler.ticksPerFrame;
    }

    public void onTouchdown()
    {
        this.ticksAfterTouchdown = 0.0F;
        this.ticksFalling = 0.0F;
    }

    public void onLiftoff()
    {
        this.ticksInAir = 0.0F;
    }

    public void onAttack()
    {
        this.ticksAfterAttack = 0.0F;
    }

    public float getClimbingRotation()
    {
        if (this.climbingRotationOverride != null)
            return this.climbingRotationOverride;

        return getLadderFacing().toYRot() + 180.0F;
    }

    private static boolean isBlockClimbable(BlockState state)
    {
        return state.getBlock() instanceof LadderBlock || state.getBlock() instanceof VineBlock;
    }

    private static Direction getClimbableBlockFacing(BlockState state)
    {
        if (state.getBlock() instanceof LadderBlock)
        {
            return state.getValue(LadderBlock.FACING);
        }
        else if (state.getBlock() instanceof VineBlock)
        {
            if (state.getValue(VineBlock.EAST))
                return Direction.WEST;
            else if (state.getValue(VineBlock.WEST))
                return Direction.EAST;
            else if (state.getValue(VineBlock.NORTH))
                return Direction.SOUTH;
            else if (state.getValue(VineBlock.SOUTH))
                return Direction.NORTH;
        }

        return Direction.NORTH;
    }

    public Direction getLadderFacing()
    {
        BlockPos position = new BlockPos(
            Mth.floor(entity.getX()),
            Mth.floor(entity.getY()),
            Mth.floor(entity.getZ())
        );

        BlockState block = entity.level().getBlockState(position);
        BlockState blockBelow = entity.level().getBlockState(position.offset(0, -1, 0));
        BlockState blockBelow2 = entity.level().getBlockState(position.offset(0, -2, 0));

        Direction facing = Direction.NORTH;
        facing = getClimbableBlockFacing(block);
        if (facing == Direction.NORTH)
            facing = getClimbableBlockFacing(blockBelow);
        if (facing == Direction.NORTH)
            facing = getClimbableBlockFacing(blockBelow2);

        return facing;
    }

    public boolean calcClimbing()
    {
        if (entity == null || entity.level() == null)
            return false;

        BlockPos position = new BlockPos(
            Mth.floor(entity.getX()),
            Mth.floor(entity.getY()),
            Mth.floor(entity.getZ())
        );

        BlockState block = entity.level().getBlockState(position);
        BlockState blockBelow = entity.level().getBlockState(position.offset(0, -1, 0));
        BlockState blockBelow2 = entity.level().getBlockState(position.offset(0, -2, 0));

        return entity.onClimbable() && !this.isOnGround() && (isBlockClimbable(block) || isBlockClimbable(blockBelow) || isBlockClimbable(blockBelow2));
    }

    public float getLedgeHeight()
    {
        if (this.ledgeHeightOverride != null)
            return this.ledgeHeightOverride;

        float clientY = (float) (entity.getY() + (entity.getY() - entity.yOld) * DataUpdateHandler.partialTicks);

        final BlockPos position = new BlockPos(
            Mth.floor(entity.getX()),
            Mth.floor(entity.getY()),
            Mth.floor(entity.getZ())
        );

        BlockState block = entity.level().getBlockState(position.offset(0, 2, 0));
        BlockState blockBelow = entity.level().getBlockState(position.offset(0, 1, 0));
        BlockState blockBelow2 = entity.level().getBlockState(position.offset(0, 0, 0));
        if (!isBlockClimbable(block))
        {
            if (!isBlockClimbable(blockBelow))
            {
                if (!isBlockClimbable(blockBelow2))
                    return (clientY - (int) clientY) + 2;
                else
                    return (clientY - (int) clientY) + 1;
            }
            else
            {
                return clientY - (int) clientY;
            }
        }

        return -2.0F;
    }

    public boolean isDrawingBow()
    {
        if (entity.getUseItemRemainingTicks() > 0)
        {
            ItemStack mainItemStack = entity.getMainHandItem();
            ItemStack offItemStack = entity.getOffhandItem();
            if ((!mainItemStack.isEmpty() && mainItemStack.getUseAnimation() == UseAnim.BOW)
                    || (!offItemStack.isEmpty() && offItemStack.getUseAnimation() == UseAnim.BOW))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public E getEntity()
    {
        return this.entity;
    }

}
