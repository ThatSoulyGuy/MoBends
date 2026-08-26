package goblinbob.mobends.core.util;

import goblinbob.mobends.api.resource.ResourceLocationHelper;
import net.minecraft.resources.ResourceLocation;

public final class ResourceLocationFactory
{
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

        //? if >=1.21 {
        /*return ResourceLocation.fromNamespaceAndPath(namespace, path);
        *///?} else {
        return new ResourceLocation(namespace, path);
        //?}
    }

    public static ResourceLocation parse(String location)
    {
        ResourceLocationHelper helper = ResourceLocationHelper.Holder.getHelper();
        if (helper != null)
        {
            return (ResourceLocation) helper.parse(location);
        }

        //? if >=1.21 {
        /*return ResourceLocation.parse(location);
        *///?} else {
        return new ResourceLocation(location);
        //?}
    }
}
