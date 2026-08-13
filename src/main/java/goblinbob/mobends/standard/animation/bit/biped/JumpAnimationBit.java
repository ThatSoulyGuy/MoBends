package goblinbob.mobends.standard.animation.bit.biped;

import goblinbob.mobends.core.animation.bit.AnimationBit;
import goblinbob.mobends.standard.data.BipedEntityData;
import net.minecraft.util.Mth;

public class JumpAnimationBit<T extends BipedEntityData<?>> extends AnimationBit<T> {
    private static final String[] ACTIONS = new String[]{"jump"};

    @Override
    public String[] getActions(T entityData) {
        return ACTIONS;
    }

    @Override
    public void onPlay(T data) {
        data.renderRotation.identity();
        data.centerRotation.identity();
        data.body.rotation.orientInstantX(20f);
        data.rightLeg.rotation.orientInstantX(0f);
        data.leftLeg.rotation.orientInstantX(0f);
        data.rightForeLeg.rotation.orientInstantX(0f);
        data.leftForeLeg.rotation.orientInstantX(0f);
        data.rightArm.rotation.orientInstantZ(2f);
        data.leftArm.rotation.orientInstantZ(-2f);
        data.rightForeArm.rotation.orientInstantX(-20f);
        data.leftForeArm.rotation.orientInstantX(-20f);
    }

    @Override
    public void perform(T data) {
        if (data.getPrevMotionY() < 0 && data.getMotionY() > 0) {
            onPlay(data);
        }

        data.globalOffset.slideToZero(0.3f);
        data.renderRotation.setSmoothness(0.3f).orientZero();
        data.centerRotation.setSmoothness(0.7f).orientZero();
        data.renderRightItemRotation.setSmoothness(0.3f).orientZero();
        data.renderLeftItemRotation.setSmoothness(0.3f).orientZero();

        float bodyRotationX = Math.max(1.0f - data.getTicksInAir() * 0.1f, 0.0f);
        data.body.rotation.setSmoothness(0.2f).orientX(bodyRotationX);
        data.rightArm.rotation.setSmoothness(0.05f).orientZ(45f);
        data.leftArm.rotation.setSmoothness(0.05f).orientZ(-45f);
        data.rightForeArm.rotation.setSmoothness(0.3f).orientX(0f);
        data.leftForeArm.rotation.setSmoothness(0.3f).orientX(0f);

        data.head.rotation.orientX(data.headPitch.get() - bodyRotationX).rotateY(data.headYaw.get());

        if (!data.isStillHorizontally()) {
            float limbSwing = data.limbSwing.get() * 0.6662f;
            float limbSwingAmount = (float) (0.7f * data.limbSwingAmount.get() / Math.PI * 180f);

            data.rightLeg.rotation.setSmoothness(1.0f).orientX(-5f + Mth.cos(limbSwing) * limbSwingAmount);
            data.leftLeg.rotation.setSmoothness(1.0f)
                    .orientX((float) (-5f + Mth.cos(limbSwing + (float) Math.PI) * limbSwingAmount));

            float limbSwingVar = (float) ((limbSwing / Math.PI) % 2);
            data.leftForeLeg.rotation.setSmoothness(0.3f).orientX(limbSwingVar > 1 ? 45f : 0f);
            data.rightForeLeg.rotation.setSmoothness(0.3f).orientX(limbSwingVar > 1 ? 0f : 45f);
            data.leftForeArm.rotation.setSmoothness(0.3f)
                    .orientX((float) ((Mth.cos(limbSwing + (float) Math.PI / 2) / 2f + 0.5f) * -20f));
            data.rightForeArm.rotation.setSmoothness(0.3f)
                    .orientX((Mth.cos(limbSwing) / 2f + 0.5f) * -20f);
        } else {
            data.rightLeg.rotation.setSmoothness(0.1f).orientZ(10f);
            data.rightLeg.rotation.setSmoothness(0.3f).rotateX(-45f);
            data.leftLeg.rotation.setSmoothness(0.1f).orientZ(-10f);
            data.leftLeg.rotation.setSmoothness(0.3f).rotateX(-17f);
            data.rightForeLeg.rotation.setSmoothness(0.3f).orientX(70f);
            data.leftForeLeg.rotation.setSmoothness(0.3f).orientX(17f);
        }
    }
}
