package goblinbob.mobends.standard.animation.controller;

import goblinbob.mobends.core.animation.bit.AnimationBit;
import goblinbob.mobends.core.animation.layer.HardAnimationLayer;
import goblinbob.mobends.standard.animation.bit.biped.SpellcastingAnimationBit;
import goblinbob.mobends.standard.animation.bit.biped.WeaponRaisedAnimationBit;
import goblinbob.mobends.standard.data.BipedEntityData;
import goblinbob.mobends.standard.data.IllagerData;
import net.minecraft.world.entity.monster.AbstractIllager;


public class IllagerController extends HumanoidMobController<IllagerData<?>>
{
    protected HardAnimationLayer<BipedEntityData<?>> layerWeapon = new HardAnimationLayer<>();

    protected AnimationBit<BipedEntityData<?>> bitWeaponRaised = new WeaponRaisedAnimationBit();
    protected AnimationBit<BipedEntityData<?>> bitSpellcasting = new SpellcastingAnimationBit();

    @Override
    public void perform(IllagerData<?> data)
    {
        super.perform(data);

        final AbstractIllager illager = data.getEntity();
        final AbstractIllager.IllagerArmPose armPose = illager.getArmPose();

        if (armPose == AbstractIllager.IllagerArmPose.SPELLCASTING)
        {
            layerWeapon.playOrContinueBit(bitSpellcasting, data);
        }
        else if (armPose == AbstractIllager.IllagerArmPose.ATTACKING
                && !illager.getMainHandItem().isEmpty())
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
