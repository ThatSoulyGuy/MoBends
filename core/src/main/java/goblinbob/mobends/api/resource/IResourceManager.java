package goblinbob.mobends.api.resource;

import javax.annotation.Nullable;
import java.io.InputStream;
import java.util.Collection;
import java.util.Optional;

public interface IResourceManager
{
    Optional<InputStream> getResource(IResourcePath location);

    Collection<InputStream> getResources(IResourcePath location);

    Collection<IResourcePath> listResources(String path, String extension);

    IResourcePath createPath(String namespace, String path);

    @Nullable
    IResourcePath parsePath(String location);
}
