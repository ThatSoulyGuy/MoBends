package goblinbob.mobends.compat;

import goblinbob.mobends.core.client.model.ModelPartTransform;
import goblinbob.mobends.lib.math.Quaternion;
import goblinbob.mobends.lib.math.SmoothOrientation;
import goblinbob.mobends.standard.data.BipedEntityData;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.LivingEntity;
import dev.architectury.platform.Platform;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class PlayerAnimationLibCompat
{
    private static final String MOD_ID = "playeranimator";

    private static final float RAD_TO_DEG = (float) (180.0 / Math.PI);
    private static final float BEND_EPSILON = 0.0001F;

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
    private static Object transformRotation;
    private static Object transformBend;

    private static final SmoothOrientation scratchOrientation = new SmoothOrientation();
    private static final Quaternion scratchDesired = new Quaternion();
    private static final Quaternion scratchParentInverse = new Quaternion();
    private static final Quaternion scratchLocal = new Quaternion();

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

            applyRotation(stack, "body", partialTicks, data.body, null);

            final Quaternion bodyRotation = data.body.rotation.getSmooth();
            scratchParentInverse.set(-bodyRotation.x, -bodyRotation.y, -bodyRotation.z, bodyRotation.w);

            applyRotation(stack, "head", partialTicks, data.head, scratchParentInverse);
            applyRotation(stack, "rightArm", partialTicks, data.rightArm, scratchParentInverse);
            applyRotation(stack, "leftArm", partialTicks, data.leftArm, scratchParentInverse);

            applyRotation(stack, "rightLeg", partialTicks, data.rightLeg, null);
            applyRotation(stack, "leftLeg", partialTicks, data.leftLeg, null);

            applyBend(stack, "rightArm", partialTicks, data.rightForeArm, true);
            applyBend(stack, "leftArm", partialTicks, data.leftForeArm, true);
            applyBend(stack, "rightLeg", partialTicks, data.rightForeLeg, false);
            applyBend(stack, "leftLeg", partialTicks, data.leftForeLeg, false);

            return true;
        }
        catch (Exception e)
        {
            return false;
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
                (Float) vec3fGetX.invoke(result),
                (Float) vec3fGetY.invoke(result),
                (Float) vec3fGetZ.invoke(result)
        };
    }

    private static float[] queryIfAnimated(Object stack, String bone, Object type, float partialTicks)
            throws Exception
    {
        float[] probeA = query(stack, bone, type, partialTicks, 0.0F, 0.0F, 0.0F);
        float[] probeB = query(stack, bone, type, partialTicks, 1.0F, 1.0F, 1.0F);

        if (probeA == null || probeB == null)
        {
            return null;
        }

        for (int i = 0; i < 3; ++i)
        {
            if (Math.abs(probeA[i] - probeB[i]) > BEND_EPSILON)
            {
                return null;
            }
        }

        return probeA;
    }

    private static void applyRotation(Object stack, String bone, float partialTicks,
                                      ModelPartTransform transform, Quaternion parentInverse) throws Exception
    {
        if (transform == null)
        {
            return;
        }

        float[] rotation = queryIfAnimated(stack, bone, transformRotation, partialTicks);
        if (rotation == null)
        {
            return;
        }

        if (parentInverse == null)
        {
            transform.rotation
                    .orientInstantX(rotation[0] * RAD_TO_DEG)
                    .rotateInstantY(rotation[1] * RAD_TO_DEG)
                    .rotateInstantZ(rotation[2] * RAD_TO_DEG);
            return;
        }

        scratchOrientation
                .orientInstantX(rotation[0] * RAD_TO_DEG)
                .rotateInstantY(rotation[1] * RAD_TO_DEG)
                .rotateInstantZ(rotation[2] * RAD_TO_DEG);
        scratchDesired.set(scratchOrientation.getSmooth());

        Quaternion.mul(parentInverse, scratchDesired, scratchLocal);
        transform.rotation.set(scratchLocal.x, scratchLocal.y, scratchLocal.z, scratchLocal.w);
    }

    private static void applyBend(Object stack, String bone, float partialTicks,
                                  ModelPartTransform transform, boolean isArm) throws Exception
    {
        if (transform == null)
        {
            return;
        }

        float[] bend = queryIfAnimated(stack, bone, transformBend, partialTicks);
        if (bend == null)
        {
            return;
        }

        final float magnitude = Math.abs(bend[1]) * RAD_TO_DEG;
        if (magnitude < BEND_EPSILON)
        {
            return;
        }

        transform.rotation.orientInstantX(isArm ? -magnitude : magnitude);
    }
}
