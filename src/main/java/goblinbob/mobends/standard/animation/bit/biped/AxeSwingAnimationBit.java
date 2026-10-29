package goblinbob.mobends.standard.animation.bit.biped;

import goblinbob.mobends.core.animation.bit.AnimationBit;
import goblinbob.mobends.core.client.model.ModelPartTransform;
import goblinbob.mobends.standard.data.BipedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;

public class AxeSwingAnimationBit extends AnimationBit<BipedEntityData<?>>
{
    public static final float DURATION = 14.0F;

    private static final float WIND_UP_TICKS = 2.5F;
    private static final float STRIKE_TICKS = 2.5F;
    private static final float RECOVER_START = 8.0F;

    public record Pose(float armX, float armY, float armZ, float foreArmX, float bodyX, float bodyY, float itemY)
    {
    }

    private final Pose windUpPose;
    private final Pose strikePose;

    public AxeSwingAnimationBit(Pose windUpPose, Pose strikePose)
    {
        this.windUpPose = windUpPose;
        this.strikePose = strikePose;
    }

    @Override
    public void onPlay(BipedEntityData<?> data)
    {
        AttackArms.resetTrails(data);
    }

    @Override
    public void perform(BipedEntityData<?> data)
    {
        final LivingEntity living = data.getEntity();
        final HumanoidArm attackArm = AttackArms.attackingArm(data, living);
        final float ticks = AttackArms.ticksAfterAttack(data, living, attackArm);

        if (ticks >= DURATION)
        {
            return;
        }

        if (ticks >= WIND_UP_TICKS && ticks < WIND_UP_TICKS + STRIKE_TICKS)
        {
            data.swordTrail.add(data, attackArm);
        }

        final float windUp = phase(ticks, 0.0F, WIND_UP_TICKS);
        final float strike = phase(ticks, WIND_UP_TICKS, STRIKE_TICKS);
        final float strength = 1.0F - phase(ticks, RECOVER_START, DURATION - RECOVER_START);

        final boolean rightHanded = attackArm == HumanoidArm.RIGHT;
        final float side = rightHanded ? 1.0F : -1.0F;
        final ModelPartTransform mainArm = rightHanded ? data.rightArm : data.leftArm;
        final ModelPartTransform mainForeArm = rightHanded ? data.rightForeArm : data.leftForeArm;
        final ModelPartTransform offArm = rightHanded ? data.leftArm : data.rightArm;
        final ModelPartTransform offForeArm = rightHanded ? data.leftForeArm : data.rightForeArm;

        data.localOffset.slideToZero(0.3F);

        final float bodyX = blend(windUpPose.bodyX(), strikePose.bodyX(), windUp, strike) * strength;
        final float bodyY = blend(windUpPose.bodyY(), strikePose.bodyY(), windUp, strike) * side * strength;

        data.body.rotation.setSmoothness(.9F).orientX(bodyX).rotateY(bodyY);
        data.head.rotation.setSmoothness(.9F).orientX(Mth.wrapDegrees(data.headPitch.get()) - bodyX)
                .rotateY(Mth.wrapDegrees(data.headYaw.get()) - bodyY);

        mainArm.rotation.setSmoothness(.9F)
                .orientX(blend(windUpPose.armX(), strikePose.armX(), windUp, strike) * strength)
                .rotateZ(blend(windUpPose.armZ(), strikePose.armZ(), windUp, strike) * side * strength)
                .rotateY(blend(windUpPose.armY(), strikePose.armY(), windUp, strike) * side * strength);
        mainForeArm.rotation.setSmoothness(.9F)
                .orientX(blend(windUpPose.foreArmX(), strikePose.foreArmX(), windUp, strike) * strength);

        offArm.rotation.setSmoothness(.3F).orientX((-35.0F * windUp + 55.0F * strike) * strength)
                .rotateZ(-10.0F * side * strength);
        offForeArm.rotation.setSmoothness(.3F).orientX(-30.0F * strength);

        (rightHanded ? data.renderRightItemRotation : data.renderLeftItemRotation).setSmoothness(.9F)
                .orientY(blend(windUpPose.itemY(), strikePose.itemY(), windUp, strike) * side * strength);

        if (data.isStillHorizontally() && !living.isPassenger() && !living.isCrouching() && !data.isInWater())
        {
            final float lunge = strike * strength;
            final ModelPartTransform frontLeg = rightHanded ? data.leftLeg : data.rightLeg;
            final ModelPartTransform frontForeLeg = rightHanded ? data.leftForeLeg : data.rightForeLeg;
            final ModelPartTransform backLeg = rightHanded ? data.rightLeg : data.leftLeg;
            final ModelPartTransform backForeLeg = rightHanded ? data.rightForeLeg : data.leftForeLeg;

            frontLeg.rotation.setSmoothness(.3F).orientX(-35.0F * lunge);
            frontForeLeg.rotation.setSmoothness(.3F).orientX(35.0F * lunge);
            backLeg.rotation.setSmoothness(.3F).orientX(20.0F * lunge);
            backForeLeg.rotation.setSmoothness(.3F).orientX(15.0F * lunge);

            data.globalOffset.slideY(-2.0F * lunge);
        }
    }

    private static float blend(float windUpValue, float strikeValue, float windUp, float strike)
    {
        return windUpValue * windUp + (strikeValue - windUpValue) * strike;
    }

    private static float phase(float ticks, float start, float length)
    {
        final float x = Mth.clamp((ticks - start) / length, 0.0F, 1.0F);
        return x * x * (3.0F - 2.0F * x);
    }
}
