package goblinbob.mobends.standard.animation.bit.biped.item;

import goblinbob.mobends.core.animation.bit.AnimationBit;
import goblinbob.mobends.core.client.model.ModelPartTransform;
import goblinbob.mobends.standard.data.BipedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;

public class StabAction extends AnimationBit<BipedEntityData<?>>
{
    private static final float HOLD_PITCH = -44.16F;
    private static final float HOLD_YAW = 5.73F;
    private static final float ELBOW_BEND = 30.0F;

    @Override
    public void perform(BipedEntityData<?> data)
    {
        final LivingEntity entity = data.getEntity();
        if (!entity.swinging)
        {
            return;
        }

        final float swingProgress = data.swingProgress.get();
        final float prepare = (1.0F - Mth.cos(progress(swingProgress, 0.0F, 0.05F) * (float) Math.PI)) * 0.5F;
        final float attack = Mth.square(progress(swingProgress, 0.05F, 0.2F));
        final float retract = inOutExpo(progress(swingProgress, 0.4F, 1.0F));

        final HumanoidArm activeHand = goblinbob.mobends.standard.animation.bit.biped.AttackArms
                .attackingArm(data, entity);
        final float sideMultiplier = activeHand == HumanoidArm.RIGHT ? 1.0F : -1.0F;
        final ModelPartTransform mainArm = activeHand == HumanoidArm.RIGHT ? data.rightArm : data.leftArm;
        final ModelPartTransform mainForeArm = activeHand == HumanoidArm.RIGHT ? data.rightForeArm : data.leftForeArm;

        data.localOffset.slideToZero(0.3F);
        data.centerRotation.setSmoothness(.3F).orientZero();

        final float bodyYaw = (20.0F * prepare - 35.0F * attack + 15.0F * retract) * sideMultiplier;
        data.body.rotation.setSmoothness(0.8F).orientY(bodyYaw);

        float bodyPitch = 0;
        if (entity.isCrouching())
        {
            data.body.rotation.rotateX(20.0F);
            bodyPitch = 20.0F;
        }

        final float headPitch = data.headPitch.get();
        final float headYaw = data.headYaw.get();
        data.head.rotation.setSmoothness(0.8F).orientX(headPitch - bodyPitch)
                .rotateY(headYaw - bodyYaw);

        final float armPitch = Mth.clamp(headPitch + HOLD_PITCH, -120.0F, 30.0F)
                + 90.0F * prepare - 120.0F * attack + 30.0F * retract;
        final float bend = ELBOW_BEND * (prepare - attack);

        mainArm.rotation.orientInstantX(armPitch + bend)
                .rotateY(headYaw - bodyYaw - HOLD_YAW * sideMultiplier).finish();
        mainForeArm.rotation.orientInstantX(-bend);
    }

    private static float progress(float time, float start, float end)
    {
        return Mth.clamp((time - start) / (end - start), 0.0F, 1.0F);
    }

    private static float inOutExpo(float x)
    {
        if (x <= 0.0F || x >= 1.0F)
        {
            return x <= 0.0F ? 0.0F : 1.0F;
        }

        return x < 0.5F
                ? (float) Math.pow(2.0, 20.0 * x - 10.0) * 0.5F
                : (2.0F - (float) Math.pow(2.0, -20.0 * x + 10.0)) * 0.5F;
    }
}
