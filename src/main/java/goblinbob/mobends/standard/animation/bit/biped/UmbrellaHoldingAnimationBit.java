package goblinbob.mobends.standard.animation.bit.biped;

import goblinbob.mobends.compat.ArtifactsCompat;
import goblinbob.mobends.core.animation.bit.AnimationBit;
import goblinbob.mobends.core.client.model.IModelPart;
import goblinbob.mobends.lib.math.Quaternion;
import goblinbob.mobends.lib.math.SmoothOrientation;
import goblinbob.mobends.standard.data.BipedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;

public class UmbrellaHoldingAnimationBit extends AnimationBit<BipedEntityData<?>>
{

	private static final float RAISE_PITCH = -180.0F;
	private static final float ELBOW_BEND = 20.0F;
	private static final float SMOOTHNESS = 0.45F;
	private static final float SNAP_ANGLE = 60.0F;


	@Override
	public void perform(BipedEntityData<?> data)
	{
		final LivingEntity living = data.getEntity();
		final boolean rightHanded = living.getMainArm() == HumanoidArm.RIGHT;
		final boolean mainHand = ArtifactsCompat.isHoldingUmbrellaUpright(living, InteractionHand.MAIN_HAND);
		final boolean offHand = ArtifactsCompat.isHoldingUmbrellaUpright(living, InteractionHand.OFF_HAND);

		if (mainHand && rightHanded || offHand && !rightHanded)
		{
			raiseArm(data.rightArm, data.rightForeArm);
		}

		if (mainHand && !rightHanded || offHand && rightHanded)
		{
			raiseArm(data.leftArm, data.leftForeArm);
		}
	}

	private void raiseArm(IModelPart arm, IModelPart foreArm)
	{
		holdPitch(arm.getRotation(), RAISE_PITCH + ELBOW_BEND);
		holdPitch(foreArm.getRotation(), -ELBOW_BEND);
	}

	private static void holdPitch(SmoothOrientation rotation, float pitch)
	{
		rotation.setSmoothness(SMOOTHNESS).orientX(pitch);

		if (angleFromPitch(rotation.getSmooth(), pitch) > SNAP_ANGLE)
		{
			rotation.orientInstantX(pitch);
		}
	}

	private static float angleFromPitch(Quaternion current, float pitch)
	{
		final float half = pitch * Mth.DEG_TO_RAD * 0.5F;
		final float dot = Math.abs(current.x * Mth.sin(half) + current.w * Mth.cos(half));
		return (float) Math.toDegrees(2.0 * Math.acos(Math.min(1.0F, dot)));
	}

}
