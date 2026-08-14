package goblinbob.mobends.compat.armourers;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

public final class AWHiddenParts
{
    private static final Set<Object> HIDDEN = Collections.newSetFromMap(new IdentityHashMap<>());

    private AWHiddenParts()
    {
    }

    public static void hide(Iterable<?> parts)
    {
        for (final Object part : parts)
        {
            HIDDEN.add(part);
        }
    }

    public static void show(Iterable<?> parts)
    {
        for (final Object part : parts)
        {
            HIDDEN.remove(part);
        }
    }

    public static boolean isHidden(Object part)
    {
        return part != null && !HIDDEN.isEmpty() && HIDDEN.contains(part);
    }
}
