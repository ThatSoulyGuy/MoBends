package goblinbob.mobends.standard.animation.bit.biped.item;

import goblinbob.mobends.core.animation.bit.AnimationBit;
import goblinbob.mobends.core.client.model.ModelPartTransform;
import goblinbob.mobends.standard.data.BipedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class StabAction extends AnimationBit<BipedEntityData<?>>
{
    private static final float HOLD_PITCH = -44.16F;
    private static final float HOLD_YAW = 5.73F;
    private static final float ELBOW_BEND = 30.0F;
    private static final float MOB_COOLDOWN_TICKS = 20.0F;

    public static float stabProgress(BipedEntityData<?> data, HumanoidArm arm)
    {
        final LivingEntity entity = data.getEntity();
        final InteractionHand hand = goblinbob.mobends.standard.animation.bit.biped.AttackArms.handOf(entity, arm);

        if (BipedActionController.isRealSpear(entity.getItemInHand(hand).getItem()))
        {
            return entity.swinging ? data.swingProgress.get() : 1.0F;
        }

        final float cooldown = entity instanceof Player player
                ? player.getCurrentItemAttackStrengthDelay()
                : MOB_COOLDOWN_TICKS;

        return goblinbob.mobends.standard.animation.bit.biped.AttackArms.ticksAfterAttack(data, entity, arm) / cooldown;
    }

    @Override
    public void perform(BipedEntityData<?> data)
    {
        final LivingEntity entity = data.getEntity();
        final float swingProgress = stabProgress(data,
                goblinbob.mobends.standard.animation.bit.biped.AttackArms.attackingArm(data, entity));
        if (swingProgress >= 1.0F)
        {
            return;
        }

        final float prepare = (1.0F - Mth.cos(progress(swingProgress, 0.0F, 0.05F) * (float) Math.PI)) * 0.5F;
        final float attack = Mth.square(progress(swingProgress, 0.05F, 0.2F));
        final float retract = inOutExpo(progress(swingProgress, 0.4F, 1.0F));

        if (goblinbob.mobends.standard.animation.bit.biped.SwimmingAnimationBit.isSwimmingHorizontally(data))
        {
            performSwimming(data, entity, prepare, attack, retract);
            return;
        }

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

    private static void performSwimming(BipedEntityData<?> data, LivingEntity entity,
                                        float prepare, float attack, float retract)
    {
        final HumanoidArm activeHand = goblinbob.mobends.standard.animation.bit.biped.AttackArms
                .attackingArm(data, entity);
        final boolean right = activeHand == HumanoidArm.RIGHT;
        final float side = right ? 1.0F : -1.0F;
        final ModelPartTransform mainArm = right ? data.rightArm : data.leftArm;
        final ModelPartTransform mainForeArm = right ? data.rightForeArm : data.leftForeArm;
        final ModelPartTransform offArm = right ? data.leftArm : data.rightArm;
        final ModelPartTransform offForeArm = right ? data.leftForeArm : data.rightForeArm;

        final float thrust = easeOutBack(attack) * (1.0F - retract);
        final float chamber = prepare * (1.0F - attack);
        final float aim = Mth.clamp(data.headPitch.get(), -60.0F, 60.0F) * 0.5F;

        mainArm.rotation.orientInstantX(-150.0F - 35.0F * thrust + 45.0F * chamber + aim)
                .rotateZ((8.0F + 10.0F * thrust) * side).finish();
        mainForeArm.rotation.orientInstantX(-120.0F * chamber - 25.0F * retract);
        data.body.rotation.rotateY((10.0F * chamber - 25.0F * thrust) * side);

        offArm.rotation.setSmoothness(.6F).orientX(-40.0F + 30.0F * thrust).rotateY(-80.0F * side);
        offForeArm.rotation.setSmoothness(.6F).orientX(-20.0F);

        (right ? data.renderRightItemRotation : data.renderLeftItemRotation)
                .set(Mth.sin(Mth.PI / 4.0F), 0.0F, 0.0F, Mth.cos(Mth.PI / 4.0F));
    }

    public static float itemPitch(float swingProgress)
    {
        return 70.0F * (Mth.square(progress(swingProgress, 0.05F, 0.2F))
                - inOutExpo(progress(swingProgress, 0.4F, 1.0F)));
    }

    private static float easeOutBack(float x)
    {
        final float c = 1.70158F;
        final float t = x - 1.0F;
        return 1.0F + (c + 1.0F) * t * t * t + c * t * t;
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
