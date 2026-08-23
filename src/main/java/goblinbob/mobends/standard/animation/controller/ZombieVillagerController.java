package goblinbob.mobends.standard.animation.controller;

import goblinbob.mobends.core.animation.bit.AnimationBit;
import goblinbob.mobends.core.animation.controller.IAnimationController;
import goblinbob.mobends.core.animation.layer.HardAnimationLayer;
import goblinbob.mobends.core.client.event.DataUpdateHandler;
import goblinbob.mobends.standard.animation.bit.biped.MobSwingAnimationBit;
import goblinbob.mobends.standard.animation.bit.biped.JumpAnimationBit;
import goblinbob.mobends.standard.animation.bit.biped.RidingAnimationBit;
import goblinbob.mobends.standard.animation.bit.biped.SittingAnimationBit;
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
	protected AnimationBit<ZombieVillagerData> bitStand, bitWalk, bitJump, bitRiding, bitSitting;
	protected AnimationBit<ZombieVillagerData>[] bitAnimationSet;
	protected MobSwingAnimationBit bitAttack;

	public ZombieVillagerController()
	{
		this.layerBase = new HardAnimationLayer<>();
		this.layerSet = new HardAnimationLayer<>();
		this.layerAction = new HardAnimationLayer<>();
		this.bitAttack = new MobSwingAnimationBit();
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
	public Collection<String> perform(ZombieVillagerData zombieData)
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

		List<String> actions = new ArrayList<String>();
		this.layerBase.perform(zombieData, actions);
		this.layerSet.perform(zombieData, actions);
		this.layerAction.perform(zombieData, actions);
		return actions;
	}

}
