package goblinbob.mobends.standard.animation.bit.biped.item;

import goblinbob.mobends.core.animation.bit.AnimationBit;
import goblinbob.mobends.core.client.model.ModelPartTransform;
import goblinbob.mobends.core.util.GUtil;
import goblinbob.mobends.standard.data.BipedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;

public class BowAction extends AnimationBit<BipedEntityData<?>>
{
	protected final HumanoidArm actionHand;

	public BowAction(HumanoidArm handSide)
	{
		this.actionHand = handSide;
	}

	@Override
	public void perform(BipedEntityData<?> data)
	{
		data.localOffset.slideToZero(0.3F);

		final LivingEntity living = data.getEntity();
		final float headPitch = data.headPitch.get();
		final float headYaw = data.headYaw.get();

		boolean mainHandSwitch = this.actionHand == HumanoidArm.RIGHT;
		// Main Hand Direction Multiplier - it helps switch animation sides depending on
		// what is your main hand.
		float handDirMtp = mainHandSwitch ? 1 : -1;
		ModelPartTransform mainArm = mainHandSwitch ? data.rightArm : data.leftArm;
		ModelPartTransform offArm = mainHandSwitch ? data.leftArm : data.rightArm;
		ModelPartTransform mainForeArm = mainHandSwitch ? data.rightForeArm : data.leftForeArm;
		ModelPartTransform offForeArm = mainHandSwitch ? data.leftForeArm : data.rightForeArm;

		int aimedBowDuration = living != null ? Math.min(living.getTicksUsingItem(), 15) : 0;

		float bodyTwistY = (((aimedBowDuration - 10) / 5.0f) * -25) * handDirMtp;
		float var2 = (aimedBowDuration / 10.0f);
		float var5 = headPitch - 90F;
		var5 = Math.max(var5, -160);

		float bodyRotationY = -bodyTwistY + headYaw;
		if (data.isClimbing())
		{
			float climbingRotation = data.getClimbingRotation();
			float renderRotationY = Mth.wrapDegrees(living.getYRot() - headYaw - climbingRotation);
			bodyRotationY = Mth.wrapDegrees(headYaw + renderRotationY);

			data.head.rotation.setSmoothness(0.5F).orientX(headPitch);
		}
		else
		{
			data.head.rotation.setSmoothness(0.5F).orientX(headPitch)
												  .rotateY(headYaw - bodyRotationY);
		}

		data.body.rotation.setSmoothness(.8F).orientY(bodyRotationY);

		mainArm.rotation.setSmoothness(.8F).orientX(headPitch - 90F)
				.rotateY(bodyTwistY);
		offArm.rotation.setSmoothness(1F).orientY(80F * handDirMtp)
				// Keeping it close to the arm no matter the head pitch
				.rotateZ((-Mth.cos(headPitch/180F * GUtil.PI) * 40F + 40F) * handDirMtp)
				.rotateX(var5);

		mainForeArm.rotation.setSmoothness(1F).orientX(0);
		offForeArm.rotation.orientX(var2 * -30F);
	}
}
