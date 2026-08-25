package goblinbob.mobends.standard.animation.bit.biped;

import goblinbob.mobends.core.animation.bit.AnimationBit;
import goblinbob.mobends.standard.data.BipedEntityData;
import net.minecraft.util.Mth;

public class CrawlingAnimationBit extends AnimationBit<BipedEntityData<?>>
{
	private static final String[] ACTIONS = new String[] { "crawling" };

	private static final float PI = (float) Math.PI;

	private static final float ARM_REACH = -170F;
	private static final float ARM_SPREAD = 14F;
	private static final float ARM_STROKE = 26F;
	private static final float FORE_ARM_BASE = -20F;
	private static final float FORE_ARM_STROKE = 18F;
	private static final float LEG_STROKE = 12F;
	private static final float LEG_SPREAD = 5F;
	private static final float FORE_LEG_BASE = 0F;
	private static final float FORE_LEG_STROKE = 16F;
	private static final float BODY_ROLL = 7F;
	private static final float HEAD_LIFT = -70F;
	private static final float MAX_HEAD_PITCH = 90F;
	private static final float GROUND_OFFSET = -2F;


	@Override
	public void perform(BipedEntityData<?> data)
	{
		data.localOffset.slideToZero(0.3F);
		data.globalOffset.slideX(0F, 0.3F);
		data.globalOffset.slideY(GROUND_OFFSET, 0.3F);
		data.globalOffset.slideZ(0F, 0.3F);
		data.centerRotation.setSmoothness(.3F).orientZero();
		data.renderRotation.setSmoothness(.3F).orientZero();
		data.renderRightItemRotation.setSmoothness(.3F).orientZero();
		data.renderLeftItemRotation.setSmoothness(.3F).orientZero();

		final float limbSwing = data.limbSwing.get() * 0.6662F;
		final float amount = Math.min(data.limbSwingAmount.get(), 1.0F);

		final float stroke = Mth.cos(limbSwing);
		final float strokeOpposite = Mth.cos(limbSwing + PI);

		data.rightArm.rotation.setSmoothness(0.6F)
				.orientX(ARM_REACH + Math.max(stroke, 0F) * ARM_STROKE * amount)
				.rotateZ(ARM_SPREAD);
		data.leftArm.rotation.setSmoothness(0.6F)
				.orientX(ARM_REACH + Math.max(strokeOpposite, 0F) * ARM_STROKE * amount)
				.rotateZ(-ARM_SPREAD);

		data.rightForeArm.rotation.setSmoothness(0.6F)
				.orientX(FORE_ARM_BASE - Math.max(stroke, 0F) * FORE_ARM_STROKE * amount);
		data.leftForeArm.rotation.setSmoothness(0.6F)
				.orientX(FORE_ARM_BASE - Math.max(strokeOpposite, 0F) * FORE_ARM_STROKE * amount);

		data.rightLeg.rotation.setSmoothness(0.7F)
				.orientX(strokeOpposite * LEG_STROKE * amount)
				.rotateZ(LEG_SPREAD);
		data.leftLeg.rotation.setSmoothness(0.7F)
				.orientX(stroke * LEG_STROKE * amount)
				.rotateZ(-LEG_SPREAD);

		data.rightForeLeg.rotation.setSmoothness(0.7F)
				.orientX(FORE_LEG_BASE + Math.max(strokeOpposite, 0F) * FORE_LEG_STROKE * amount);
		data.leftForeLeg.rotation.setSmoothness(0.7F)
				.orientX(FORE_LEG_BASE + Math.max(stroke, 0F) * FORE_LEG_STROKE * amount);

		final float bodyRoll = stroke * BODY_ROLL * amount;
		data.body.rotation.setSmoothness(0.5F).orientY(bodyRoll);

		final float headPitch = Mth.clamp(data.headPitch.get() + HEAD_LIFT, -MAX_HEAD_PITCH, MAX_HEAD_PITCH)
				- HEAD_LIFT;

		data.head.rotation.setSmoothness(0.8F)
				.orientX(headPitch)
				.rotateY(data.headYaw.get() - bodyRoll)
				.rotateX(HEAD_LIFT);
	}
}
