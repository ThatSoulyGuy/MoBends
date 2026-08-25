package goblinbob.mobends.standard.animation.controller;

import goblinbob.mobends.core.animation.bit.AnimationBit;
import goblinbob.mobends.core.animation.controller.IAnimationController;
import goblinbob.mobends.core.animation.layer.HardAnimationLayer;
import goblinbob.mobends.standard.animation.bit.biped.FallingAnimationBit;
import goblinbob.mobends.standard.animation.bit.biped.JumpAnimationBit;
import goblinbob.mobends.standard.animation.bit.biped.RidingAnimationBit;
import goblinbob.mobends.standard.animation.bit.biped.SittingAnimationBit;
import goblinbob.mobends.standard.animation.bit.biped.SprintAnimationBit;
import goblinbob.mobends.standard.animation.bit.biped.StandAnimationBit;
import goblinbob.mobends.standard.animation.bit.biped.WalkAnimationBit;
import goblinbob.mobends.standard.animation.bit.biped.WeaponRaisedAnimationBit;
import goblinbob.mobends.standard.animation.bit.biped.item.BipedActionController;
import goblinbob.mobends.standard.data.BipedEntityData;
import goblinbob.mobends.standard.data.PiglinData;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.monster.piglin.PiglinArmPose;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PiglinController implements IAnimationController<PiglinData<?>>
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

    protected final BipedActionController actionController = new BipedActionController();

    public void performActionAnimations(PiglinData<?> data, AbstractPiglin piglin)
    {
        final HumanoidArm primaryHand = piglin.getMainArm();
        final ItemStack heldItemMainhand = piglin.getMainHandItem();
        final ItemStack heldItemOffhand = piglin.getOffhandItem();
        final Item activeItem = piglin.getUseItem().getItem();

        actionController.perform(data, primaryHand, heldItemMainhand, heldItemOffhand, activeItem);
    }

    @Override
    public void perform(PiglinData<?> data)
    {
        final AbstractPiglin piglin = data.getEntity();

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

        if (piglin.getArmPose() == PiglinArmPose.ATTACKING_WITH_MELEE_WEAPON)
        {
            layerWeapon.playOrContinueBit(bitWeaponRaised, data);
        }
        else
        {
            layerWeapon.clearAnimation();
        }

        layerBase.perform(data);
        performActionAnimations(data, piglin);
        layerWeapon.perform(data);
    }
}
