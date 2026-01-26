package goblinbob.mobends.platform;

import goblinbob.mobends.api.entity.IEntity;
import goblinbob.mobends.api.entity.IEquipmentSlot;
import goblinbob.mobends.api.entity.IItemStack;
import goblinbob.mobends.api.entity.ILivingEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;

/**
 * Shared implementation of ILivingEntity.
 * Wraps Minecraft's LivingEntity with a platform-agnostic interface.
 */
public class McLivingEntity extends McEntity implements ILivingEntity
{
    protected final LivingEntity livingEntity;

    public McLivingEntity(LivingEntity entity)
    {
        super(entity);
        this.livingEntity = entity;
    }

    @Override
    public float getHealth()
    {
        return livingEntity.getHealth();
    }

    @Override
    public float getMaxHealth()
    {
        return livingEntity.getMaxHealth();
    }

    @Override
    public float getLimbSwing()
    {
        return livingEntity.walkAnimation.position();
    }

    @Override
    public float getLimbSwingAmount()
    {
        return livingEntity.walkAnimation.speed();
    }

    @Override
    public float getAttackAnim()
    {
        return livingEntity.attackAnim;
    }

    @Override
    public int getHurtTime()
    {
        return livingEntity.hurtTime;
    }

    @Override
    public int getDeathTime()
    {
        return livingEntity.deathTime;
    }

    @Override
    public float getHeadYaw()
    {
        return livingEntity.yHeadRot;
    }

    @Override
    public float getPrevHeadYaw()
    {
        return livingEntity.yHeadRotO;
    }

    @Override
    public float getBodyYaw()
    {
        return livingEntity.yBodyRot;
    }

    @Override
    public float getPrevBodyYaw()
    {
        return livingEntity.yBodyRotO;
    }

    @Override
    public boolean isBaby()
    {
        return livingEntity.isBaby();
    }

    @Override
    public boolean isSleeping()
    {
        return livingEntity.isSleeping();
    }

    @Override
    public boolean isDead()
    {
        return livingEntity.isDeadOrDying();
    }

    @Override
    public boolean isUsingItem()
    {
        return livingEntity.isUsingItem();
    }

    @Override
    public int getTicksUsingItem()
    {
        return livingEntity.getTicksUsingItem();
    }

    @Override
    public int getUsedItemHand()
    {
        if (!livingEntity.isUsingItem())
        {
            return -1;
        }
        return livingEntity.getUsedItemHand() == InteractionHand.MAIN_HAND ? 0 : 1;
    }

    @Override
    public boolean isFallFlying()
    {
        return livingEntity.isFallFlying();
    }

    @Override
    public boolean isSwimming()
    {
        return livingEntity.isSwimming();
    }

    @Override
    public boolean isVisuallySwimming()
    {
        return livingEntity.isVisuallySwimming();
    }

    @Override
    public boolean isCrouching()
    {
        return livingEntity.isCrouching();
    }

    @Override
    public IItemStack getItemBySlot(IEquipmentSlot slot)
    {
        EquipmentSlot mcSlot = convertSlot(slot);
        return new McItemStack(livingEntity.getItemBySlot(mcSlot));
    }

    @Override
    @Nullable
    public IEntity getVehicle()
    {
        if (livingEntity.getVehicle() == null)
        {
            return null;
        }
        return new McEntity(livingEntity.getVehicle());
    }

    private EquipmentSlot convertSlot(IEquipmentSlot slot)
    {
        return switch (slot)
        {
            case MAINHAND -> EquipmentSlot.MAINHAND;
            case OFFHAND -> EquipmentSlot.OFFHAND;
            case FEET -> EquipmentSlot.FEET;
            case LEGS -> EquipmentSlot.LEGS;
            case CHEST -> EquipmentSlot.CHEST;
            case HEAD -> EquipmentSlot.HEAD;
        };
    }

    public LivingEntity getLivingEntity()
    {
        return livingEntity;
    }
}
