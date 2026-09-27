package goblinbob.mobends.standard.animation.controller;

import goblinbob.mobends.core.animation.bit.AnimationBit;
import goblinbob.mobends.core.animation.layer.HardAnimationLayer;
import goblinbob.mobends.standard.animation.bit.biped.WeaponRaisedAnimationBit;
import goblinbob.mobends.standard.data.BipedEntityData;
import goblinbob.mobends.standard.data.PiglinData;
import net.minecraft.world.entity.monster.piglin.PiglinArmPose;


public class PiglinController extends HumanoidMobController<PiglinData<?>>
{
    protected HardAnimationLayer<BipedEntityData<?>> layerWeapon = new HardAnimationLayer<>();

    protected AnimationBit<BipedEntityData<?>> bitWeaponRaised = new WeaponRaisedAnimationBit();

    @Override
    public void perform(PiglinData<?> data)
    {
        super.perform(data);

        if (data.getEntity().getArmPose() == PiglinArmPose.ATTACKING_WITH_MELEE_WEAPON)
        {
            layerWeapon.playOrContinueBit(bitWeaponRaised, data);
        }
        else
        {
            layerWeapon.clearAnimation();
        }

        layerWeapon.perform(data);
    }
}
