package goblinbob.mobends.compat;

import dev.architectury.platform.Platform;
import net.minecraft.world.entity.LivingEntity;

public final class GuardVillagersCompat
{
    private static final String MOD_ID = "guardvillagers";

    private static final String[] GUARD_CLASSES = {
            "tallestegg.guardvillagers.entities.Guard",
            "tallestegg.guardvillagers.common.entity.Guard",
            "dev.sterner.guardvillagers.common.entity.Guard"
    };

    private static boolean resolved = false;
    private static Class<LivingEntity> entityClass = null;

    private GuardVillagersCompat()
    {
    }

    @SuppressWarnings("unchecked")
    public static Class<LivingEntity> getEntityClass()
    {
        if (resolved)
        {
            return entityClass;
        }
        resolved = true;

        if (!Platform.isModLoaded(MOD_ID))
        {
            return null;
        }

        for (final String className : GUARD_CLASSES)
        {
            try
            {
                final Class<?> candidate = Class.forName(className);
                if (LivingEntity.class.isAssignableFrom(candidate))
                {
                    entityClass = (Class<LivingEntity>) candidate;
                    break;
                }
            }
            catch (Throwable ignored)
            {
            }
        }

        return entityClass;
    }
}
