package goblinbob.mobends.core.util;

import goblinbob.mobends.api.resource.ResourceLocationHelper;
import net.minecraft.resources.ResourceLocation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Factory for creating ResourceLocation objects in a cross-version compatible way.
 *
 * Uses reflection to handle API differences:
 * - MC 1.21.1: ResourceLocation.fromNamespaceAndPath() and ResourceLocation.parse()
 * - MC 1.20.1: new ResourceLocation() constructors
 */
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
            // Try 1.21.1 static methods first
            fromNsPath = ResourceLocation.class.getMethod("fromNamespaceAndPath", String.class, String.class);
        }
        catch (NoSuchMethodException e)
        {
            // Fall back to 1.20.1 constructor
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
            // Try 1.21.1 parse method
            parseMethod = ResourceLocation.class.getMethod("parse", String.class);
        }
        catch (NoSuchMethodException e)
        {
            // Fall back to 1.20.1 constructor
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
        // Utility class
    }

    /**
     * Creates a ResourceLocation from namespace and path.
     *
     * @param namespace The namespace (e.g., "mobends")
     * @param path The path (e.g., "textures/gui/icons.png")
     * @return The ResourceLocation
     */
    public static ResourceLocation create(String namespace, String path)
    {
        // First check if platform helper is available (preferred)
        ResourceLocationHelper helper = ResourceLocationHelper.Holder.getHelper();
        if (helper != null)
        {
            return (ResourceLocation) helper.create(namespace, path);
        }

        // Fall back to reflection
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

    /**
     * Parses a ResourceLocation from a string.
     *
     * @param location The location string (e.g., "minecraft:textures/gui/widgets.png")
     * @return The ResourceLocation
     */
    public static ResourceLocation parse(String location)
    {
        // First check if platform helper is available (preferred)
        ResourceLocationHelper helper = ResourceLocationHelper.Holder.getHelper();
        if (helper != null)
        {
            return (ResourceLocation) helper.parse(location);
        }

        // Fall back to reflection
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
