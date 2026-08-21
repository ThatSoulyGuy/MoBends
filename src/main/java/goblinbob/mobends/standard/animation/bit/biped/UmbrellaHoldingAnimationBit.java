package goblinbob.mobends.standard.animation.bit.biped;

import goblinbob.mobends.compat.ArtifactsCompat;
import goblinbob.mobends.core.animation.bit.AnimationBit;
import goblinbob.mobends.core.client.model.IModelPart;
import goblinbob.mobends.standard.data.BipedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;

public class UmbrellaHoldingAnimationBit extends AnimationBit<BipedEntityData<?>>
{

	private static final String[] ACTIONS = new String[] { "umbrella_holding" };

	private static final float RAISE_PITCH = -180.0F;
	private static final float ELBOW_BEND = 20.0F;

	@Override
	public String[] getActions(BipedEntityData<?> data)
	{
		return ACTIONS;
	}

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
		arm.getRotation().orientX(RAISE_PITCH + ELBOW_BEND);
		foreArm.getRotation().orientX(-ELBOW_BEND);
	}

}
