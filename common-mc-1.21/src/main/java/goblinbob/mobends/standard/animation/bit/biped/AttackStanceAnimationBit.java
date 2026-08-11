package goblinbob.mobends.standard.animation.bit.biped;

import goblinbob.mobends.core.animation.bit.AnimationBit;
import goblinbob.mobends.core.client.event.DataUpdateHandler;
import goblinbob.mobends.core.client.model.IModelPart;
import goblinbob.mobends.lib.math.SmoothOrientation;
import goblinbob.mobends.standard.data.BipedEntityData;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.util.Mth;

public class AttackStanceAnimationBit extends AnimationBit<BipedEntityData<?>>
{
	protected final float PI = (float) Math.PI;
	protected final float kneelDuration = 0.15F;
	protected final float legSpreadSpeed = 0.1F;
	protected float legSpreadAnimation = 0F;

	@Override
	public void onPlay(BipedEntityData<?> entityData)
	{
		this.legSpreadAnimation = 0F;
	}

	@Override
	public void perform(BipedEntityData<?> data)
	{
		LivingEntity entity = data.getEntity();
		HumanoidArm primaryHand = entity.getMainArm();
		boolean crouching = entity.isCrouching();

		boolean mainHandSwitch = primaryHand == HumanoidArm.RIGHT;
		float handDirMtp = mainHandSwitch ? 1 : -1;
		IModelPart mainArm = mainHandSwitch ? data.rightArm : data.leftArm;
		IModelPart offArm = mainHandSwitch ? data.leftArm : data.rightArm;
		IModelPart mainForeArm = mainHandSwitch ? data.rightForeArm : data.leftForeArm;
		IModelPart offForeArm = mainHandSwitch ? data.leftForeArm : data.rightForeArm;
		SmoothOrientation mainItemRotation = mainHandSwitch ? data.renderRightItemRotation : data.renderLeftItemRotation;

		float breath0 = (float) Math.sin(DataUpdateHandler.getTicks() / 5.0);
		float breath1 = (float) Math.cos(DataUpdateHandler.getTicks() / 5.7);

		data.renderRotation.setSmoothness(.3F).orientY(-30 * handDirMtp);

		float bodyRotationX = 20.0F + breath0 * 2.0F;

		data.body.rotation.setSmoothness(0.3F).orientX(bodyRotationX);
		data.head.rotation.rotateY(-30 * handDirMtp);
		data.head.rotation.rotateX(-bodyRotationX);

		if (!crouching)
		{
			data.rightLeg.rotation.setSmoothness(0.3F).orientX(-30)
					.rotateZ(10)
					.rotateY(25);
			data.leftLeg.rotation.setSmoothness(0.3F).orientX(-30)
					.rotateZ(-10)
					.rotateY(-25);

			data.rightForeLeg.rotation.setSmoothness(0.3F).orientX(30);
			data.leftForeLeg.rotation.setSmoothness(0.3F).orientX(30);
		}

		mainArm.getRotation().setSmoothness(0.3F).orientZ(60F * handDirMtp + breath0 * 5F)
				.rotateY(breath1 * 5F);
		offArm.getRotation().setSmoothness(0.3F).orientZ(-60 * handDirMtp + breath1 * 5F);

		mainForeArm.getRotation().setSmoothness(0.3F).orientX(-20);
		offForeArm.getRotation().setSmoothness(0.3F).orientX(-60);

		mainItemRotation.setSmoothness(.3F).orientX(65);
		if (!crouching)
			data.globalOffset.slideY(-2.0F);

		float touchdown = Math.min(data.getTicksAfterTouchdown() * kneelDuration, 1.0F);
		if (!crouching && touchdown < 1.0F)
		{
			data.body.rotation.setSmoothness(1F);
			data.body.rotation.orientX(5F * (1 - touchdown) + 15F);
			data.globalOffset.setY(-Mth.sin(touchdown * PI) * 2F - 2F);
		}
	}
}
