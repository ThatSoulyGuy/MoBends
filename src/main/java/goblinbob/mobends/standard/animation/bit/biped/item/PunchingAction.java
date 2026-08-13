package goblinbob.mobends.standard.animation.bit.biped.item;

import goblinbob.mobends.core.animation.bit.AnimationBit;
import goblinbob.mobends.core.animation.layer.HardAnimationLayer;
import goblinbob.mobends.standard.animation.bit.biped.FistGuardAnimationBit;
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

    protected HumanoidArm punchingFist = HumanoidArm.LEFT;
    protected float lastTicksAfterAttack = 0;

    public PunchingAction(HumanoidArm ignoredHandSide)
    {

    }

    @Override
    public void perform(BipedEntityData<?> entityData)
    {
        float ticksAfterAttack = entityData.getTicksAfterAttack();
        if (ticksAfterAttack < lastTicksAfterAttack)
        {
            LivingEntity entity = entityData.getEntity();
            if (entity == Minecraft.getInstance().player)
            {
                punchingFist = punchingFist == HumanoidArm.LEFT ? HumanoidArm.RIGHT : HumanoidArm.LEFT;
            }
            else
            {
                punchingFist = (entity.tickCount / 6) % 2 == 0 ? HumanoidArm.LEFT : HumanoidArm.RIGHT;
            }
        }
        lastTicksAfterAttack = ticksAfterAttack;

        if (ticksAfterAttack < 10)
        {
            this.layerBase.playOrContinueBit(punchingFist == HumanoidArm.LEFT ? bitPunchLeft : bitPunchRight, entityData);
        }
        else if (ticksAfterAttack < 60)
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
