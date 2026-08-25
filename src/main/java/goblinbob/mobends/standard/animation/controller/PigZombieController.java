package goblinbob.mobends.standard.animation.controller;

import goblinbob.mobends.core.animation.bit.AnimationBit;
import goblinbob.mobends.core.animation.controller.IAnimationController;
import goblinbob.mobends.core.animation.layer.HardAnimationLayer;
import goblinbob.mobends.core.client.event.DataUpdateHandler;
import goblinbob.mobends.standard.animation.bit.biped.MobSwingAnimationBit;
import goblinbob.mobends.standard.animation.bit.biped.JumpAnimationBit;
import goblinbob.mobends.standard.animation.bit.biped.RidingAnimationBit;
import goblinbob.mobends.standard.animation.bit.biped.SittingAnimationBit;
import goblinbob.mobends.standard.animation.bit.pigzombie.StandAnimationBit;
import goblinbob.mobends.standard.animation.bit.pigzombie.WalkAnimationBit;
import goblinbob.mobends.standard.data.BipedEntityData;
import goblinbob.mobends.standard.data.PigZombieData;
import net.minecraft.world.entity.monster.ZombifiedPiglin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PigZombieController implements IAnimationController<PigZombieData>
{

	protected HardAnimationLayer<BipedEntityData<ZombifiedPiglin>> layerBase;
	protected HardAnimationLayer<BipedEntityData<?>> layerAction;
	protected AnimationBit<? extends BipedEntityData<ZombifiedPiglin>> bitStand, bitWalk, bitJump, bitRiding, bitSitting;
	protected MobSwingAnimationBit bitAttack;

	public PigZombieController()
	{
		this.layerBase = new HardAnimationLayer<>();
		this.layerAction = new HardAnimationLayer<>();
		this.bitStand = new StandAnimationBit();
		this.bitWalk = new WalkAnimationBit();
		this.bitJump = new JumpAnimationBit<>();
		this.bitRiding = new RidingAnimationBit<BipedEntityData<ZombifiedPiglin>>();
		this.bitSitting = new SittingAnimationBit<BipedEntityData<ZombifiedPiglin>>();
		this.bitAttack = new MobSwingAnimationBit();
	}

	@Override
	public void perform(PigZombieData pigZombieData)
	{
		ZombifiedPiglin pigZombie =  pigZombieData.getEntity();

		if (pigZombieData.isRiding())
		{
			this.layerBase.playOrContinueBit(
					pigZombieData.isRidingLivingEntity() ? bitRiding : bitSitting, pigZombieData);
		}
		else if (!pigZombieData.isOnGround() || pigZombieData.getTicksAfterTouchdown() < 1)
		{
			this.layerBase.playOrContinueBit(bitJump, pigZombieData);
		}
		else
		{
			if (pigZombieData.isStillHorizontally())
			{
				this.layerBase.playOrContinueBit(bitStand, pigZombieData);
			}
			else
			{
				this.layerBase.playOrContinueBit(bitWalk, pigZombieData);
			}
		}

		if (pigZombie.getAttackAnim(DataUpdateHandler.partialTicks) > 0)
		{
			this.layerAction.playOrContinueBit(this.bitAttack, pigZombieData);
		}
		else
		{
			this.layerAction.clearAnimation();
		}

		List<String> actions = new ArrayList<>();
		this.layerBase.perform(pigZombieData);
		this.layerAction.perform(pigZombieData);
	}

}
