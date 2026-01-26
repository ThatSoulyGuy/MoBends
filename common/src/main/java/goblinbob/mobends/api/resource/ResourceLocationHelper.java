package goblinbob.mobends.api.resource;

import goblinbob.mobends.api.platform.PlatformServices;

/**
 * Helper for creating ResourceLocation objects in a cross-version compatible way.
 *
 * In MC 1.21.1: ResourceLocation.fromNamespaceAndPath(namespace, path) and ResourceLocation.parse(string)
 * In MC 1.20.1: new ResourceLocation(namespace, path) and new ResourceLocation(string)
 */
public interface ResourceLocationHelper
{
    /**
     * Creates a ResourceLocation from namespace and path.
     *
     * @param namespace The namespace (e.g., "mobends")
     * @param path The path (e.g., "textures/gui/icons.png")
     * @return The native ResourceLocation object
     */
    Object create(String namespace, String path);

    /**
     * Parses a ResourceLocation from a string.
     *
     * @param location The location string (e.g., "minecraft:textures/gui/widgets.png")
     * @return The native ResourceLocation object
     */
    Object parse(String location);

    /**
     * Singleton holder for the platform-specific implementation.
     */
    class Holder
    {
        private static ResourceLocationHelper helper;

        public static void setHelper(ResourceLocationHelper helper)
        {
            Holder.helper = helper;
        }

        public static ResourceLocationHelper getHelper()
        {
            return helper;
        }
    }
}
