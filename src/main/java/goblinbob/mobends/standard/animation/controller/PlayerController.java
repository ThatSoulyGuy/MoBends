package goblinbob.mobends.standard.animation.controller;

import goblinbob.mobends.core.animation.bit.AnimationBit;
import goblinbob.mobends.core.animation.controller.IAnimationController;
import goblinbob.mobends.core.animation.layer.HardAnimationLayer;
import goblinbob.mobends.standard.animation.bit.biped.*;
import goblinbob.mobends.standard.animation.bit.biped.item.*;
import goblinbob.mobends.standard.animation.bit.player.*;
import goblinbob.mobends.standard.data.BipedEntityData;
import goblinbob.mobends.standard.data.PlayerData;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class PlayerController implements IAnimationController<PlayerData>
{
    protected HardAnimationLayer<BipedEntityData<?>> layerBase = new HardAnimationLayer<>();
    protected HardAnimationLayer<BipedEntityData<?>> layerTorch = new HardAnimationLayer<>();
    protected HardAnimationLayer<BipedEntityData<?>> layerSneak = new HardAnimationLayer<>();
    protected HardAnimationLayer<BipedEntityData<?>> layerCape = new HardAnimationLayer<>();

    protected AnimationBit<BipedEntityData<?>> bitStand = new StandAnimationBit<>();
    protected AnimationBit<BipedEntityData<?>> bitJump = new JumpAnimationBit<>();
    protected AnimationBit<BipedEntityData<?>> bitSneak = new SneakAnimationBit();
    protected AnimationBit<BipedEntityData<?>> bitLadderClimb = new LadderClimbAnimationBit();
    protected AnimationBit<BipedEntityData<?>> bitSwimming = new SwimmingAnimationBit();
    protected AnimationBit<BipedEntityData<?>> bitCrawling = new CrawlingAnimationBit();
    protected AnimationBit<BipedEntityData<?>> bitRiding = new RidingAnimationBit<>();
    protected AnimationBit<BipedEntityData<?>> bitSitting = new SittingAnimationBit<>();
    protected AnimationBit<BipedEntityData<?>> bitFalling = new FallingAnimationBit();
    protected AnimationBit<BipedEntityData<?>> bitParagliding = new ParaglidingAnimationBit();
    protected AnimationBit<PlayerData> bitWalk = new goblinbob.mobends.standard.animation.bit.player.WalkAnimationBit<>();
    protected AnimationBit<PlayerData> bitSprint = new goblinbob.mobends.standard.animation.bit.player.SprintAnimationBit<>();
    protected AnimationBit<PlayerData> bitSprintJump = new SprintJumpAnimationBit();
    protected AnimationBit<BipedEntityData<?>> bitTorchHolding = new TorchHoldingAnimationBit();
    protected FlyingAnimationBit bitFlying = new FlyingAnimationBit();
    protected ElytraAnimationBit bitElytra = new ElytraAnimationBit();
    protected CapeAnimationBit bitCape = new CapeAnimationBit();
    protected SleepingAnimationBit bitSleeping = new SleepingAnimationBit();
    protected AnimationBit<BipedEntityData<?>> bitExternalPose = new ExternalPoseAnimationBit();
    protected HardAnimationLayer<BipedEntityData<?>> layerZipline = new HardAnimationLayer<>();
    protected AnimationBit<BipedEntityData<?>> bitZiplineHang = new ZiplineHangAnimationBit();
    protected AnimationBit<BipedEntityData<?>> bitZiplineArm = new ZiplineArmAnimationBit();

    protected final BipedActionController actionController = new BipedActionController();

    public static boolean isCrawling(PlayerData data, AbstractClientPlayer player)
    {
        return (player.isVisuallySwimming() && !data.isInWater())
                || goblinbob.mobends.compat.CrawlCompat.isCrawling(player);
    }

    public void performActionAnimations(PlayerData data, AbstractClientPlayer player)
    {
        if (player.isAlive() && player.isSleeping())
        {
            actionController.clearAction();
            return;
        }

        if (isCrawling(data, player))
        {
            actionController.clearAction();
            return;
        }

        if (goblinbob.mobends.compat.ParagliderCompat.isParagliding(player))
        {
            actionController.clearAction();
            return;
        }

        if (goblinbob.mobends.compat.ZiplineCompat.isZiplining(player))
        {
            actionController.clearAction();
            return;
        }

        final HumanoidArm primaryHand = player.getMainArm();
        final ItemStack heldItemMainhand = player.getMainHandItem();
        final ItemStack heldItemOffhand = player.getOffhandItem();
        final Item activeItem = player.getUseItem().getItem();

        actionController.perform(data, primaryHand, heldItemMainhand, heldItemOffhand, activeItem);
    }

    @Override
    public void perform(PlayerData data)
    {
        final AbstractClientPlayer player = data.getEntity();

        if (goblinbob.mobends.compat.ModCompatManager.isExternallyPosed(player))
        {
            layerBase.playOrContinueBit(bitExternalPose, data);
            layerSneak.clearAnimation();
            layerTorch.clearAnimation();
            layerCape.clearAnimation();
            actionController.clearAction();

            layerBase.perform(data);
            return;
        }

        layerCape.playOrContinueBit(bitCape, data);

        if (player.isAlive() && player.isSleeping())
        {
            layerBase.playOrContinueBit(bitSleeping, data);
            layerSneak.clearAnimation();
        }
        else if (data.isRiding())
        {
            if (data.isRidingLivingEntity())
            {
                layerBase.playOrContinueBit(bitRiding, data);
            }
            else
            {
                layerBase.playOrContinueBit(bitSitting, data);
            }
            layerSneak.clearAnimation();
        }
        else
        {
            if (goblinbob.mobends.compat.ParagliderCompat.isParagliding(player))
            {
                layerBase.playOrContinueBit(bitParagliding, data);
                layerSneak.clearAnimation();
                layerTorch.clearAnimation();
            }
            else if (player.getFallFlyingTicks() > 4)
            {
                layerBase.playOrContinueBit(bitElytra, data);
                layerSneak.clearAnimation();
                layerTorch.clearAnimation();
            }
            else if (data.isClimbing())
            {
                layerBase.playOrContinueBit(bitLadderClimb, data);
                layerSneak.clearAnimation();
                layerTorch.clearAnimation();
            }
            else if (isCrawling(data, player))
            {
                layerBase.playOrContinueBit(bitCrawling, data);
                layerSneak.clearAnimation();
                layerTorch.clearAnimation();
            }
            else if (data.isInWater() && (player.isVisuallySwimming() || !player.isCrouching()))
            {
                layerBase.playOrContinueBit(bitSwimming, data);
                layerSneak.clearAnimation();
                layerTorch.clearAnimation();
            }
            else if ((!data.isOnGround() && !data.isInWater()) || data.getTicksAfterTouchdown() < 1)
            {
                if (goblinbob.mobends.compat.ZiplineCompat.isZiplining(player))
                {
                    layerBase.playOrContinueBit(bitZiplineHang, data);
                }
                else if (data.isFlying())
                {
                    layerBase.playOrContinueBit(bitFlying, data);
                }
                else
                {
                    if (data.getTicksFalling() > FallingAnimationBit.TICKS_BEFORE_FALLING)
                    {
                        layerBase.playOrContinueBit(bitFalling, data);
                    }
                    else
                    {
                        if (player.isSprinting())
                            layerBase.playOrContinueBit(bitSprintJump, data);
                        else
                            layerBase.playOrContinueBit(bitJump, data);
                    }
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
                else
                {
                    if (player.isSprinting())
                    {
                        layerBase.playOrContinueBit(bitSprint, data);
                        layerTorch.clearAnimation();
                    }
                    else
                    {
                        layerBase.playOrContinueBit(bitWalk, data);
                        layerTorch.playOrContinueBit(bitTorchHolding, data);
                    }
                }

                if (player.isCrouching())
                    layerSneak.playOrContinueBit(bitSneak, data);
                else
                    layerSneak.clearAnimation();
            }
        }

        data.renderLeftItemRotation.orientZero();
        data.renderRightItemRotation.orientZero();

        layerZipline.playOrContinueBit(bitZiplineArm, data);

        layerBase.perform(data);
        layerSneak.perform(data);
        layerTorch.perform(data);
        this.performActionAnimations(data, player);
        layerZipline.perform(data);
        layerCape.perform(data);
    }
}
