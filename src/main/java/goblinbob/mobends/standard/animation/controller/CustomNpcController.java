package goblinbob.mobends.standard.animation.controller;

import goblinbob.mobends.compat.CustomNpcsCompat;
import goblinbob.mobends.compat.ModCompatManager;
import goblinbob.mobends.core.animation.bit.AnimationBit;
import goblinbob.mobends.core.animation.controller.IAnimationController;
import goblinbob.mobends.core.animation.layer.HardAnimationLayer;
import goblinbob.mobends.standard.animation.bit.biped.FallingAnimationBit;
import goblinbob.mobends.standard.animation.bit.biped.JumpAnimationBit;
import goblinbob.mobends.standard.animation.bit.biped.LadderClimbAnimationBit;
import goblinbob.mobends.standard.animation.bit.biped.RidingAnimationBit;
import goblinbob.mobends.standard.animation.bit.biped.SittingAnimationBit;
import goblinbob.mobends.standard.animation.bit.biped.SneakAnimationBit;
import goblinbob.mobends.standard.animation.bit.biped.StandAnimationBit;
import goblinbob.mobends.standard.animation.bit.biped.SwimmingAnimationBit;
import goblinbob.mobends.standard.animation.bit.biped.TorchHoldingAnimationBit;
import goblinbob.mobends.standard.animation.bit.biped.item.BipedActionController;
import goblinbob.mobends.standard.animation.bit.player.ExternalPoseAnimationBit;
import goblinbob.mobends.standard.animation.bit.player.SprintAnimationBit;
import goblinbob.mobends.standard.animation.bit.player.WalkAnimationBit;
import goblinbob.mobends.standard.data.BipedEntityData;
import goblinbob.mobends.standard.data.CustomNpcData;
import net.minecraft.world.entity.LivingEntity;

public class CustomNpcController implements IAnimationController<CustomNpcData<?>>
{
    protected final HardAnimationLayer<BipedEntityData<?>> layerBase = new HardAnimationLayer<>();
    protected final HardAnimationLayer<BipedEntityData<?>> layerTorch = new HardAnimationLayer<>();
    protected final HardAnimationLayer<BipedEntityData<?>> layerSneak = new HardAnimationLayer<>();

    protected final AnimationBit<BipedEntityData<?>> bitStand = new StandAnimationBit<>();
    protected final AnimationBit<BipedEntityData<?>> bitWalk = new WalkAnimationBit<>();
    protected final AnimationBit<BipedEntityData<?>> bitSprint = new SprintAnimationBit<>();
    protected final AnimationBit<BipedEntityData<?>> bitJump = new JumpAnimationBit<>();
    protected final AnimationBit<BipedEntityData<?>> bitFalling = new FallingAnimationBit();
    protected final AnimationBit<BipedEntityData<?>> bitRiding = new RidingAnimationBit<>();
    protected final AnimationBit<BipedEntityData<?>> bitSitting = new SittingAnimationBit<>();
    protected final AnimationBit<BipedEntityData<?>> bitLadderClimb = new LadderClimbAnimationBit();
    protected final AnimationBit<BipedEntityData<?>> bitSwimming = new SwimmingAnimationBit();
    protected final AnimationBit<BipedEntityData<?>> bitSneak = new SneakAnimationBit();
    protected final AnimationBit<BipedEntityData<?>> bitTorchHolding = new TorchHoldingAnimationBit();
    protected final AnimationBit<BipedEntityData<?>> bitExternalPose = new ExternalPoseAnimationBit();

    protected final BipedActionController actionController = new BipedActionController();

    @Override
    public void perform(CustomNpcData<?> data)
    {
        final LivingEntity entity = data.getEntity();

        if (ModCompatManager.isExternallyPosed(entity))
        {
            layerBase.playOrContinueBit(bitExternalPose, data);
            layerSneak.clearAnimation();
            layerTorch.clearAnimation();
            actionController.clearAction();

            layerBase.perform(data);
            return;
        }

        if (data.isRiding() || CustomNpcsCompat.isSitting(entity))
        {
            layerBase.playOrContinueBit(data.isRiding() && data.isRidingLivingEntity() ? bitRiding : bitSitting, data);
            layerSneak.clearAnimation();
            layerTorch.clearAnimation();
        }
        else if (data.isClimbing())
        {
            layerBase.playOrContinueBit(bitLadderClimb, data);
            layerSneak.clearAnimation();
            layerTorch.clearAnimation();
        }
        else if (data.isInWater() && !entity.isCrouching())
        {
            layerBase.playOrContinueBit(bitSwimming, data);
            layerSneak.clearAnimation();
            layerTorch.clearAnimation();
        }
        else if ((!data.isOnGround() && !data.isInWater()) || data.getTicksAfterTouchdown() < 1)
        {
            if (data.getTicksFalling() > FallingAnimationBit.TICKS_BEFORE_FALLING)
            {
                layerBase.playOrContinueBit(bitFalling, data);
            }
            else
            {
                layerBase.playOrContinueBit(bitJump, data);
            }

            layerSneak.clearAnimation();
            layerTorch.clearAnimation();
        }
        else
        {
            if (data.isStillHorizontally())
            {
                layerBase.playOrContinueBit(bitStand, data);
                layerTorch.playOrContinueBit(bitTorchHolding, data);
            }
            else if (entity.isSprinting() || data.isMovingAtSprintSpeed())
            {
                layerBase.playOrContinueBit(bitSprint, data);
                layerTorch.clearAnimation();
            }
            else
            {
                layerBase.playOrContinueBit(bitWalk, data);
                layerTorch.playOrContinueBit(bitTorchHolding, data);
            }

            if (entity.isCrouching())
            {
                layerSneak.playOrContinueBit(bitSneak, data);
            }
            else
            {
                layerSneak.clearAnimation();
            }
        }

        data.renderLeftItemRotation.orientZero();
        data.renderRightItemRotation.orientZero();

        layerBase.perform(data);
        layerSneak.perform(data);
        layerTorch.perform(data);

        actionController.perform(data, entity.getMainArm(),
                entity.getMainHandItem(), entity.getOffhandItem(), entity.getUseItem().getItem());
    }
}
