package goblinbob.mobends.standard.animation.bit.biped;

import goblinbob.mobends.core.animation.bit.AnimationBit;
import goblinbob.mobends.core.client.model.IModelPart;
import goblinbob.mobends.lib.math.SmoothOrientation;
import goblinbob.mobends.standard.data.BipedEntityData;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.util.Mth;
import org.joml.Vector3f;

public class AttackSlashInwardAnimationBit extends AnimationBit<BipedEntityData<?>>
{
	private static final String[] ACTIONS = new String[] { "attack", "attack_slash_inward" };

	@Override
	public String[] getActions(BipedEntityData<?> entityData)
	{
		return ACTIONS;
	}

	@Override
	public void onPlay(BipedEntityData<?> data)
	{
		AttackArms.resetTrails(data);
	}

	@Override
	public void perform(BipedEntityData<?> data)
	{
		data.localOffset.slideToZero(0.3F);

		final LivingEntity living = data.getEntity();
		final HumanoidArm primaryHand = AttackArms.attackingArm(data, living);

		boolean mainHandSwitch = primaryHand == HumanoidArm.RIGHT;
		float handDirMtp = mainHandSwitch ? 1 : -1;
		IModelPart mainArm = mainHandSwitch ? data.rightArm : data.leftArm;
		IModelPart offArm = mainHandSwitch ? data.leftArm : data.rightArm;
		IModelPart mainForeArm = mainHandSwitch ? data.rightForeArm : data.leftForeArm;
		IModelPart offForeArm = mainHandSwitch ? data.leftForeArm : data.rightForeArm;
		SmoothOrientation mainItemRotation = mainHandSwitch ? data.renderRightItemRotation : data.renderLeftItemRotation;
		SmoothOrientation offItemRotation = mainHandSwitch ? data.renderLeftItemRotation : data.renderRightItemRotation;

		final boolean dualWielding = AttackArms.isDualWielding(data);
		final float mainTicks = AttackArms.ticksAfterAttack(data, living, primaryHand);
		final float offTicks = AttackArms.ticksAfterAttack(data, living, AttackArms.offArm(primaryHand));

		AttackArms.emitTrails(data, living, primaryHand, mainTicks, dualWielding);

		float armSwing = Math.min((mainTicks / 10F) * 3F, 1F);
		float offArmSwing = dualWielding ? Math.min((offTicks / 10F) * 3F, 1F) : 0F;

		Vector3f bodyRot = new Vector3f(0, 0, 0);
		bodyRot.x = 20F - (dualWielding ? Math.min(armSwing, offArmSwing) : armSwing) * 20F;
		bodyRot.y = -70F * (armSwing - offArmSwing) * handDirMtp;

		data.body.rotation.setSmoothness(.9F).orientX(bodyRot.x)
				.orientY(bodyRot.y);
		data.head.rotation.setSmoothness(.9F).orientX(Mth.wrapDegrees(data.headPitch.get()) - bodyRot.x)
						  .rotateY(Mth.wrapDegrees(data.headYaw.get()) - bodyRot.y);

		mainArm.getRotation().setSmoothness(.9F).orientZ(90F * handDirMtp)
				.rotateY((60F - armSwing * 180F) * handDirMtp);

		mainForeArm.getRotation().setSmoothness(.3F).orientX(-10);

		if (dualWielding)
		{
			offArm.getRotation().setSmoothness(.9F).orientZ(90F * -handDirMtp)
					.rotateY((60F - offArmSwing * 180F) * -handDirMtp);
			offForeArm.getRotation().setSmoothness(.3F).orientX(-10);
			offItemRotation.setSmoothness(.9F).orientInstantX(180);
		}
		else
		{
			offArm.getRotation().setSmoothness(.3F).orientZ(-20 * handDirMtp);
			offForeArm.getRotation().setSmoothness(.3F).orientX(-60);
		}

		if (data.isStillHorizontally() && !living.isPassenger())
		{
			if (!living.isCrouching())
			{
				data.rightLeg.rotation.orientZ(5)
						.rotateY(15F)
						.rotateX(-20F);
				data.leftLeg.rotation.orientZ(-5)
						.rotateY(-15F)
						.rotateX(-20F);
				data.rightForeLeg.rotation.orientX(25F);

				data.globalOffset.slideY(-1.0F);
			}

			data.renderRotation.setSmoothness(.3F).orientY(0 * handDirMtp);
		}

		mainItemRotation.setSmoothness(.9F).orientInstantX(50.0F);
	}
}
