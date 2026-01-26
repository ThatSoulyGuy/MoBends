package goblinbob.mobends.standard.animation.bit.biped;

import goblinbob.mobends.core.animation.bit.AnimationBit;
import goblinbob.mobends.core.client.event.DataUpdateHandler;
import goblinbob.mobends.core.client.model.ModelPartTransform;
import goblinbob.mobends.standard.data.BipedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;

public class EatingAnimationBit extends AnimationBit<BipedEntityData<?>>
{
	protected final HumanoidArm actionHand;

	protected float bringUpAnimation;

	public EatingAnimationBit(HumanoidArm handSide)
	{
		this.actionHand = handSide;
	}

	@Override
	public void onPlay(BipedEntityData<?> data)
	{
		bringUpAnimation = 0F;
	}
	
	@Override
	public void perform(BipedEntityData<?> data)
	{
		final float ticks = DataUpdateHandler.getTicks();

		final boolean mainHandSwitch = this.actionHand == HumanoidArm.RIGHT;
		// Main Hand Direction Multiplier - it helps switch animation sides depending on
		// what is your main hand.
		final float handDirMtp = mainHandSwitch ? 1 : -1;
		final ModelPartTransform mainArm = mainHandSwitch ? data.rightArm : data.leftArm;
		final ModelPartTransform mainForeArm = mainHandSwitch ? data.rightForeArm : data.leftForeArm;

		if (bringUpAnimation < 1F)
		{
			bringUpAnimation += DataUpdateHandler.ticksPerFrame * 0.15F;
			bringUpAnimation = Math.min(bringUpAnimation, 1F);
		}
		else
		{
			float wiggle = Mth.cos(ticks * 1F);
			data.head.rotation.orientX(wiggle * 5.0F)
					.rotateY(15.0F * handDirMtp);
		}
		
		mainArm.rotation.orientX(bringUpAnimation * -80.0F)
						.rotateZ(45.0F * bringUpAnimation * handDirMtp);
		mainForeArm.rotation.orientX(bringUpAnimation * -45.0F);
	}
}
