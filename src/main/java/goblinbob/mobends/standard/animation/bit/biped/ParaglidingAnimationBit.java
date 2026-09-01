package goblinbob.mobends.standard.animation.bit.biped;

import goblinbob.mobends.core.animation.bit.AnimationBit;
import goblinbob.mobends.core.client.event.DataUpdateHandler;
import goblinbob.mobends.standard.data.BipedEntityData;
import net.minecraft.util.Mth;

public class ParaglidingAnimationBit extends AnimationBit<BipedEntityData<?>>
{

	private static final float ARM_PITCH = -166.16F;
	private static final float ELBOW_BEND = 20.0F;
	private static final float LEG_DANGLE = 8.0F;
	private static final float KNEE_BEND = 15.0F;
	private static final float SWAY_SPAN = 4.0F;
	private static final float SWAY_SPEED = 0.09F;


	@Override
	public void perform(BipedEntityData<?> data)
	{
		final float sway = Mth.cos(DataUpdateHandler.getTicks() * SWAY_SPEED) * SWAY_SPAN;

		data.centerRotation.setSmoothness(0.7F).orientZero();
		data.renderRotation.setSmoothness(0.5F).orientX(0.0F);
		data.globalOffset.slideToZero(0.7F);

		data.body.rotation.setSmoothness(0.5F).orientX(0.0F);
		data.head.rotation.setSmoothness(1.0F).orientX(data.headPitch.get())
				.rotateY(data.headYaw.get());

		data.leftArm.rotation.setSmoothness(0.4F).orientX(ARM_PITCH + ELBOW_BEND);
		data.rightArm.rotation.setSmoothness(0.4F).orientX(ARM_PITCH + ELBOW_BEND);
		data.leftForeArm.rotation.setSmoothness(0.4F).orientX(-ELBOW_BEND);
		data.rightForeArm.rotation.setSmoothness(0.4F).orientX(-ELBOW_BEND);

		data.leftLeg.rotation.setSmoothness(0.4F).orientX(-LEG_DANGLE + sway).rotateZ(-3.0F);
		data.rightLeg.rotation.setSmoothness(0.4F).orientX(-LEG_DANGLE - sway).rotateZ(3.0F);
		data.leftForeLeg.rotation.setSmoothness(0.4F).orientX(KNEE_BEND - sway);
		data.rightForeLeg.rotation.setSmoothness(0.4F).orientX(KNEE_BEND + sway);
	}

}
