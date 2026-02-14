package goblinbob.mobends.neoforge.compat;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.ModList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * Compatibility helper for Physics Mod.
 * When Physics Mod has active ragdoll/physics on an entity,
 * Mo'Bends defers entirely and does not apply animations.
 *
 * Detection uses a two-tier approach:
 * - Tier 1: entity.isDeadOrDying() when Physics Mod is loaded (ragdoll on death)
 * - Tier 2: Reflection into Physics Mod API for precise ragdoll state check
 */
@OnlyIn(Dist.CLIENT)
public class PhysicsModCompat
{
    private static final Logger LOGGER = LoggerFactory.getLogger("MoBends-PhysicsModCompat");
    private static final String MOD_ID = "physicsmod";

    private static boolean initialized = false;
    private static boolean isLoaded = false;
    private static boolean reflectionAvailable = false;

    // Reflection cache for Physics Mod ragdoll API
    private static Method isRagdollActiveMethod;

    public static void init()
    {
        if (initialized) return;
        initialized = true;

        isLoaded = ModList.get().isLoaded(MOD_ID);

        if (isLoaded)
        {
            LOGGER.info("Physics Mod detected, initializing compatibility layer");
            try
            {
                initReflection();
                reflectionAvailable = true;
                LOGGER.info("Physics Mod compatibility initialized with reflection API");
            }
            catch (Exception e)
            {
                LOGGER.info("Physics Mod reflection API not available, using death-state fallback");
            }
        }
    }

    private static void initReflection() throws Exception
    {
        // Try known Physics Mod API classes across versions
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
                LOGGER.debug("Found Physics Mod class: {}", className);
                break;
            }
            catch (ClassNotFoundException ignored) {}
        }

        if (ragdollApiClass == null)
        {
            throw new ClassNotFoundException("No known Physics Mod API class found");
        }

        // Try to find a method to check ragdoll state per entity
        String[] possibleMethods = { "isRagdollActive", "hasActiveRagdoll", "isEntityPhysicsActive" };

        for (String methodName : possibleMethods)
        {
            try
            {
                isRagdollActiveMethod = ragdollApiClass.getMethod(methodName, net.minecraft.world.entity.Entity.class);
                LOGGER.debug("Found ragdoll check method: {}.{}", ragdollApiClass.getSimpleName(), methodName);
                return;
            }
            catch (NoSuchMethodException ignored) {}

            try
            {
                isRagdollActiveMethod = ragdollApiClass.getMethod(methodName, LivingEntity.class);
                LOGGER.debug("Found ragdoll check method: {}.{}", ragdollApiClass.getSimpleName(), methodName);
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

    /**
     * Check if Physics Mod has active physics/ragdoll on the entity.
     * Tier 2 (reflection) is tried first, falls back to Tier 1 (death state).
     */
    public static boolean hasActivePhysics(LivingEntity entity)
    {
        if (!isModLoaded()) return false;

        // Tier 2: Reflection API for precise state check
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
                LOGGER.debug("Reflection check failed, falling back to death state: {}", e.getMessage());
            }
        }

        // Tier 1: Physics Mod activates ragdoll on entity death
        return entity.isDeadOrDying();
    }

    public static String getCompatInfo()
    {
        if (!isModLoaded()) return "Physics Mod: Not loaded";
        if (reflectionAvailable) return "Physics Mod: Loaded, reflection API active";
        return "Physics Mod: Loaded, death-state fallback active";
    }
}
