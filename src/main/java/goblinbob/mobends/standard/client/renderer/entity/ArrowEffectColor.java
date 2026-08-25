package goblinbob.mobends.standard.client.renderer.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.SpectralArrow;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class ArrowEffectColor
{
    public static final int NO_COLOR = -1;

    private static final Object NO_ACCESSOR = new Object();

    private static final Map<Class<?>, Object> ACCESSOR_CACHE = new HashMap<>();
    private static final Map<Class<?>, Boolean> SPECTRAL_CACHE = new HashMap<>();

    private ArrowEffectColor()
    {
    }

    public static boolean isSpectral(AbstractArrow arrow)
    {
        if (arrow instanceof SpectralArrow)
        {
            return true;
        }

        return SPECTRAL_CACHE.computeIfAbsent(arrow.getClass(), type -> {
            for (Class<?> current = type;
                 current != null && AbstractArrow.class.isAssignableFrom(current);
                 current = current.getSuperclass())
            {
                if (current.getSimpleName().toLowerCase(Locale.ROOT).contains("spectral"))
                {
                    return Boolean.TRUE;
                }
            }

            return Boolean.FALSE;
        });
    }

    public static int getEffectColor(AbstractArrow arrow)
    {
        if (arrow instanceof Arrow vanillaArrow)
        {
            return sanitize(vanillaArrow.getColor());
        }

        final Object accessor = resolveAccessor(arrow);
        if (accessor == NO_ACCESSOR)
        {
            return NO_COLOR;
        }

        try
        {
            @SuppressWarnings("unchecked")
            final EntityDataAccessor<Integer> typed = (EntityDataAccessor<Integer>) accessor;
            return sanitize(arrow.getEntityData().get(typed));
        }
        catch (Throwable t)
        {
            ACCESSOR_CACHE.put(arrow.getClass(), NO_ACCESSOR);
            return NO_COLOR;
        }
    }

    private static Object resolveAccessor(AbstractArrow arrow)
    {
        final Class<?> type = arrow.getClass();

        final Object cached = ACCESSOR_CACHE.get(type);
        if (cached != null)
        {
            return cached;
        }

        Object resolved = NO_ACCESSOR;

        outer:
        for (Class<?> current = type;
             current != null && AbstractArrow.class.isAssignableFrom(current);
             current = current.getSuperclass())
        {
            for (final Field field : current.getDeclaredFields())
            {
                if (!Modifier.isStatic(field.getModifiers())
                        || !EntityDataAccessor.class.isAssignableFrom(field.getType()))
                {
                    continue;
                }

                final String name = field.getName().toLowerCase(Locale.ROOT);
                if (!name.contains("colour") && !name.contains("color"))
                {
                    continue;
                }

                try
                {
                    field.setAccessible(true);
                    final Object candidate = field.get(null);

                    if (candidate instanceof EntityDataAccessor<?> accessor
                            && arrow.getEntityData().get(accessor) instanceof Integer)
                    {
                        resolved = candidate;
                        break outer;
                    }
                }
                catch (Throwable ignored)
                {
                }
            }
        }

        ACCESSOR_CACHE.put(type, resolved);
        return resolved;
    }

    private static int sanitize(int color)
    {
        return color <= 0 ? NO_COLOR : color;
    }
}
