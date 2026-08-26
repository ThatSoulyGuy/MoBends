package goblinbob.mobends.compat;

import dev.architectury.platform.Platform;
import net.minecraft.world.entity.LivingEntity;

public class CorpseCompat
{
    private static final String MOD_ID = "corpse";
    private static final String[] RENDER_ENTITY_CLASSES = {
            "de.maxhenkel.corpse.entities.DummyPlayer",
            "de.maxhenkel.corpse.entities.DummySkeleton"
    };

    private static boolean initialized = false;
    private static Class<?>[] renderEntityClasses = null;

    public static void init()
    {
        if (initialized)
        {
            return;
        }
        initialized = true;

        if (!Platform.isModLoaded(MOD_ID))
        {
            return;
        }

        final Class<?>[] resolved = new Class<?>[RENDER_ENTITY_CLASSES.length];
        int found = 0;

        for (final String className : RENDER_ENTITY_CLASSES)
        {
            try
            {
                resolved[found] = Class.forName(className);
                ++found;
            }
            catch (Throwable ignored)
            {
            }
        }

        if (found == 0)
        {
            return;
        }

        final Class<?>[] trimmed = new Class<?>[found];
        System.arraycopy(resolved, 0, trimmed, 0, found);
        renderEntityClasses = trimmed;
    }

    public static boolean isModLoaded()
    {
        if (!initialized)
        {
            init();
        }
        return renderEntityClasses != null;
    }

    public static boolean isCorpse(LivingEntity entity)
    {
        if (entity == null || !isModLoaded())
        {
            return false;
        }

        for (final Class<?> renderEntityClass : renderEntityClasses)
        {
            if (renderEntityClass.isInstance(entity))
            {
                return true;
            }
        }

        return false;
    }
}
