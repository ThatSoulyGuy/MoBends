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
import goblinbob.mobends.standard.animation.bit.biped.item.BipedActionController;
import goblinbob.mobends.standard.data.BipedEntityData;
import goblinbob.mobends.standard.data.HumanoidMobData;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class HumanoidMobController implements IAnimationController<HumanoidMobData<?>>
{
    protected final HardAnimationLayer<BipedEntityData<?>> layerBase = new HardAnimationLayer<>();

    protected final AnimationBit<BipedEntityData<?>> bitStand = new StandAnimationBit<>();
    protected final AnimationBit<BipedEntityData<?>> bitWalk = new WalkAnimationBit<>();
    protected final AnimationBit<BipedEntityData<?>> bitSprint = new SprintAnimationBit<>();
    protected final AnimationBit<BipedEntityData<?>> bitJump = new JumpAnimationBit<>();
    protected final AnimationBit<BipedEntityData<?>> bitFalling = new FallingAnimationBit();
    protected final AnimationBit<BipedEntityData<?>> bitRiding = new RidingAnimationBit<>();
    protected final AnimationBit<BipedEntityData<?>> bitSitting = new SittingAnimationBit<>();

    protected final BipedActionController actionController = new BipedActionController();

    @Override
    public Collection<String> perform(HumanoidMobData<?> data)
    {
        final LivingEntity entity = data.getEntity();

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

        final List<String> actions = new ArrayList<>();
        layerBase.perform(data, actions);

        actionController.perform(data, entity.getMainArm(),
                entity.getMainHandItem(), entity.getOffhandItem(), entity.getUseItem().getItem());

        return actions;
    }
}
