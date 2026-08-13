package goblinbob.mobends.platform;

import goblinbob.mobends.api.resource.IResourceManager;
import goblinbob.mobends.api.resource.IResourcePath;
import goblinbob.mobends.core.util.ResourceLocationFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class McResourceManager implements IResourceManager
{
    @Override
    public Optional<InputStream> getResource(IResourcePath location)
    {
        ResourceManager manager = Minecraft.getInstance().getResourceManager();
        ResourceLocation loc = getResourceLocation(location);

        try
        {
            Optional<Resource> resource = manager.getResource(loc);
            if (resource.isPresent())
            {
                return Optional.of(resource.get().open());
            }
        }
        catch (IOException e)
        {
        }

        return Optional.empty();
    }

    @Override
    public Collection<InputStream> getResources(IResourcePath location)
    {
        ResourceManager manager = Minecraft.getInstance().getResourceManager();
        ResourceLocation loc = getResourceLocation(location);
        List<InputStream> streams = new ArrayList<>();

        try
        {
            for (Resource resource : manager.getResourceStack(loc))
            {
                try
                {
                    streams.add(resource.open());
                }
                catch (IOException e)
                {
                }
            }
        }
        catch (Exception e)
        {
        }

        return streams;
    }

    @Override
    public Collection<IResourcePath> listResources(String path, String extension)
    {
        ResourceManager manager = Minecraft.getInstance().getResourceManager();
        List<IResourcePath> result = new ArrayList<>();

        manager.listResources(path, loc -> loc.getPath().endsWith(extension))
                .forEach((loc, resource) -> result.add(new McResourcePath(loc)));

        return result;
    }

    @Override
    public IResourcePath createPath(String namespace, String path)
    {
        return new McResourcePath(namespace, path);
    }

    @Override
    @Nullable
    public IResourcePath parsePath(String location)
    {
        try
        {
            ResourceLocation loc = ResourceLocationFactory.parse(location);
            return new McResourcePath(loc);
        }
        catch (Exception e)
        {
            return null;
        }
    }

    private ResourceLocation getResourceLocation(IResourcePath path)
    {
        if (path instanceof McResourcePath mcPath)
        {
            return mcPath.getLocation();
        }
        return ResourceLocationFactory.create(path.getNamespace(), path.getPath());
    }
}
