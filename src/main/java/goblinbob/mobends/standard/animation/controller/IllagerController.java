package goblinbob.mobends.standard.animation.controller;

import goblinbob.mobends.core.animation.bit.AnimationBit;
import goblinbob.mobends.core.animation.controller.IAnimationController;
import goblinbob.mobends.core.animation.layer.HardAnimationLayer;
import goblinbob.mobends.standard.animation.bit.biped.FallingAnimationBit;
import goblinbob.mobends.standard.animation.bit.biped.JumpAnimationBit;
import goblinbob.mobends.standard.animation.bit.biped.RidingAnimationBit;
import goblinbob.mobends.standard.animation.bit.biped.SittingAnimationBit;
import goblinbob.mobends.standard.animation.bit.biped.SpellcastingAnimationBit;
import goblinbob.mobends.standard.animation.bit.biped.SprintAnimationBit;
import goblinbob.mobends.standard.animation.bit.biped.StandAnimationBit;
import goblinbob.mobends.standard.animation.bit.biped.WalkAnimationBit;
import goblinbob.mobends.standard.animation.bit.biped.WeaponRaisedAnimationBit;
import goblinbob.mobends.standard.animation.bit.biped.item.BipedActionController;
import goblinbob.mobends.standard.data.BipedEntityData;
import goblinbob.mobends.standard.data.IllagerData;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;


public class IllagerController implements IAnimationController<IllagerData<?>>
{
    protected HardAnimationLayer<BipedEntityData<?>> layerBase = new HardAnimationLayer<>();
    protected HardAnimationLayer<BipedEntityData<?>> layerWeapon = new HardAnimationLayer<>();

    protected AnimationBit<BipedEntityData<?>> bitStand = new StandAnimationBit<>();
    protected AnimationBit<BipedEntityData<?>> bitWalk = new WalkAnimationBit<>();
    protected AnimationBit<BipedEntityData<?>> bitSprint = new SprintAnimationBit<>();
    protected AnimationBit<BipedEntityData<?>> bitJump = new JumpAnimationBit<>();
    protected AnimationBit<BipedEntityData<?>> bitFalling = new FallingAnimationBit();
    protected AnimationBit<BipedEntityData<?>> bitRiding = new RidingAnimationBit<>();
    protected AnimationBit<BipedEntityData<?>> bitSitting = new SittingAnimationBit<>();
    protected AnimationBit<BipedEntityData<?>> bitWeaponRaised = new WeaponRaisedAnimationBit();
    protected AnimationBit<BipedEntityData<?>> bitSpellcasting = new SpellcastingAnimationBit();

    protected final BipedActionController actionController = new BipedActionController();

    public void performActionAnimations(IllagerData<?> data, AbstractIllager illager)
    {
        final HumanoidArm primaryHand = illager.getMainArm();
        final ItemStack heldItemMainhand = illager.getMainHandItem();
        final ItemStack heldItemOffhand = illager.getOffhandItem();
        final Item activeItem = illager.getUseItem().getItem();

        actionController.perform(data, primaryHand, heldItemMainhand, heldItemOffhand, activeItem);
    }

    @Override
    public void perform(IllagerData<?> data)
    {
        final AbstractIllager illager = data.getEntity();

        if (data.isRiding())
        {
            layerBase.playOrContinueBit(data.isRidingLivingEntity() ? bitRiding : bitSitting, data);
        }
        else if (!data.isOnGround() || data.getTicksAfterTouchdown() < 1)
        {
            if (data.getTicksFalling() > FallingAnimationBit.TICKS_BEFORE_FALLING)
            {
                layerBase.playOrContinueBit(bitFalling, data);
            }
            else
            {
                layerBase.playOrContinueBit(bitJump, data);
            }
        }
        else if (data.isStillHorizontally())
        {
            layerBase.playOrContinueBit(bitStand, data);
        }
        else if (data.isMovingAtSprintSpeed())
        {
            layerBase.playOrContinueBit(bitSprint, data);
        }
        else
        {
            layerBase.playOrContinueBit(bitWalk, data);
        }

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

        layerBase.perform(data);
        performActionAnimations(data, illager);
        layerWeapon.perform(data);
    }
}
