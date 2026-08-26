package goblinbob.mobends.api.animation;

import goblinbob.mobends.core.bender.EntityBenderRegistry;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Predicate;

public final class MoBendsAnimationControl
{
    private static final Logger LOGGER = LoggerFactory.getLogger("MoBends");

    private static final List<Entry> POSE_OVERRIDES = new CopyOnWriteArrayList<>();
    private static final List<Entry> ANIMATION_DEFERRALS = new CopyOnWriteArrayList<>();
    private static final List<Entry> EXTERNAL_ANIMATIONS = new CopyOnWriteArrayList<>();
    private static final List<Entry> STATIC_POSES = new CopyOnWriteArrayList<>();

    private static final Set<String> SELF_POSING_MODS = new CopyOnWriteArraySet<>();

    private static final Set<EntityType<?>> EXCLUDED_TYPES = new CopyOnWriteArraySet<>();
    private static final Set<Class<?>> EXCLUDED_CLASSES = new CopyOnWriteArraySet<>();

    private MoBendsAnimationControl()
    {
    }

    public static void registerPoseOverride(String modId, Predicate<LivingEntity> isPosing)
    {
        put(POSE_OVERRIDES, modId, isPosing);
    }

    public static void registerAnimationDeferral(String modId, Predicate<LivingEntity> shouldDefer)
    {
        put(ANIMATION_DEFERRALS, modId, shouldDefer);
    }

    public static void registerExternalAnimation(String modId, Predicate<LivingEntity> hasAnimation)
    {
        put(EXTERNAL_ANIMATIONS, modId, hasAnimation);
    }

    public static void registerStaticPose(String modId, Predicate<LivingEntity> isStatic)
    {
        put(STATIC_POSES, modId, isStatic);
    }

    public static void registerSelfPosingMod(String modId)
    {
        if (modId != null && !modId.isEmpty())
        {
            SELF_POSING_MODS.add(modId);
        }
    }

    public static void excludeEntityType(EntityType<?> entityType)
    {
        if (entityType != null && EXCLUDED_TYPES.add(entityType))
        {
            EntityBenderRegistry.instance.clearCache();
        }
    }

    public static void excludeEntityClass(Class<? extends LivingEntity> entityClass)
    {
        if (entityClass != null && EXCLUDED_CLASSES.add(entityClass))
        {
            EntityBenderRegistry.instance.clearCache();
        }
    }

    public static boolean isExcluded(LivingEntity entity)
    {
        if (entity == null)
        {
            return false;
        }

        if (!EXCLUDED_TYPES.isEmpty() && EXCLUDED_TYPES.contains(entity.getType()))
        {
            return true;
        }

        for (final Class<?> excluded : EXCLUDED_CLASSES)
        {
            if (excluded.isInstance(entity))
            {
                return true;
            }
        }

        return false;
    }

    public static boolean isExcluded(EntityType<?> entityType, Class<?> entityClass)
    {
        if (entityType != null && EXCLUDED_TYPES.contains(entityType))
        {
            return true;
        }

        if (entityClass == null)
        {
            return false;
        }

        for (final Class<?> excluded : EXCLUDED_CLASSES)
        {
            if (excluded.isAssignableFrom(entityClass))
            {
                return true;
            }
        }

        return false;
    }

    public static void unregister(String modId)
    {
        if (modId == null)
        {
            return;
        }

        POSE_OVERRIDES.removeIf(entry -> entry.modId.equals(modId));
        ANIMATION_DEFERRALS.removeIf(entry -> entry.modId.equals(modId));
        EXTERNAL_ANIMATIONS.removeIf(entry -> entry.modId.equals(modId));
        STATIC_POSES.removeIf(entry -> entry.modId.equals(modId));
        SELF_POSING_MODS.remove(modId);
    }

    public static boolean isPoseOverridden(LivingEntity entity)
    {
        return anyMatch(POSE_OVERRIDES, entity);
    }

    public static boolean isAnimationDeferred(LivingEntity entity)
    {
        return anyMatch(ANIMATION_DEFERRALS, entity);
    }

    public static boolean hasExternalAnimation(LivingEntity entity)
    {
        return anyMatch(EXTERNAL_ANIMATIONS, entity);
    }

    public static boolean isStaticallyPosed(LivingEntity entity)
    {
        return anyMatch(STATIC_POSES, entity);
    }

    public static boolean isSelfPosingMod(String modId)
    {
        return modId != null && SELF_POSING_MODS.contains(modId);
    }

    private static void put(List<Entry> target, String modId, Predicate<LivingEntity> predicate)
    {
        if (modId == null || modId.isEmpty() || predicate == null)
        {
            return;
        }

        target.removeIf(entry -> entry.modId.equals(modId));
        target.add(new Entry(modId, predicate));
    }

    private static boolean anyMatch(List<Entry> entries, LivingEntity entity)
    {
        if (entity == null || entries.isEmpty())
        {
            return false;
        }

        for (final Entry entry : entries)
        {
            try
            {
                if (entry.predicate.test(entity))
                {
                    return true;
                }
            }
            catch (Throwable t)
            {
                entries.remove(entry);
                LOGGER.warn("Mo'Bends animation control predicate from '{}' threw and was removed.", entry.modId, t);
            }
        }

        return false;
    }

    private static final class Entry
    {
        private final String modId;
        private final Predicate<LivingEntity> predicate;

        private Entry(String modId, Predicate<LivingEntity> predicate)
        {
            this.modId = modId;
            this.predicate = predicate;
        }
    }
}
