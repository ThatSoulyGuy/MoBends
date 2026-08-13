package goblinbob.mobends.platform;

import goblinbob.mobends.api.resource.IResourcePath;
import goblinbob.mobends.core.util.ResourceLocationFactory;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

public class McResourcePath implements IResourcePath
{
    private final ResourceLocation location;

    public McResourcePath(ResourceLocation location)
    {
        this.location = location;
    }

    public McResourcePath(String namespace, String path)
    {
        this.location = ResourceLocationFactory.create(namespace, path);
    }

    @Override
    public String getNamespace()
    {
        return location.getNamespace();
    }

    @Override
    public String getPath()
    {
        return location.getPath();
    }

    @Override
    public IResourcePath withPath(String path)
    {
        return new McResourcePath(location.getNamespace(), path);
    }

    @Override
    public Object getNative()
    {
        return location;
    }

    public ResourceLocation getLocation()
    {
        return location;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o instanceof IResourcePath that)
        {
            return Objects.equals(getNamespace(), that.getNamespace()) &&
                   Objects.equals(getPath(), that.getPath());
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(location);
    }

    @Override
    public String toString()
    {
        return location.toString();
    }
}
