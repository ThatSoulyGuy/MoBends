package goblinbob.mobends.standard.animation.bit.biped.item;

import goblinbob.mobends.core.animation.bit.AnimationBit;
import goblinbob.mobends.core.animation.layer.HardAnimationLayer;
import goblinbob.mobends.standard.animation.bit.biped.FistGuardAnimationBit;
import goblinbob.mobends.standard.animation.bit.biped.RowingAnimationBit;
import goblinbob.mobends.standard.animation.bit.biped.SwimPunchAnimationBit;
import goblinbob.mobends.standard.animation.bit.biped.SwimmingAnimationBit;
import goblinbob.mobends.standard.animation.bit.player.PunchAnimationBit;
import goblinbob.mobends.standard.data.BipedEntityData;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;

public class PunchingAction extends AnimationBit<BipedEntityData<?>>
{
    protected final HardAnimationLayer<BipedEntityData<?>> layerBase = new HardAnimationLayer<>();

    protected final FistGuardAnimationBit bitFistGuard = new FistGuardAnimationBit();
    protected final PunchAnimationBit bitPunchLeft = new PunchAnimationBit(HumanoidArm.LEFT);
    protected final PunchAnimationBit bitPunchRight = new PunchAnimationBit(HumanoidArm.RIGHT);
    protected final SwimPunchAnimationBit bitSwimPunchLeft = new SwimPunchAnimationBit(HumanoidArm.LEFT);
    protected final SwimPunchAnimationBit bitSwimPunchRight = new SwimPunchAnimationBit(HumanoidArm.RIGHT);

    protected HumanoidArm punchingFist = HumanoidArm.LEFT;
    protected float lastTicksAfterAttack = 0;
    protected float ticksSinceMove = 100.0F;

    private static final float MIN_MOVE_INTERVAL = 4.0F;

    @Override
    public void perform(BipedEntityData<?> entityData)
    {
        float ticksAfterAttack = entityData.getTicksAfterAnyAttack();

        ticksSinceMove += goblinbob.mobends.core.client.event.DataUpdateHandler.ticksPerFrame;

        final boolean canAdvance = !goblinbob.mobends.compat.ModCompatManager.tracksPerHandAttacks()
                || ticksSinceMove >= MIN_MOVE_INTERVAL;

        if (ticksAfterAttack < lastTicksAfterAttack && canAdvance)
        {
            ticksSinceMove = 0.0F;
            LivingEntity entity = entityData.getEntity();
            if (entity == Minecraft.getInstance().player)
            {
                punchingFist = punchingFist.getOpposite();
            }
            else
            {
                punchingFist = (entity.tickCount / 6) % 2 == 0 ? HumanoidArm.LEFT : HumanoidArm.RIGHT;
            }
        }
        lastTicksAfterAttack = ticksAfterAttack;

        if (SwimmingAnimationBit.isSwimmingHorizontally(entityData))
        {
            if (ticksAfterAttack < 10)
            {
                this.layerBase.playOrContinueBit(punchingFist == HumanoidArm.LEFT ? bitSwimPunchLeft : bitSwimPunchRight, entityData);
            }
            else
            {
                this.layerBase.clearAnimation();
            }
        }
        else if (ticksAfterAttack < 10)
        {
            this.layerBase.playOrContinueBit(punchingFist == HumanoidArm.LEFT ? bitPunchLeft : bitPunchRight, entityData);
        }
        else if (ticksAfterAttack < 60 && !RowingAnimationBit.isPaddling(entityData.getEntity()))
        {
            this.layerBase.playOrContinueBit(bitFistGuard, entityData);
        }
        else
        {
            this.layerBase.clearAnimation();
        }

        this.layerBase.perform(entityData);
    }
}
