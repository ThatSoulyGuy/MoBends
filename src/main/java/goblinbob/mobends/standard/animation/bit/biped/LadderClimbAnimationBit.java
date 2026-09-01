package goblinbob.mobends.standard.animation.bit.biped;

import goblinbob.mobends.core.animation.bit.AnimationBit;
import goblinbob.mobends.lib.util.GUtil;
import goblinbob.mobends.standard.data.BipedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

public class LadderClimbAnimationBit extends AnimationBit<BipedEntityData<?>>
{


    @Override
    public void perform(BipedEntityData<?> data)
    {
        final LivingEntity living = data.getEntity();

        data.centerRotation.setSmoothness(.3F).orientZero();

		final float legAnimationOffset = (float) Math.PI;
		final float progress = data.getClimbingCycle();
		final float armSwingRight = (float) Math.sin(progress) * 0.5F + 0.5F;
		final float armSwingLeft = (float) Math.sin(progress + Math.PI) * 0.5F + 0.5F;
		final float armSwingRight2 = (float) Math.sin(progress - 0.3F) * 0.5F + 0.5F;
		final float armSwingLeft2 = (float) Math.sin(progress + Math.PI - 0.3F) * 0.5F + 0.5F;
		final float armSwingDouble = (float) Math.sin(progress * 2) * 0.5F + 0.5F;
		final float armSwingDouble2 = (float) Math.sin(progress * 2 - 1.8F) * 0.5F + 0.5F;

		final float legSwingRight = (float) Math.sin(progress + legAnimationOffset) * 0.5F + 0.5F;
		final float legSwingLeft = (float) Math.sin(progress + legAnimationOffset + Math.PI) * 0.5F + 0.5F;
		final float legSwingRight2 = (float) Math.sin(progress + legAnimationOffset + 0.3F) * 0.5F + 0.5F;
		final float legSwingLeft2 = (float) Math.sin(progress + legAnimationOffset + Math.PI + 0.3F) * 0.5F + 0.5F;

		final float armOrientX = -45F;

		final float climbingRotation = data.getClimbingRotation();
		final float renderRotationY = Mth.wrapDegrees(living.getYRot() - data.headYaw.get() - climbingRotation);
        data.renderRotation.setSmoothness(.6F).orientY(renderRotationY);
        data.localOffset.slideZ(armSwingDouble2, .6F);

        data.body.rotation.setSmoothness(.5F).orientX(armSwingDouble * 10F);
        data.rightArm.rotation.setSmoothness(.5F).orientX(-90F + armOrientX + armSwingRight * 70F);
        data.leftArm.rotation.setSmoothness(.5F).orientX(-90F + armOrientX + armSwingLeft * 70F);
        data.rightForeArm.rotation.setSmoothness(.5F).orientX(armSwingRight2 * -80F);
        data.leftForeArm.rotation.setSmoothness(.5F).orientX(armSwingLeft2 * -80F);

        data.rightLeg.rotation.setSmoothness(.5F).orientX(-45F - legSwingRight * 50F);
        data.leftLeg.rotation.setSmoothness(.5F).orientX(-45F - legSwingLeft * 50F);
        data.rightForeLeg.rotation.setSmoothness(.5F).orientX(20F + legSwingRight2 * 90F);
        data.leftForeLeg.rotation.setSmoothness(.5F).orientX(20F + legSwingLeft2 * 90F);

        data.head.rotation.orientX(data.headPitch.get())
                .rotateY(GUtil.clamp(Mth.wrapDegrees(data.headYaw.get() + renderRotationY), -90F, 90F));

		final float ledgeClimbStart = 0.6F;
        if (data.getLedgeHeight() >= ledgeClimbStart)
        {
            final float armRotX = data.getLedgeHeight() - ledgeClimbStart;
            data.body.rotation.setSmoothness(.5F).orientX(armRotX * 50F);

            data.rightArm.rotation.setSmoothness(.5F).orientX(-100F + armRotX * 40F);
            data.leftArm.rotation.setSmoothness(.5F).orientX(-100F + armRotX * 40F);
            data.rightForeArm.rotation.setSmoothness(.5F).orientX(-10F);
            data.leftForeArm.rotation.setSmoothness(.5F).orientX(-10F);
        }
    }

}
