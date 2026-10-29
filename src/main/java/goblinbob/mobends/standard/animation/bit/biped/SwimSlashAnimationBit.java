package goblinbob.mobends.standard.animation.bit.biped;

import goblinbob.mobends.core.animation.bit.AnimationBit;
import goblinbob.mobends.core.client.model.ModelPartTransform;
import goblinbob.mobends.lib.math.Quaternion;
import goblinbob.mobends.standard.data.BipedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;

public class SwimSlashAnimationBit extends AnimationBit<BipedEntityData<?>>
{
    public record Timing(float windUpTicks, float strikeTicks, float recoverStart, float recoverTicks,
                         float windUpPull, float followThrough, float bodyRoll, float crunch,
                         boolean headFacesSwing)
    {
    }

    public static final Timing SWORD = new Timing(0.0F, 3.3F, 5.0F, 5.0F, 0.0F, 0.0F, 0.25F, 0.0F, false);
    public static final Timing AXE = new Timing(3.5F, 2.5F, 8.0F, 6.0F, 0.35F, 0.15F, 0.45F, 25.0F, true);

    private static final float BLADE_SWING = 40.0F;

    private final float startPitch;
    private final float endPitch;
    private final float startSweep;
    private final float endSweep;
    private final float bladeRoll;
    private final Timing timing;

    public SwimSlashAnimationBit(float startPitch, float endPitch, float startSweep, float endSweep, float bladeRoll,
                                 Timing timing)
    {
        this.startPitch = startPitch;
        this.endPitch = endPitch;
        this.startSweep = startSweep;
        this.endSweep = endSweep;
        this.bladeRoll = bladeRoll;
        this.timing = timing;
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

        if (ticks >= timing.windUpTicks())
        {
            AttackArms.emitTrails(data, living, attackArm, ticks - timing.windUpTicks(), false);
        }

        final float windUp = timing.windUpTicks() > 0.0F ? smoothstep(ticks / timing.windUpTicks()) : 1.0F;
        final float strike = smoothstep((ticks - timing.windUpTicks()) / timing.strikeTicks());
        final float recover = smoothstep((ticks - timing.recoverStart()) / timing.recoverTicks());
        final float path = strike * (1.0F + timing.followThrough()) - timing.windUpPull() * windUp * (1.0F - strike);
        final float lead = (strike * 2.0F - 1.0F) * (1.0F - recover);

        final boolean right = attackArm == HumanoidArm.RIGHT;
        final float side = right ? 1.0F : -1.0F;
        final ModelPartTransform arm = right ? data.rightArm : data.leftArm;
        final ModelPartTransform foreArm = right ? data.rightForeArm : data.leftForeArm;

        final float aim = Mth.clamp(data.headPitch.get(), -60.0F, 60.0F) * 0.5F * (1.0F - recover);
        final float pitch = Mth.lerp(recover, Mth.lerp(path, startPitch, endPitch), -120.0F) + aim;
        final float sweep = Mth.lerp(path, startSweep, endSweep) * side * (1.0F - recover);
        final float yaw = 90.0F * side * recover;

        final float sweepDirection = Math.signum((endSweep - startSweep) * side);
        final float pitchDirection = Math.signum(endPitch - startPitch);

        arm.rotation.setSmoothness(.9F).orientX(pitch).rotateY(yaw).rotateZ(sweep);
        foreArm.rotation.setSmoothness(.3F).orientX(Mth.lerp(recover, -15.0F, -40.0F));
        data.body.rotation.rotateY(-timing.bodyRoll() * sweep)
                .rotateX(timing.crunch() * pitchDirection * (path - 0.5F) * (1.0F - recover));

        final Quaternion blade = new Quaternion();

        if (timing.headFacesSwing())
        {
            final float facing = (float) Math.atan2((endSweep - startSweep) * side, endPitch - startPitch);
            blade.setFromAxisAngle(0.0F, 1.0F, 0.0F, facing * (1.0F - recover));
            (right ? data.renderRightItemRotation : data.renderLeftItemRotation).set(blade.x, blade.y, blade.z, blade.w);
            return;
        }

        final Quaternion roll = new Quaternion();
        blade.setFromAxisAngle(1.0F, 0.0F, 0.0F,
                (90.0F + pitchDirection * BLADE_SWING * lead) * (1.0F - recover) * Mth.DEG_TO_RAD);
        roll.setFromAxisAngle(0.0F, 0.0F, 1.0F, bladeRoll * side * (1.0F - recover) * Mth.DEG_TO_RAD);
        Quaternion.mul(blade, roll, blade);
        blade.rotate(0.0F, 0.0F, 1.0F, -sweepDirection * BLADE_SWING * lead * Mth.DEG_TO_RAD);

        (right ? data.renderRightItemRotation : data.renderLeftItemRotation).set(blade.x, blade.y, blade.z, blade.w);
    }

    private static float smoothstep(float value)
    {
        final float x = Mth.clamp(value, 0.0F, 1.0F);
        return x * x * (3.0F - 2.0F * x);
    }
}
