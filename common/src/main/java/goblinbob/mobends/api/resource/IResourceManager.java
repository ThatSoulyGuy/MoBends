package goblinbob.mobends.api.resource;

import javax.annotation.Nullable;
import java.io.InputStream;
import java.util.Collection;
import java.util.Optional;

/**
 * Platform-agnostic resource manager abstraction.
 * Provides access to resources from resource packs and the mod's assets.
 */
public interface IResourceManager
{
    /**
     * Gets a resource as an input stream
     * @param location The resource location
     * @return An optional containing the input stream, or empty if not found
     */
    Optional<InputStream> getResource(IResourcePath location);

    /**
     * Gets all resources matching a location (from all resource packs)
     * @param location The resource location
     * @return A collection of input streams
     */
    Collection<InputStream> getResources(IResourcePath location);

    /**
     * Lists all resources in a directory
     * @param path The directory path
     * @param extension The file extension to filter by (e.g., ".json")
     * @return A collection of resource paths
     */
    Collection<IResourcePath> listResources(String path, String extension);

    /**
     * Creates a resource path from namespace and path
     * @param namespace The namespace (e.g., "mobends")
     * @param path The path (e.g., "bendspacks/default.json")
     * @return The resource path
     */
    IResourcePath createPath(String namespace, String path);

    /**
     * Parses a resource path from a string (e.g., "mobends:bendspacks/default.json")
     * @param location The location string
     * @return The resource path, or null if invalid
     */
    @Nullable
    IResourcePath parsePath(String location);
}
