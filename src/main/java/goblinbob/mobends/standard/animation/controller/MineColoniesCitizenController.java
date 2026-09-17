package goblinbob.mobends.standard.animation.controller;

import goblinbob.mobends.core.animation.bit.AnimationBit;
import goblinbob.mobends.standard.animation.bit.biped.HumanoidSleepingAnimationBit;
import goblinbob.mobends.standard.data.BipedEntityData;
import goblinbob.mobends.standard.data.HumanoidMobData;
import net.minecraft.world.entity.LivingEntity;

public class MineColoniesCitizenController extends HumanoidMobController
{
    protected final AnimationBit<BipedEntityData<?>> bitSleeping = new HumanoidSleepingAnimationBit<>();

    @Override
    public void perform(HumanoidMobData<?> data)
    {
        final LivingEntity entity = data.getEntity();

        if (!entity.isAlive() || !entity.isSleeping())
        {
            super.perform(data);
            return;
        }

        layerBase.playOrContinueBit(bitSleeping, data);
        layerBase.perform(data);
        actionController.clearAction();
    }
}
