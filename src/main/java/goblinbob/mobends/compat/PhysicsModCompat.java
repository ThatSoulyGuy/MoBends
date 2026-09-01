package goblinbob.mobends.compat;

import net.minecraft.world.entity.LivingEntity;
import dev.architectury.platform.Platform;

import java.lang.reflect.Method;

public class PhysicsModCompat
{
    private static final String MOD_ID = "physicsmod";

    private static boolean initialized = false;
    private static boolean isLoaded = false;
    private static boolean reflectionAvailable = false;

    private static Method isRagdollActiveMethod;

    public static void init()
    {
        if (initialized) return;
        initialized = true;

        isLoaded = Platform.isModLoaded(MOD_ID);

        if (isLoaded)
        {
            try
            {
                initReflection();
                reflectionAvailable = true;
            }
            catch (Exception ignored)
            {
            }
        }
    }

    private static void initReflection() throws Exception
    {
        String[] possibleClasses = {
            "net.diebuddies.physics.ragdoll.RagdollManager",
            "net.diebuddies.physics.ragdoll.RagdollHook",
            "net.diebuddies.PhysicsApi",
            "net.diebuddies.config.PhysicsConfig"
        };

        Class<?> ragdollApiClass = null;
        for (String className : possibleClasses)
        {
            try
            {
                ragdollApiClass = Class.forName(className);
                break;
            }
            catch (ClassNotFoundException ignored) {}
        }

        if (ragdollApiClass == null)
        {
            throw new ClassNotFoundException("No known Physics Mod API class found");
        }

        String[] possibleMethods = { "isRagdollActive", "hasActiveRagdoll", "isEntityPhysicsActive" };

        for (String methodName : possibleMethods)
        {
            try
            {
                isRagdollActiveMethod = ragdollApiClass.getMethod(methodName, net.minecraft.world.entity.Entity.class);
                return;
            }
            catch (NoSuchMethodException ignored) {}

            try
            {
                isRagdollActiveMethod = ragdollApiClass.getMethod(methodName, LivingEntity.class);
                return;
            }
            catch (NoSuchMethodException ignored) {}
        }

        throw new NoSuchMethodException("No ragdoll state check method found");
    }

    public static boolean isModLoaded()
    {
        if (!initialized) init();
        return isLoaded;
    }

    public static boolean hasActivePhysics(LivingEntity entity)
    {
        if (!isModLoaded()) return false;

        if (reflectionAvailable && isRagdollActiveMethod != null)
        {
            try
            {
                Object result = isRagdollActiveMethod.invoke(null, entity);
                if (result instanceof Boolean)
                {
                    return (Boolean) result;
                }
            }
            catch (Exception e)
            {
            }
        }

        return entity.isDeadOrDying();
    }
}
