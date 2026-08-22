package goblinbob.mobends.standard.animation.controller;

import goblinbob.mobends.core.animation.bit.AnimationBit;
import goblinbob.mobends.core.animation.controller.IAnimationController;
import goblinbob.mobends.core.animation.layer.HardAnimationLayer;
import goblinbob.mobends.standard.animation.bit.biped.item.BipedActionController;
import goblinbob.mobends.standard.animation.bit.biped.JumpAnimationBit;
import goblinbob.mobends.standard.animation.bit.biped.RidingAnimationBit;
import goblinbob.mobends.standard.animation.bit.biped.SittingAnimationBit;
import goblinbob.mobends.standard.animation.bit.skeleton.StandAnimationBit;
import goblinbob.mobends.standard.animation.bit.skeleton.WalkAnimationBit;
import goblinbob.mobends.standard.data.SkeletonData;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SkeletonController implements IAnimationController<SkeletonData<?>>
{
	protected HardAnimationLayer<SkeletonData<?>> layerBase;
	protected AnimationBit<? extends SkeletonData<?>> bitStand, bitWalk, bitJump, bitRiding, bitSitting;
	protected AnimationBit<SkeletonData<?>> bitSprint;

	protected final BipedActionController actionController = new BipedActionController();

	public SkeletonController()
	{
		this.layerBase = new HardAnimationLayer<>();

		this.bitStand = new StandAnimationBit();
		this.bitWalk = new WalkAnimationBit();
		this.bitJump = new JumpAnimationBit<SkeletonData<?>>();
		this.bitRiding = new RidingAnimationBit<SkeletonData<?>>();
		this.bitSitting = new SittingAnimationBit<SkeletonData<?>>();
		this.bitSprint = new goblinbob.mobends.standard.animation.bit.biped.SprintAnimationBit<SkeletonData<?>>();
	}

	public void performActionAnimations(SkeletonData<?> data, AbstractSkeleton skeleton)
	{
		final HumanoidArm primaryHand = skeleton.getMainArm();
		final ItemStack heldItemMainhand = skeleton.getMainHandItem();
		final ItemStack heldItemOffhand = skeleton.getOffhandItem();
		final Item activeItem = skeleton.getUseItem().getItem();

		actionController.perform(data, primaryHand, heldItemMainhand, heldItemOffhand, activeItem);
	}

	@Override
	public Collection<String> perform(SkeletonData<?> skeletonData)
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

		List<String> actions = new ArrayList<>();
		this.layerBase.perform(skeletonData, actions);
		this.performActionAnimations(skeletonData, skeleton);
		return actions;
	}
}
