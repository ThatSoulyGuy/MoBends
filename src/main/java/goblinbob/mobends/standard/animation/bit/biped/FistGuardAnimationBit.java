package goblinbob.mobends.standard.animation.bit.biped;

import goblinbob.mobends.core.animation.bit.AnimationBit;
import goblinbob.mobends.standard.data.BipedEntityData;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.HumanoidArm;

public class FistGuardAnimationBit extends AnimationBit<BipedEntityData<?>>
{


	@Override
	public void perform(BipedEntityData<?> data)
	{
		LivingEntity living = data.getEntity();
		HumanoidArm primaryHand = AttackArms.attackingArm(data, living);

		boolean mainHandSwitch = primaryHand == HumanoidArm.RIGHT;
		float handDirMtp = mainHandSwitch ? 1 : -1;

		if (!data.isStillHorizontally())
		{
			return;
		}

		boolean crouching = living.isCrouching();
		boolean grounded = !crouching && !data.isRiding();

		if (grounded)
			data.globalOffset.slideY(-2.0F);
		data.renderRotation.setSmoothness(.3F).orientY(-20 * handDirMtp);

		data.rightArm.rotation.setSmoothness(.3F).orientX(-90F)
				.rotateZ(20F);
		data.rightForeArm.rotation.setSmoothness(.3F).orientX(-80F);

		data.leftArm.rotation.setSmoothness(.3F).orientX(-90F)
				.rotateZ(-20F);
		data.leftForeArm.rotation.setSmoothness(.3F).orientX(-80F);

		data.body.rotation.rotateX(10);

		if (grounded)
		{
			data.rightLeg.rotation.setSmoothness(.3F).orientX(-30F)
					.rotateZ(10);
			data.leftLeg.rotation.setSmoothness(.3F).orientX(-30F)
					.rotateY(-25F)
					.rotateZ(-10);

			data.rightForeLeg.rotation.setSmoothness(.3F).orientX(30);
			data.leftForeLeg.rotation.setSmoothness(.3F).orientX(30);
		}

		data.head.rotation.rotateX(-10);
		data.head.rotation.rotateY(-20 * handDirMtp);
	}
}
