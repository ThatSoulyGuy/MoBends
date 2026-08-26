package goblinbob.mobends.standard.animation.controller;

import goblinbob.mobends.core.animation.bit.AnimationBit;
import goblinbob.mobends.core.animation.controller.IAnimationController;
import goblinbob.mobends.core.animation.layer.HardAnimationLayer;
import goblinbob.mobends.standard.animation.bit.biped.JumpAnimationBit;
import goblinbob.mobends.standard.animation.bit.biped.RidingAnimationBit;
import goblinbob.mobends.standard.animation.bit.biped.SittingAnimationBit;
import goblinbob.mobends.standard.animation.bit.biped.StandAnimationBit;
import goblinbob.mobends.standard.animation.bit.biped.WalkAnimationBit;
import goblinbob.mobends.standard.data.VillagerData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class VillagerController implements IAnimationController<VillagerData<?>>
{
    protected HardAnimationLayer<VillagerData<?>> layerBase;

    protected AnimationBit<VillagerData<?>> bitStand, bitWalk, bitJump, bitRiding, bitSitting;

    public VillagerController()
    {
        this.layerBase = new HardAnimationLayer<>();
        this.bitStand = new StandAnimationBit<>();
        this.bitWalk = new WalkAnimationBit<>();
        this.bitJump = new JumpAnimationBit<>();
        this.bitRiding = new RidingAnimationBit<>();
        this.bitSitting = new SittingAnimationBit<>();
    }

    @Override
    public Collection<String> perform(VillagerData<?> data)
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
        else
        {
            this.layerBase.playOrContinueBit(bitWalk, data);
        }

        final List<String> actions = new ArrayList<>();
        this.layerBase.perform(data, actions);
        return actions;
    }
}
