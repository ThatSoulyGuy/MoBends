package goblinbob.mobends.compat;

import net.minecraft.client.model.geom.ModelPart;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public final class EmfSupport
{
    private static final String EMF_PACKAGE = "traben.";

    private static Field childrenField;
    private static boolean childrenResolved = false;

    private static Class<?> cachedPartClass;
    private static Method cachedGetRoot;
    private static Method cachedAnimate;

    private EmfSupport()
    {
    }

    public static boolean isForeignPart(ModelPart part)
    {
        return part != null && part.getClass().getName().startsWith(EMF_PACKAGE);
    }

    public static void advanceAnimation(ModelPart part)
    {
        if (!isForeignPart(part))
        {
            return;
        }

        try
        {
            if (cachedPartClass != part.getClass())
            {
                cachedPartClass = part.getClass();
                cachedGetRoot = part.getClass().getMethod("getRoot");
                cachedAnimate = null;
            }

            if (cachedGetRoot == null)
            {
                return;
            }

            final Object root = cachedGetRoot.invoke(part);
            if (root == null)
            {
                return;
            }

            if (cachedAnimate == null)
            {
                cachedAnimate = root.getClass().getMethod("animate");
            }

            cachedAnimate.invoke(root);
        }
        catch (Exception e)
        {
            cachedGetRoot = null;
            cachedAnimate = null;
        }
    }

    @SuppressWarnings("unchecked")
    public static Collection<ModelPart> childrenOf(ModelPart part)
    {
        if (part == null)
        {
            return Collections.emptyList();
        }

        resolveChildrenField();

        if (childrenField == null)
        {
            return Collections.emptyList();
        }

        try
        {
            final Object value = childrenField.get(part);
            if (value instanceof Map<?, ?> map)
            {
                return ((Map<String, ModelPart>) map).values();
            }
        }
        catch (Exception ignored)
        {
        }

        return Collections.emptyList();
    }

    private static void resolveChildrenField()
    {
        if (childrenResolved)
        {
            return;
        }
        childrenResolved = true;

        for (Field field : ModelPart.class.getDeclaredFields())
        {
            if (Map.class.isAssignableFrom(field.getType()))
            {
                field.setAccessible(true);
                childrenField = field;
                return;
            }
        }
    }
}
