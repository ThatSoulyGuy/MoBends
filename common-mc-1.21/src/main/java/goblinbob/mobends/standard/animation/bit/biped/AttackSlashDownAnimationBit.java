package goblinbob.mobends.standard.animation.bit.biped;

import goblinbob.mobends.core.animation.bit.AnimationBit;
import goblinbob.mobends.core.client.event.DataUpdateHandler;
import goblinbob.mobends.core.client.model.IModelPart;
import goblinbob.mobends.lib.math.SmoothOrientation;
import goblinbob.mobends.lib.util.GUtil;
import goblinbob.mobends.standard.data.BipedEntityData;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.util.Mth;

public class AttackSlashDownAnimationBit extends AnimationBit<BipedEntityData<?>>
{
	private float ticksPlayed;

	@Override
	public void onPlay(BipedEntityData<?> data)
	{
		data.swordTrail.reset();
		
		this.ticksPlayed = 0F;
	}

	@Override
	public void perform(BipedEntityData<?> data)
	{
		data.localOffset.slideToZero(0.3F);

		final LivingEntity living = data.getEntity();
		final HumanoidArm primaryHand = living.getMainArm();

		boolean mainHandSwitch = primaryHand == HumanoidArm.RIGHT;
		// Main Hand Direction Multiplier - it helps switch animation sides depending on
		// what is your main hand.
		float handDirMtp = mainHandSwitch ? 1 : -1;
		IModelPart mainArm = mainHandSwitch ? data.rightArm : data.leftArm;
		IModelPart offArm = mainHandSwitch ? data.leftArm : data.rightArm;
		IModelPart mainForeArm = mainHandSwitch ? data.rightForeArm : data.leftForeArm;
		IModelPart offForeArm = mainHandSwitch ? data.leftForeArm : data.rightForeArm;
		SmoothOrientation mainItemRotation = mainHandSwitch ? data.renderRightItemRotation : data.renderLeftItemRotation;

		if (data.getTicksAfterAttack() < 4F &&
			living.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof SwordItem)
		{
			data.swordTrail.add(data);
		}

		float attackState = this.ticksPlayed / 10F;
		float armSwing = GUtil.clamp(attackState * 3F, 0F, 1F);

		float bodyRotationX = 20F - attackState * 20F;
		float bodyRotationY = (30F + 10F * attackState) * handDirMtp;

		data.body.rotation.setSmoothness(.9F).orientX(bodyRotationX)
											 .orientY(bodyRotationY);
		data.head.rotation.setSmoothness(.9F).orientX(Mth.wrapDegrees(data.headPitch.get()) - bodyRotationX)
						  .rotateY(Mth.wrapDegrees(data.headYaw.get()) - bodyRotationY);
		
		mainArm.getRotation().setSmoothness(.3F).orientZ(60F * handDirMtp)
												.rotateInstantY((-20F + armSwing * 70F) * handDirMtp);
		offArm.getRotation().setSmoothness(.3F).orientZ(-80 * handDirMtp);

		mainForeArm.getRotation().setSmoothness(.3F).orientX(-20F);
		offForeArm.getRotation().setSmoothness(.3F).orientX(-60F);

		if (data.isStillHorizontally() && !living.isPassenger())
		{
			if (!living.isCrouching())
			{
				data.rightLeg.rotation.setSmoothness(.3F).orientX(-30F)
						.rotateZ(10)
						.rotateY(25);
				data.leftLeg.rotation.setSmoothness(.3F).orientX(-30F)
						.rotateZ(-10)
						.rotateY(-25);

				data.rightForeLeg.rotation.setSmoothness(.3F).orientX(30F);
				data.leftForeLeg.rotation.setSmoothness(.3F).orientX(30F);

				data.globalOffset.slideY(-2F);
			}

			data.head.rotation.rotateY(-30 * handDirMtp);
			data.renderRotation.setSmoothness(.3F).orientY(-30 * handDirMtp);
		}

		mainItemRotation.orientInstantX(90);

		ticksPlayed += DataUpdateHandler.ticksPerFrame;
	}
}
