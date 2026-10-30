package goblinbob.mobends.compat;

import dev.architectury.platform.Platform;
import goblinbob.mobends.core.client.model.IModelPart;
import goblinbob.mobends.lib.math.Quaternion;
import goblinbob.mobends.lib.math.vector.Vec3f;
import goblinbob.mobends.standard.data.BipedEntityData;
import goblinbob.mobends.standard.mutators.BipedMutator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;

import java.util.Map;
import java.util.WeakHashMap;

public final class TouchNMotionCompat
{
    static final int HEAD = 0;
    static final int BODY = 1;
    static final int LEFT_ARM = 2;
    static final int RIGHT_ARM = 3;
    static final int LEFT_LEG = 4;
    static final int RIGHT_LEG = 5;
    static final int LEFT_FOREARM = 6;
    static final int PART_COUNT = 6;
    private static final int BONE_COUNT = 8;
    private static final float CLAIM_TICKS = 1.0F;
    private static final float ATTACK_TICKS = 10.0F;
    private static final float ATTACK_FADE_TICKS = 5.0F;
    private static final float STRAIGHT_FROM = (float) Math.toRadians(15.0);
    private static final float STRAIGHT_AT = (float) Math.toRadians(45.0);

    static final Quaternion[] rotations = new Quaternion[PART_COUNT];
    static final float[] offsets = new float[PART_COUNT * 3];
    static final float[] armWeights = new float[2];

    private static final Map<BipedEntityData<?>, Applied> APPLIED = new WeakHashMap<>();
    private static final Quaternion bodyRotation = new Quaternion();
    private static final Quaternion bodyInverse = new Quaternion();
    private static final Quaternion bodyChangeInverse = new Quaternion();
    private static final Quaternion rotation = new Quaternion();

    private static boolean initialized = false;
    private static boolean installed = false;
    private static float lastAttackTicks = Float.MAX_VALUE;
    private static boolean useSwing = false;

    static
    {
        for (int i = 0; i < PART_COUNT; i++)
        {
            rotations[i] = new Quaternion();
        }
    }

    private TouchNMotionCompat()
    {
    }

    private record Applied(Quaternion[] rotations, float[] offsets)
    {
    }

    private static void init()
    {
        initialized = true;

        if (!Platform.isModLoaded("touch_n_motion"))
        {
            return;
        }

        try
        {
            installed = TouchNMotionHooks.install();
        }
        catch (Throwable e)
        {
            installed = false;
        }
    }

    public static void applyPose(LivingEntity entity, BipedMutator<?, ?, ?> mutator, HumanoidModel<?> model)
    {
        if (!initialized)
        {
            init();
        }

        if (!installed || !(entity instanceof Player player) || model == null || !EmfSupport.isForeignPart(model.head)
                || player.getVehicle() instanceof Boat || ModCompatManager.isExternallyPosed(player)
                || PlayerAnimationLibCompat.isAnimatingVanillaModel(player))
        {
            return;
        }

        final BipedEntityData<?> data = mutator.getRenderData();
        if (data == null || data.externalPoseAdopted || APPLIED.containsKey(data))
        {
            return;
        }

        mutator.syncPosesToVanillaModel(model);
        if (!TouchNMotionHooks.run(player, model))
        {
            return;
        }

        leaveToMoBends(data);

        final Applied applied = new Applied(new Quaternion[BONE_COUNT], new float[PART_COUNT * 3]);
        apply(new IModelPart[] {mutator.getHead(), mutator.getBody(), mutator.getLeftArm(), mutator.getRightArm(),
                mutator.getLeftLeg(), mutator.getRightLeg(), mutator.getLeftForeArm(), mutator.getRightForeArm()}, null);
        apply(partsOf(data), applied);
        APPLIED.put(data, applied);
    }

    public static void prepare(BipedEntityData<?> data)
    {
        if (!initialized)
        {
            init();
        }

        final Minecraft mc = Minecraft.getInstance();
        if (installed && data.getEntity() == mc.player)
        {
            data.hideAttacks(false);
            final float attackTicks = data.getTicksAfterAnyAttack();
            if (attackTicks < lastAttackTicks)
            {
                useSwing = mc.options.keyUse.isDown();
            }
            lastAttackTicks = attackTicks;

            if (useSwing && attackTicks < CLAIM_TICKS)
            {
                if (TouchNMotionHooks.claimsSwing(mc.player))
                {
                    data.cancelAttacks();
                    useSwing = false;
                }
                else
                {
                    data.hideAttacks(true);
                }
            }
        }

        final Applied applied = APPLIED.isEmpty() ? null : APPLIED.remove(data);
        if (applied == null || data.externalPoseAdopted)
        {
            return;
        }

        final IModelPart[] parts = partsOf(data);
        for (int i = 0; i < BONE_COUNT; i++)
        {
            rotation.set(applied.rotations[i]);
            rotation.conjugate();
            parts[i].getRotation().rotateAll(rotation);

            if (i < PART_COUNT)
            {
                ((Vec3f) parts[i].getOffset()).add(-applied.offsets[i * 3], -applied.offsets[i * 3 + 1],
                        -applied.offsets[i * 3 + 2]);
            }
        }
    }

    private static void leaveToMoBends(BipedEntityData<?> data)
    {
        final float attack = 1.0F - Mth.clamp((data.getTicksAfterAnyAttack() - ATTACK_TICKS) / ATTACK_FADE_TICKS, 0.0F, 1.0F);
        if (attack > 0.0F)
        {
            for (int arm = LEFT_ARM; arm <= RIGHT_ARM; arm++)
            {
                towards(rotations[arm], rotations[BODY], attack);
                scaleOffset(arm, 1.0F - attack);
                armWeights[arm - LEFT_ARM] *= 1.0F - attack;
            }
        }

        if (!data.isOnGround() || !data.isStillHorizontally())
        {
            rotations[LEFT_LEG].setIdentity();
            rotations[RIGHT_LEG].setIdentity();
            scaleOffset(BODY, 0.0F);
            scaleOffset(LEFT_LEG, 0.0F);
            scaleOffset(RIGHT_LEG, 0.0F);
        }
    }

    private static void towards(Quaternion q, Quaternion target, float t)
    {
        final float sign = q.x * target.x + q.y * target.y + q.z * target.z + q.w * target.w < 0.0F ? -1.0F : 1.0F;
        q.set(q.x + (target.x * sign - q.x) * t, q.y + (target.y * sign - q.y) * t,
                q.z + (target.z * sign - q.z) * t, q.w + (target.w * sign - q.w) * t);
        q.normalise();
    }

    private static void scaleOffset(int part, float scale)
    {
        offsets[part * 3] *= scale;
        offsets[part * 3 + 1] *= scale;
        offsets[part * 3 + 2] *= scale;
    }

    private static IModelPart[] partsOf(BipedEntityData<?> data)
    {
        return new IModelPart[] {data.head, data.body, data.leftArm, data.rightArm, data.leftLeg, data.rightLeg,
                data.leftForeArm, data.rightForeArm};
    }

    private static void apply(IModelPart[] parts, Applied applied)
    {
        final float[] armTurns = new float[2];
        bodyRotation.set(parts[BODY].getRotation().getSmooth());
        bodyInverse.set(bodyRotation);
        bodyInverse.conjugate();
        bodyChangeInverse.set(rotations[BODY]);
        bodyChangeInverse.conjugate();

        for (int i = 0; i < BONE_COUNT; i++)
        {
            if (parts[i] == null)
            {
                rotation.setIdentity();
            }
            else if (i == HEAD || i == LEFT_ARM || i == RIGHT_ARM)
            {
                Quaternion.mul(bodyChangeInverse, rotations[i], rotation);
                Quaternion.mul(rotation, bodyRotation, rotation);
                Quaternion.mul(bodyInverse, rotation, rotation);

                if (i != HEAD)
                {
                    final float angle = 2.0F * (float) Math.acos(Math.min(1.0F, Math.abs(rotation.w)));
                    armTurns[i - LEFT_ARM] = Mth.clamp((angle - STRAIGHT_FROM) / (STRAIGHT_AT - STRAIGHT_FROM), 0.0F, 1.0F);
                }
            }
            else if (i < PART_COUNT)
            {
                rotation.set(rotations[i]);
            }
            else
            {
                final int arm = i - LEFT_FOREARM;
                straighten(parts[i].getRotation().getSmooth(), Math.max(armWeights[arm], armTurns[arm]), rotation);
            }

            if (parts[i] != null)
            {
                parts[i].getRotation().rotateAll(rotation);

                if (i < PART_COUNT)
                {
                    ((Vec3f) parts[i].getOffset()).add(offsets[i * 3], offsets[i * 3 + 1], offsets[i * 3 + 2]);
                }
            }

            if (applied != null)
            {
                applied.rotations[i] = new Quaternion(rotation.x, rotation.y, rotation.z, rotation.w);
            }
        }

        if (applied != null)
        {
            System.arraycopy(offsets, 0, applied.offsets, 0, offsets.length);
        }
    }

    private static void straighten(Quaternion current, float weight, Quaternion dest)
    {
        final float sign = current.w < 0.0F ? -1.0F : 1.0F;
        dest.set(current.x * (1.0F - weight), current.y * (1.0F - weight), current.z * (1.0F - weight),
                current.w * (1.0F - weight) + sign * weight);
        dest.normalise();
        Quaternion.mul(dest.x, dest.y, dest.z, dest.w, -current.x, -current.y, -current.z, current.w, dest);
    }
}
