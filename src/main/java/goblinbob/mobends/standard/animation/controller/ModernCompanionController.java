package goblinbob.mobends.standard.animation.controller;

import goblinbob.mobends.compat.ModernCompanionsCompat;
import goblinbob.mobends.core.animation.bit.AnimationBit;
import goblinbob.mobends.standard.animation.bit.biped.GroundSittingAnimationBit;
import goblinbob.mobends.standard.data.BipedEntityData;
import goblinbob.mobends.standard.data.HumanoidMobData;
import net.minecraft.world.entity.LivingEntity;

public class ModernCompanionController extends HumanoidMobController
{
    protected final AnimationBit<BipedEntityData<?>> bitGroundSitting = new GroundSittingAnimationBit<>();

    @Override
    public void perform(HumanoidMobData<?> data)
    {
        final LivingEntity entity = data.getEntity();

        if (data.isRiding() || !ModernCompanionsCompat.isSittingOnGround(entity))
        {
            super.perform(data);
            return;
        }

        layerBase.playOrContinueBit(bitGroundSitting, data);
        layerBase.perform(data);

        actionController.perform(data, entity.getMainArm(),
                entity.getMainHandItem(), entity.getOffhandItem(), entity.getUseItem().getItem());
    }
}
