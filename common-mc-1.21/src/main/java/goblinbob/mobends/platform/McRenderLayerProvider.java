package goblinbob.mobends.platform;

import goblinbob.mobends.api.rendering.IRenderLayer;
import goblinbob.mobends.api.rendering.IRenderLayerProvider;
import goblinbob.mobends.api.resource.IResourcePath;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class McRenderLayerProvider implements IRenderLayerProvider
{
    @Override
    public IRenderLayer entitySolid(IResourcePath texture)
    {
        return new McRenderLayer(RenderType.entitySolid(getResourceLocation(texture)));
    }

    @Override
    public IRenderLayer entityCutout(IResourcePath texture)
    {
        return new McRenderLayer(RenderType.entityCutout(getResourceLocation(texture)));
    }

    @Override
    public IRenderLayer entityCutoutNoCull(IResourcePath texture)
    {
        return new McRenderLayer(RenderType.entityCutoutNoCull(getResourceLocation(texture)));
    }

    @Override
    public IRenderLayer entityTranslucent(IResourcePath texture)
    {
        return new McRenderLayer(RenderType.entityTranslucent(getResourceLocation(texture)));
    }

    @Override
    public IRenderLayer entityTranslucentCull(IResourcePath texture)
    {
        return new McRenderLayer(RenderType.entityTranslucentCull(getResourceLocation(texture)));
    }

    @Override
    public IRenderLayer armorCutoutNoCull(IResourcePath texture)
    {
        return new McRenderLayer(RenderType.armorCutoutNoCull(getResourceLocation(texture)));
    }

    @Override
    public IRenderLayer entitySmoothCutout(IResourcePath texture)
    {
        return new McRenderLayer(RenderType.entitySmoothCutout(getResourceLocation(texture)));
    }

    @Override
    public IRenderLayer eyes(IResourcePath texture)
    {
        return new McRenderLayer(RenderType.eyes(getResourceLocation(texture)));
    }

    @Override
    public IRenderLayer lines()
    {
        return new McRenderLayer(RenderType.lines());
    }

    private ResourceLocation getResourceLocation(IResourcePath path)
    {
        if (path instanceof McResourcePath mcPath)
        {
            return mcPath.getLocation();
        }
        return VersionAdapter.Holder.get().createResourceLocation(path.getNamespace(), path.getPath());
    }
}
