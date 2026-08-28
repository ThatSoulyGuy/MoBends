package goblinbob.mobends.standard.animation.controller;

import goblinbob.mobends.core.animation.bit.AnimationBit;
import goblinbob.mobends.core.animation.controller.IAnimationController;
import goblinbob.mobends.core.animation.layer.HardAnimationLayer;
import goblinbob.mobends.core.client.event.DataUpdateHandler;
import goblinbob.mobends.standard.animation.bit.biped.MobSwingAnimationBit;
import goblinbob.mobends.standard.animation.bit.biped.JumpAnimationBit;
import goblinbob.mobends.standard.animation.bit.biped.RidingAnimationBit;
import goblinbob.mobends.standard.animation.bit.biped.SittingAnimationBit;
import goblinbob.mobends.standard.animation.bit.biped.SpearThrowAnimationBit;
import goblinbob.mobends.standard.animation.bit.biped.StandAnimationBit;
import goblinbob.mobends.standard.animation.bit.biped.WalkAnimationBit;
import goblinbob.mobends.standard.animation.bit.zombie_base.ZombieLeanAnimationBit;
import goblinbob.mobends.standard.animation.bit.zombie_base.ZombieStumblingAnimationBit;
import goblinbob.mobends.standard.data.BipedEntityData;
import goblinbob.mobends.standard.data.ZombieVillagerData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ZombieVillagerController implements IAnimationController<ZombieVillagerData>
{

	protected HardAnimationLayer<ZombieVillagerData> layerBase;
	protected HardAnimationLayer<ZombieVillagerData> layerSet;
	protected HardAnimationLayer<BipedEntityData<?>> layerAction;
	protected HardAnimationLayer<BipedEntityData<?>> layerSpear;
	protected AnimationBit<ZombieVillagerData> bitStand, bitWalk, bitJump, bitRiding, bitSitting;
	protected AnimationBit<ZombieVillagerData>[] bitAnimationSet;
	protected MobSwingAnimationBit bitAttack;
	protected AnimationBit<BipedEntityData<?>> bitSpearThrow;

	public ZombieVillagerController()
	{
		this.layerBase = new HardAnimationLayer<>();
		this.layerSet = new HardAnimationLayer<>();
		this.layerAction = new HardAnimationLayer<>();
		this.layerSpear = new HardAnimationLayer<>();
		this.bitAttack = new MobSwingAnimationBit();
		this.bitSpearThrow = new SpearThrowAnimationBit();
		this.bitStand = new StandAnimationBit<>();
		this.bitWalk = new WalkAnimationBit<>();
		this.bitJump = new JumpAnimationBit<>();
		this.bitRiding = new RidingAnimationBit<>();
		this.bitSitting = new SittingAnimationBit<>();
		this.bitAnimationSet = new AnimationBit[] {
			new ZombieLeanAnimationBit(),
			new ZombieStumblingAnimationBit()
		};
	}

	@Override
	public void perform(ZombieVillagerData zombieData)
	{
		if (zombieData.isRiding())
		{
			this.layerBase.playOrContinueBit(
					zombieData.isRidingLivingEntity() ? bitRiding : bitSitting, zombieData);
			this.layerSet.clearAnimation();
		}
		else
		{
			if (!zombieData.isOnGround() || zombieData.getTicksAfterTouchdown() < 1)
			{
				this.layerBase.playOrContinueBit(bitJump, zombieData);
			}
			else
			{
				if (zombieData.isStillHorizontally())
				{
					this.layerBase.playOrContinueBit(bitStand, zombieData);
				}
				else
				{
					this.layerBase.playOrContinueBit(bitWalk, zombieData);
				}
			}

			this.layerSet.playOrContinueBit(bitAnimationSet[zombieData.getAnimationSet()], zombieData);
		}

		if (zombieData.getEntity().getAttackAnim(DataUpdateHandler.partialTicks) > 0
				&& MobSwingAnimationBit.canPerform(zombieData.getEntity()))
		{
			this.layerAction.playOrContinueBit(this.bitAttack, zombieData);
		}
		else
		{
			this.layerAction.clearAnimation();
		}

		if (SpearThrowAnimationBit.getRaisedSpearArm(zombieData.getEntity()) != null)
		{
			this.layerSpear.playOrContinueBit(this.bitSpearThrow, zombieData);
		}
		else
		{
			this.layerSpear.clearAnimation();
		}

		this.layerBase.perform(zombieData);
		this.layerSet.perform(zombieData);
		this.layerAction.perform(zombieData);
		this.layerSpear.perform(zombieData);
	}

}
