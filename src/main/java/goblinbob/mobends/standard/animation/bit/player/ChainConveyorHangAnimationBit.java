package goblinbob.mobends.standard.animation.bit.player;

import goblinbob.mobends.compat.CreateCompat;
import goblinbob.mobends.core.animation.bit.AnimationBit;
import goblinbob.mobends.core.client.event.DataUpdateHandler;
import goblinbob.mobends.core.client.model.ModelPartTransform;
import goblinbob.mobends.standard.data.BipedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;

public class ChainConveyorHangAnimationBit extends AnimationBit<BipedEntityData<?>>
{
    private static final float SWING_SPEED = 0.3F / (float) Math.PI;
    private static final float SWING_PHASE = 10.0F;

    private static final float BODY_TILT = 15.0F;
    private static final float BODY_TILT_SPAN = 10.0F;
    private static final float BODY_ROLL_SPAN = 3.0F;
    private static final float LIMB_SWAY = 15.0F;

    private static final float GRIP_ARM_PITCH = -150.0F;
    private static final float GRIP_ARM_SPREAD = 15.0F;
    private static final float GRIP_ELBOW_BEND = 10.0F;

    private static final float FREE_ARM_SPREAD = 20.0F;
    private static final float FREE_ELBOW_BEND = 12.0F;

    private static final float LEADING_LEG_PITCH = -25.0F;
    private static final float TRAILING_LEG_PITCH = 10.0F;
    private static final float LEG_SPREAD = 10.0F;
    private static final float KNEE_BEND = 12.0F;

    private static final float SMOOTHNESS = 0.4F;

    @Override
    public void perform(BipedEntityData<?> data)
    {
        final HumanoidArm gripArm = CreateCompat.getChainGripArm(data.getEntity());
        final boolean leftGrip = gripArm == HumanoidArm.LEFT;
        final float side = leftGrip ? -1.0F : 1.0F;

        final float time = DataUpdateHandler.getTicks();
        final float swingPhase = Mth.sin((time + SWING_PHASE) * SWING_SPEED);
        final float lean = side * (BODY_TILT + swingPhase * BODY_TILT_SPAN);
        final float bodyRoll = side * (BODY_TILT + swingPhase * BODY_ROLL_SPAN);
        final float limbSway = Mth.sin(time * SWING_SPEED) * LIMB_SWAY + 0.5F * lean;

        data.centerRotation.setSmoothness(0.7F).orientZero();
        data.renderRotation.setSmoothness(0.5F).orientX(0.0F);
        data.globalOffset.slideToZero(0.7F);
        data.localOffset.slideToZero(0.7F);

        data.body.rotation.setSmoothness(SMOOTHNESS).orientZ(bodyRoll);
        data.head.rotation.setSmoothness(1.0F).orientX(data.headPitch.get())
                .rotateY(data.headYaw.get())
                .rotateZ(lean - bodyRoll);

        final ModelPartTransform gripUpperArm = leftGrip ? data.leftArm : data.rightArm;
        final ModelPartTransform gripForeArm = leftGrip ? data.leftForeArm : data.rightForeArm;
        final ModelPartTransform freeUpperArm = leftGrip ? data.rightArm : data.leftArm;
        final ModelPartTransform freeForeArm = leftGrip ? data.rightForeArm : data.leftForeArm;

        gripUpperArm.rotation.setSmoothness(SMOOTHNESS)
                .orientX(GRIP_ARM_PITCH + GRIP_ELBOW_BEND)
                .rotateZ(side * GRIP_ARM_SPREAD - bodyRoll);
        gripForeArm.rotation.setSmoothness(SMOOTHNESS).orientX(-GRIP_ELBOW_BEND);

        freeUpperArm.rotation.setSmoothness(SMOOTHNESS)
                .orientX(FREE_ELBOW_BEND)
                .rotateZ(-side * FREE_ARM_SPREAD + limbSway);
        freeForeArm.rotation.setSmoothness(SMOOTHNESS).orientX(-FREE_ELBOW_BEND);

        final ModelPartTransform leadingLeg = leftGrip ? data.leftLeg : data.rightLeg;
        final ModelPartTransform leadingForeLeg = leftGrip ? data.leftForeLeg : data.rightForeLeg;
        final ModelPartTransform trailingLeg = leftGrip ? data.rightLeg : data.leftLeg;
        final ModelPartTransform trailingForeLeg = leftGrip ? data.rightForeLeg : data.leftForeLeg;

        leadingLeg.rotation.setSmoothness(SMOOTHNESS)
                .orientX(LEADING_LEG_PITCH)
                .rotateZ(side * LEG_SPREAD + limbSway);
        leadingForeLeg.rotation.setSmoothness(SMOOTHNESS).orientX(KNEE_BEND);

        trailingLeg.rotation.setSmoothness(SMOOTHNESS)
                .orientX(TRAILING_LEG_PITCH)
                .rotateZ(-side * LEG_SPREAD + limbSway);
        trailingForeLeg.rotation.setSmoothness(SMOOTHNESS).orientX(KNEE_BEND);
    }
}
