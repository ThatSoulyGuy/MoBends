package goblinbob.mobends.standard.animation.controller;

import goblinbob.mobends.core.animation.bit.AnimationBit;
import goblinbob.mobends.core.animation.controller.IAnimationController;
import goblinbob.mobends.core.animation.layer.HardAnimationLayer;
import goblinbob.mobends.standard.animation.bit.biped.item.BipedActionController;
import goblinbob.mobends.standard.animation.bit.biped.JumpAnimationBit;
import goblinbob.mobends.standard.animation.bit.biped.RidingAnimationBit;
import goblinbob.mobends.standard.animation.bit.biped.SittingAnimationBit;
import goblinbob.mobends.standard.animation.bit.biped.StandAnimationBit;
import goblinbob.mobends.standard.animation.bit.skeleton.WalkAnimationBit;
import goblinbob.mobends.standard.data.SkeletonData;
import net.minecraft.world.entity.monster.AbstractSkeleton;

public class SkeletonController implements IAnimationController<SkeletonData<?>>
{
	protected HardAnimationLayer<SkeletonData<?>> layerBase;
	protected AnimationBit<? extends SkeletonData<?>> bitStand, bitWalk, bitJump, bitRiding, bitSitting;
	protected AnimationBit<SkeletonData<?>> bitSprint;

	protected final BipedActionController actionController = new BipedActionController();

	public SkeletonController()
	{
		this.layerBase = new HardAnimationLayer<>();

		this.bitStand = new StandAnimationBit<SkeletonData<?>>();
		this.bitWalk = new WalkAnimationBit();
		this.bitJump = new JumpAnimationBit<SkeletonData<?>>();
		this.bitRiding = new RidingAnimationBit<SkeletonData<?>>();
		this.bitSitting = new SittingAnimationBit<SkeletonData<?>>();
		this.bitSprint = new goblinbob.mobends.standard.animation.bit.biped.SprintAnimationBit<SkeletonData<?>>();
	}

	public void performActionAnimations(SkeletonData<?> data, AbstractSkeleton skeleton)
	{
		actionController.perform(data, skeleton.getMainArm(), skeleton.getMainHandItem(), skeleton.getOffhandItem(),
				skeleton.getUseItem().getItem());
	}

	@Override
	public void perform(SkeletonData<?> skeletonData)
	{
		AbstractSkeleton skeleton = skeletonData.getEntity();

		if (skeletonData.isRiding())
		{
			this.layerBase.playOrContinueBit(
					skeletonData.isRidingLivingEntity() ? bitRiding : bitSitting, skeletonData);
		}
		else if (!skeletonData.isOnGround() || skeletonData.getTicksAfterTouchdown() < 1)
		{
			this.layerBase.playOrContinueBit(bitJump, skeletonData);
		}
		else
		{
			if (skeletonData.isStillHorizontally())
			{
				this.layerBase.playOrContinueBit(bitStand, skeletonData);
			}
			else if (skeletonData.isMovingAtSprintSpeed())
			{
				this.layerBase.playOrContinueBit(bitSprint, skeletonData);
			}
			else
			{
				this.layerBase.playOrContinueBit(bitWalk, skeletonData);
			}
		}

		this.layerBase.perform(skeletonData);
		this.performActionAnimations(skeletonData, skeleton);
	}
}
