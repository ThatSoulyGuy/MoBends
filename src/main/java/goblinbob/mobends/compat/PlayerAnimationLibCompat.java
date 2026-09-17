package goblinbob.mobends.compat;

import goblinbob.mobends.api.animation.MoBendsAnimationControl;
import goblinbob.mobends.core.client.model.ModelPartTransform;
import goblinbob.mobends.lib.math.Quaternion;
import goblinbob.mobends.lib.math.vector.Vec3f;
import goblinbob.mobends.standard.data.BipedEntityData;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import dev.architectury.platform.Platform;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

public class PlayerAnimationLibCompat
{
    private static final String MOD_ID = "playeranimator";

    private static final float INFLUENCE_EPSILON = 1.0e-3F;

    private static final float[] TORSO_REST = {0.0F, 0.0F, 0.0F};
    private static final float[] HEAD_REST = {0.0F, 0.0F, 0.0F};
    private static final float[] RIGHT_ARM_REST = {-5.0F, 2.0F, 0.0F};
    private static final float[] LEFT_ARM_REST = {5.0F, 2.0F, 0.0F};
    private static final float[] RIGHT_LEG_REST = {-1.9F, 12.0F, 0.1F};
    private static final float[] LEFT_LEG_REST = {1.9F, 12.0F, 0.1F};

    private static boolean initialized = false;
    private static boolean isLoaded = false;

    private static Method getPlayerAnimLayerMethod;
    private static Method isActiveMethod;
    private static Method setupAnimMethod;
    private static Method get3DTransformMethod;
    private static Constructor<?> vec3fConstructor;
    private static Method vec3fGetX;
    private static Method vec3fGetY;
    private static Method vec3fGetZ;
    private static Object transformPosition;
    private static Object transformRotation;
    private static Object transformBend;
    private static Field layerMapField;
    private static boolean layerMapResolved = false;

    private static final Channel torsoRotation = new Channel(3);
    private static final Channel torsoPosition = new Channel(3);
    private static final Channel torsoBend = new Channel(2);
    private static final Channel bodyBend = new Channel(2);
    private static final Channel partRotation = new Channel(3);
    private static final Channel partPosition = new Channel(3);
    private static final Channel partBend = new Channel(2);

    private static final Quaternion bodyBefore = new Quaternion();
    private static final Quaternion bodyAfter = new Quaternion();
    private static final Quaternion bodyAfterInverse = new Quaternion();
    private static final Quaternion torsoTarget = new Quaternion();
    private static final Quaternion totalBend = new Quaternion();
    private static final Quaternion upperBend = new Quaternion();
    private static final Quaternion scratchWorld = new Quaternion();
    private static final Quaternion scratchTarget = new Quaternion();
    private static final Quaternion scratchLocal = new Quaternion();
    private static final Quaternion scratchBend = new Quaternion();
    private static final float[] scratchEuler = new float[3];
    private static final float[] scratchVector = new float[3];

    public static void init()
    {
        if (initialized)
        {
            return;
        }
        initialized = true;

        isLoaded = Platform.isModLoaded(MOD_ID);

        if (isLoaded)
        {
            try
            {
                initReflection();
            }
            catch (Exception e)
            {
                isLoaded = false;
                org.slf4j.LoggerFactory.getLogger("MoBends").warn(
                        "Player Animator was detected but its API could not be bound; "
                                + "animations from other mods will not drive the Mo'Bends model.", e);
            }
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void initReflection() throws Exception
    {
        Class<?> accessClass = Class.forName("dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess");
        getPlayerAnimLayerMethod = accessClass.getMethod("getPlayerAnimLayer", AbstractClientPlayer.class);

        Class<?> animationClass = Class.forName("dev.kosmx.playerAnim.api.layered.IAnimation");
        Class<?> transformTypeClass = Class.forName("dev.kosmx.playerAnim.api.TransformType");
        Class<?> vec3fClass = Class.forName("dev.kosmx.playerAnim.core.util.Vec3f");

        isActiveMethod = animationClass.getMethod("isActive");
        setupAnimMethod = animationClass.getMethod("setupAnim", float.class);
        get3DTransformMethod = animationClass.getMethod("get3DTransform",
                String.class, transformTypeClass, float.class, vec3fClass);

        vec3fConstructor = vec3fClass.getConstructor(float.class, float.class, float.class);
        vec3fGetX = vec3fClass.getMethod("getX");
        vec3fGetY = vec3fClass.getMethod("getY");
        vec3fGetZ = vec3fClass.getMethod("getZ");

        transformPosition = Enum.valueOf((Class<Enum>) transformTypeClass, "POSITION");
        transformRotation = Enum.valueOf((Class<Enum>) transformTypeClass, "ROTATION");
        transformBend = Enum.valueOf((Class<Enum>) transformTypeClass, "BEND");
    }

    public static boolean isModLoaded()
    {
        if (!initialized)
        {
            init();
        }
        return isLoaded;
    }

    private static Object getActiveStack(LivingEntity entity)
    {
        if (!isModLoaded() || !(entity instanceof AbstractClientPlayer player))
        {
            return null;
        }

        if (BetterCombatCompat.blocksAdoption(entity))
        {
            return null;
        }

        final Object stack = getAnimatingStack(player);
        if (stack == null || hasSelfPosingLayer(player))
        {
            return null;
        }

        return stack;
    }

    private static Object getAnimatingStack(AbstractClientPlayer player)
    {
        try
        {
            Object stack = getPlayerAnimLayerMethod.invoke(null, player);
            if (stack == null)
            {
                return null;
            }

            Boolean active = (Boolean) isActiveMethod.invoke(stack);
            return active != null && active ? stack : null;
        }
        catch (Exception e)
        {
            return null;
        }
    }

    public static boolean hasActiveAnimation(LivingEntity entity)
    {
        return getActiveStack(entity) != null;
    }

    public static boolean isAnimatingVanillaModel(LivingEntity entity)
    {
        return isModLoaded() && entity instanceof AbstractClientPlayer player && getAnimatingStack(player) != null;
    }

    private static boolean hasSelfPosingLayer(AbstractClientPlayer player)
    {
        final Map<?, ?> layers = animationLayersOf(player);
        if (layers == null || layers.isEmpty())
        {
            return false;
        }

        for (final Map.Entry<?, ?> entry : layers.entrySet())
        {
            if (!(entry.getKey() instanceof ResourceLocation id) || entry.getValue() == null
                    || !MoBendsAnimationControl.isSelfPosingMod(id.getNamespace()))
            {
                continue;
            }

            try
            {
                final Boolean active = (Boolean) isActiveMethod.invoke(entry.getValue());
                if (active != null && active)
                {
                    return true;
                }
            }
            catch (Exception ignored)
            {
            }
        }

        return false;
    }

    private static Map<?, ?> animationLayersOf(AbstractClientPlayer player)
    {
        if (!layerMapResolved)
        {
            layerMapResolved = true;
            layerMapField = findAnimationLayerMapField(player.getClass());
        }

        if (layerMapField == null)
        {
            return null;
        }

        try
        {
            return layerMapField.get(player) instanceof Map<?, ?> map ? map : null;
        }
        catch (Exception e)
        {
            return null;
        }
    }

    private static Field findAnimationLayerMapField(Class<?> type)
    {
        for (Class<?> current = type; current != null; current = current.getSuperclass())
        {
            for (final Field field : current.getDeclaredFields())
            {
                if (Map.class.isAssignableFrom(field.getType()) && field.getName().contains("modAnimationData"))
                {
                    try
                    {
                        field.setAccessible(true);
                        return field;
                    }
                    catch (Exception e)
                    {
                        return null;
                    }
                }
            }
        }
        return null;
    }

    public static boolean applyToPose(BipedEntityData<?> data, float partialTicks)
    {
        if (data == null)
        {
            return false;
        }

        final Object stack = getActiveStack(data.getEntity());
        if (stack == null)
        {
            return false;
        }

        try
        {
            setupAnimMethod.invoke(stack, partialTicks);

            sample(stack, "torso", transformRotation, partialTicks, torsoRotation);
            sample(stack, "torso", transformPosition, partialTicks, torsoPosition);
            sample(stack, "torso", transformBend, partialTicks, torsoBend);
            sample(stack, "body", transformBend, partialTicks, bodyBend);

            bodyBefore.set(data.body.rotation.getSmooth());

            resolveRotation(torsoRotation, bodyBefore, torsoTarget);
            torsoBendRotation(torsoBend.value[0] + bodyBend.value[0],
                    torsoBend.value[1] + bodyBend.value[1], totalBend);
            torsoBendRotation(bodyBend.value[0], bodyBend.value[1], upperBend);

            Quaternion.mul(torsoTarget, totalBend, bodyAfter);

            final boolean bodyChanged = torsoRotation.isInfluenced() || !totalBend.isIdentity();
            if (bodyChanged)
            {
                data.body.rotation.setSmooth(bodyAfter.x, bodyAfter.y, bodyAfter.z, bodyAfter.w);
            }
            else
            {
                bodyAfter.set(bodyBefore);
            }

            bodyAfterInverse.set(-bodyAfter.x, -bodyAfter.y, -bodyAfter.z, bodyAfter.w);

            boolean offsetsWritten = false;

            if (torsoPosition.isInfluenced())
            {
                applyRootOffset(data.body, torsoPosition, TORSO_REST);
                offsetsWritten = true;
            }

            final boolean upperFollows = bodyChanged || !upperBend.isIdentity();

            offsetsWritten |= applyUpperPart(stack, "head", partialTicks, data.head, null,
                    HEAD_REST, data.body.scale, upperFollows);
            offsetsWritten |= applyUpperPart(stack, "rightArm", partialTicks, data.rightArm, data.rightForeArm,
                    RIGHT_ARM_REST, data.body.scale, upperFollows);
            offsetsWritten |= applyUpperPart(stack, "leftArm", partialTicks, data.leftArm, data.leftForeArm,
                    LEFT_ARM_REST, data.body.scale, upperFollows);

            offsetsWritten |= applyLeg(stack, "rightLeg", partialTicks, data.rightLeg, data.rightForeLeg, RIGHT_LEG_REST);
            offsetsWritten |= applyLeg(stack, "leftLeg", partialTicks, data.leftLeg, data.leftForeLeg, LEFT_LEG_REST);

            if (offsetsWritten)
            {
                data.externalPoseAdopted = true;
            }

            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    private static boolean applyUpperPart(Object stack, String bone, float partialTicks,
                                          ModelPartTransform part, ModelPartTransform lowerPart,
                                          float[] rest, Vec3f bodyScale, boolean followsBody) throws Exception
    {
        if (part == null)
        {
            return false;
        }

        sample(stack, bone, transformRotation, partialTicks, partRotation);
        sample(stack, bone, transformPosition, partialTicks, partPosition);
        if (lowerPart != null)
        {
            sample(stack, bone, transformBend, partialTicks, partBend);
        }
        else
        {
            partBend.clear();
        }

        final float influence = Math.max(partRotation.influence(),
                Math.max(partPosition.influence(), partBend.influence()));

        if (influence <= INFLUENCE_EPSILON && !followsBody)
        {
            return false;
        }

        Quaternion.mul(bodyBefore, part.rotation.getSmooth(), scratchWorld);
        resolveRotation(partRotation, scratchWorld, scratchTarget);
        Quaternion.mul(upperBend, scratchTarget, scratchWorld);
        Quaternion.mul(bodyAfterInverse, scratchWorld, scratchLocal);
        part.rotation.setSmooth(scratchLocal.x, scratchLocal.y, scratchLocal.z, scratchLocal.w);

        boolean offsetsWritten = false;

        if (partPosition.isInfluenced())
        {
            final float positionInfluence = partPosition.influence();

            Quaternion.mul(bodyAfterInverse, upperBend, scratchLocal);
            rotateVector(scratchLocal,
                    partPosition.value[0] - rest[0] * (1.0F - partPosition.weight[0]),
                    partPosition.value[1] - rest[1] * (1.0F - partPosition.weight[1]),
                    partPosition.value[2] - rest[2] * (1.0F - partPosition.weight[2]),
                    scratchVector);

            part.offset.set(
                    part.offset.x * (1.0F - positionInfluence) + divideByScale(scratchVector[0], bodyScale.x),
                    part.offset.y * (1.0F - positionInfluence) + divideByScale(scratchVector[1], bodyScale.y),
                    part.offset.z * (1.0F - positionInfluence) + divideByScale(scratchVector[2], bodyScale.z));

            offsetsWritten = true;
        }

        if (lowerPart != null && influence > INFLUENCE_EPSILON)
        {
            applyLimbBend(lowerPart, partBend, influence);
        }

        return offsetsWritten;
    }

    private static boolean applyLeg(Object stack, String bone, float partialTicks,
                                    ModelPartTransform part, ModelPartTransform lowerPart,
                                    float[] rest) throws Exception
    {
        if (part == null)
        {
            return false;
        }

        sample(stack, bone, transformRotation, partialTicks, partRotation);
        sample(stack, bone, transformPosition, partialTicks, partPosition);
        sample(stack, bone, transformBend, partialTicks, partBend);

        final float influence = Math.max(partRotation.influence(),
                Math.max(partPosition.influence(), partBend.influence()));

        if (influence <= INFLUENCE_EPSILON)
        {
            return false;
        }

        if (partRotation.isInfluenced())
        {
            resolveRotation(partRotation, part.rotation.getSmooth(), scratchTarget);
            part.rotation.setSmooth(scratchTarget.x, scratchTarget.y, scratchTarget.z, scratchTarget.w);
        }

        boolean offsetsWritten = false;

        if (partPosition.isInfluenced())
        {
            applyRootOffset(part, partPosition, rest);
            offsetsWritten = true;
        }

        if (lowerPart != null)
        {
            applyLimbBend(lowerPart, partBend, influence);
        }

        return offsetsWritten;
    }

    private static void applyRootOffset(ModelPartTransform part, Channel position, float[] rest)
    {
        part.offset.set(
                position.value[0] - rest[0] * (1.0F - position.weight[0]) + part.offset.x * position.weight[0],
                position.value[1] - rest[1] * (1.0F - position.weight[1]) + part.offset.y * position.weight[1],
                position.value[2] - rest[2] * (1.0F - position.weight[2]) + part.offset.z * position.weight[2]);
    }

    private static void applyLimbBend(ModelPartTransform lowerPart, Channel bend, float influence)
    {
        limbBendRotation(bend.value[0], bend.value[1], scratchBend);

        final Quaternion current = lowerPart.rotation.getSmooth();
        nlerp(current, scratchBend, Math.min(1.0F, influence), scratchLocal);
        lowerPart.rotation.setSmooth(scratchLocal.x, scratchLocal.y, scratchLocal.z, scratchLocal.w);
    }

    private static void sample(Object stack, String bone, Object type, float partialTicks, Channel dest)
            throws Exception
    {
        final float[] fromZero = query(stack, bone, type, partialTicks, 0.0F, 0.0F, 0.0F);
        final float[] fromOne = query(stack, bone, type, partialTicks, 1.0F, 1.0F, 1.0F);

        if (fromZero == null || fromOne == null)
        {
            dest.clear();
            return;
        }

        for (int i = 0; i < 3; ++i)
        {
            dest.value[i] = fromZero[i];
            dest.weight[i] = fromOne[i] - fromZero[i];
        }
    }

    private static float[] query(Object stack, String bone, Object type, float partialTicks,
                                 float fx, float fy, float fz) throws Exception
    {
        Object fallback = vec3fConstructor.newInstance(fx, fy, fz);
        Object result = get3DTransformMethod.invoke(stack, bone, type, partialTicks, fallback);
        if (result == null)
        {
            return null;
        }
        return new float[]{
                ((Number) vec3fGetX.invoke(result)).floatValue(),
                ((Number) vec3fGetY.invoke(result)).floatValue(),
                ((Number) vec3fGetZ.invoke(result)).floatValue()
        };
    }

    private static void resolveRotation(Channel rotation, Quaternion current, Quaternion dest)
    {
        if (!rotation.isInfluenced())
        {
            dest.set(current);
            return;
        }

        eulerZyxOf(current, scratchEuler);

        fromEulerZyx(
                rotation.resolve(0, scratchEuler[0]),
                rotation.resolve(1, scratchEuler[1]),
                rotation.resolve(2, scratchEuler[2]),
                dest);
    }

    private static void torsoBendRotation(float axis, float bend, Quaternion dest)
    {
        dest.setFromAxisAngle((float) Math.cos(axis), 0.0F, -(float) Math.sin(axis), bend);
    }

    private static void limbBendRotation(float axis, float bend, Quaternion dest)
    {
        dest.setFromAxisAngle((float) Math.cos(axis), 0.0F, (float) Math.sin(axis), bend);
    }

    static void eulerZyxOf(Quaternion q, float[] dest)
    {
        final float sinX = 2.0F * (q.w * q.x + q.y * q.z);
        final float cosX = 1.0F - 2.0F * (q.x * q.x + q.y * q.y);
        dest[0] = (float) Math.atan2(sinX, cosX);

        final float sinY = 2.0F * (q.w * q.y - q.z * q.x);
        dest[1] = Math.abs(sinY) >= 1.0F
                ? (float) Math.copySign(Math.PI / 2.0, sinY)
                : (float) Math.asin(sinY);

        final float sinZ = 2.0F * (q.w * q.z + q.x * q.y);
        final float cosZ = 1.0F - 2.0F * (q.y * q.y + q.z * q.z);
        dest[2] = (float) Math.atan2(sinZ, cosZ);
    }

    static void fromEulerZyx(float x, float y, float z, Quaternion dest)
    {
        final float cx = (float) Math.cos(x * 0.5F);
        final float sx = (float) Math.sin(x * 0.5F);
        final float cy = (float) Math.cos(y * 0.5F);
        final float sy = (float) Math.sin(y * 0.5F);
        final float cz = (float) Math.cos(z * 0.5F);
        final float sz = (float) Math.sin(z * 0.5F);

        dest.set(
                cz * cy * sx - sz * sy * cx,
                cz * sy * cx + sz * cy * sx,
                sz * cy * cx - cz * sy * sx,
                cz * cy * cx + sz * sy * sx);
    }

    static void rotateVector(Quaternion q, float vx, float vy, float vz, float[] dest)
    {
        final float tx = 2.0F * (q.y * vz - q.z * vy);
        final float ty = 2.0F * (q.z * vx - q.x * vz);
        final float tz = 2.0F * (q.x * vy - q.y * vx);

        dest[0] = vx + q.w * tx + (q.y * tz - q.z * ty);
        dest[1] = vy + q.w * ty + (q.z * tx - q.x * tz);
        dest[2] = vz + q.w * tz + (q.x * ty - q.y * tx);
    }

    static void nlerp(Quaternion from, Quaternion to, float t, Quaternion dest)
    {
        final float dot = from.x * to.x + from.y * to.y + from.z * to.z + from.w * to.w;
        final float sign = dot < 0.0F ? -1.0F : 1.0F;

        dest.set(
                from.x + (to.x * sign - from.x) * t,
                from.y + (to.y * sign - from.y) * t,
                from.z + (to.z * sign - from.z) * t,
                from.w + (to.w * sign - from.w) * t);
        dest.normalise();
    }

    private static float divideByScale(float value, float scale)
    {
        return scale == 0.0F ? value : value / scale;
    }

    private static final class Channel
    {
        private final int components;
        private final float[] value = new float[3];
        private final float[] weight = {1.0F, 1.0F, 1.0F};

        private Channel(int components)
        {
            this.components = components;
        }

        private void clear()
        {
            for (int i = 0; i < 3; ++i)
            {
                value[i] = 0.0F;
                weight[i] = 1.0F;
            }
        }

        private float influence()
        {
            float influence = 0.0F;
            for (int i = 0; i < components; ++i)
            {
                influence = Math.max(influence, Math.min(1.0F, Math.max(0.0F, 1.0F - weight[i])));
            }
            return influence;
        }

        private boolean isInfluenced()
        {
            return influence() > INFLUENCE_EPSILON;
        }

        private float resolve(int component, float current)
        {
            return value[component] + current * weight[component];
        }
    }
}
