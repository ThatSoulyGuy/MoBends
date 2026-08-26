package goblinbob.mobends.standard.animation.controller;

import goblinbob.mobends.core.animation.bit.AnimationBit;
import goblinbob.mobends.core.animation.controller.IAnimationController;
import goblinbob.mobends.core.animation.layer.HardAnimationLayer;
import goblinbob.mobends.standard.animation.bit.biped.JumpAnimationBit;
import goblinbob.mobends.standard.animation.bit.biped.RidingAnimationBit;
import goblinbob.mobends.standard.animation.bit.biped.SittingAnimationBit;
import goblinbob.mobends.standard.animation.bit.biped.SprintAnimationBit;
import goblinbob.mobends.standard.animation.bit.biped.StandAnimationBit;
import goblinbob.mobends.standard.animation.bit.biped.WalkAnimationBit;
import goblinbob.mobends.standard.data.VillagerData;

public class VillagerController implements IAnimationController<VillagerData<?>>
{
    protected HardAnimationLayer<VillagerData<?>> layerBase;

    protected AnimationBit<VillagerData<?>> bitStand, bitWalk, bitSprint, bitJump, bitRiding, bitSitting;

    public VillagerController()
    {
        this.layerBase = new HardAnimationLayer<>();
        this.bitStand = new StandAnimationBit<>();
        this.bitWalk = new WalkAnimationBit<>();
        this.bitSprint = new SprintAnimationBit<>();
        this.bitJump = new JumpAnimationBit<>();
        this.bitRiding = new RidingAnimationBit<>();
        this.bitSitting = new SittingAnimationBit<>();
    }

    @Override
    public void perform(VillagerData<?> data)
    {
        if (data.isRiding())
        {
            this.layerBase.playOrContinueBit(data.isRidingLivingEntity() ? bitRiding : bitSitting, data);
        }
        else if (!data.isOnGround() || data.getTicksAfterTouchdown() < 1)
        {
            this.layerBase.playOrContinueBit(bitJump, data);
        }
        else if (data.isStillHorizontally())
        {
            this.layerBase.playOrContinueBit(bitStand, data);
        }
        else if (data.isMovingAtSprintSpeed())
        {
            this.layerBase.playOrContinueBit(bitSprint, data);
        }
        else
        {
            this.layerBase.playOrContinueBit(bitWalk, data);
        }

        this.layerBase.perform(data);
        performActionAnimations(data);
    }

    protected void performActionAnimations(VillagerData<?> data)
    {
    }
}
