package goblinbob.mobends.core.util;

import goblinbob.mobends.api.resource.ResourceLocationHelper;
import net.minecraft.resources.ResourceLocation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public final class ResourceLocationFactory
{
    private static final Method FROM_NAMESPACE_AND_PATH;
    private static final Method PARSE_METHOD;
    private static final Constructor<ResourceLocation> STRING_STRING_CONSTRUCTOR;
    private static final Constructor<ResourceLocation> STRING_CONSTRUCTOR;

    static
    {
        Method fromNsPath = null;
        Method parseMethod = null;
        Constructor<ResourceLocation> ssConstructor = null;
        Constructor<ResourceLocation> sConstructor = null;

        try
        {
            fromNsPath = ResourceLocation.class.getMethod("fromNamespaceAndPath", String.class, String.class);
        }
        catch (NoSuchMethodException e)
        {
            try
            {
                ssConstructor = ResourceLocation.class.getConstructor(String.class, String.class);
            }
            catch (NoSuchMethodException ex)
            {
                throw new RuntimeException("Cannot find ResourceLocation creation method", ex);
            }
        }

        try
        {
            parseMethod = ResourceLocation.class.getMethod("parse", String.class);
        }
        catch (NoSuchMethodException e)
        {
            try
            {
                sConstructor = ResourceLocation.class.getConstructor(String.class);
            }
            catch (NoSuchMethodException ex)
            {
                throw new RuntimeException("Cannot find ResourceLocation parse method", ex);
            }
        }

        FROM_NAMESPACE_AND_PATH = fromNsPath;
        PARSE_METHOD = parseMethod;
        STRING_STRING_CONSTRUCTOR = ssConstructor;
        STRING_CONSTRUCTOR = sConstructor;
    }

    private ResourceLocationFactory()
    {
    }

    public static ResourceLocation create(String namespace, String path)
    {
        ResourceLocationHelper helper = ResourceLocationHelper.Holder.getHelper();
        if (helper != null)
        {
            return (ResourceLocation) helper.create(namespace, path);
        }

        try
        {
            if (FROM_NAMESPACE_AND_PATH != null)
            {
                return (ResourceLocation) FROM_NAMESPACE_AND_PATH.invoke(null, namespace, path);
            }
            else if (STRING_STRING_CONSTRUCTOR != null)
            {
                return STRING_STRING_CONSTRUCTOR.newInstance(namespace, path);
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException("Failed to create ResourceLocation", e);
        }
        throw new IllegalStateException("No ResourceLocation creation method available");
    }

    public static ResourceLocation parse(String location)
    {
        ResourceLocationHelper helper = ResourceLocationHelper.Holder.getHelper();
        if (helper != null)
        {
            return (ResourceLocation) helper.parse(location);
        }

        try
        {
            if (PARSE_METHOD != null)
            {
                return (ResourceLocation) PARSE_METHOD.invoke(null, location);
            }
            else if (STRING_CONSTRUCTOR != null)
            {
                return STRING_CONSTRUCTOR.newInstance(location);
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException("Failed to parse ResourceLocation", e);
        }
        throw new IllegalStateException("No ResourceLocation parse method available");
    }
}
