package goblinbob.mobends.api.resource;

/**
 * Platform-agnostic resource location abstraction.
 * Represents a namespaced identifier (e.g., "minecraft:textures/entity/player.png")
 */
public interface IResourcePath
{
    /**
     * @return The namespace part (e.g., "minecraft", "mobends")
     */
    String getNamespace();

    /**
     * @return The path part (e.g., "textures/entity/player.png")
     */
    String getPath();

    /**
     * @return The full string representation (e.g., "minecraft:textures/entity/player.png")
     */
    default String asString()
    {
        return getNamespace() + ":" + getPath();
    }

    /**
     * Creates a new resource path with a different path but same namespace
     * @param path The new path
     * @return A new resource path
     */
    IResourcePath withPath(String path);

    /**
     * @return The native platform ResourceLocation object
     */
    Object getNative();
}
