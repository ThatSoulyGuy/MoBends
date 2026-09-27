package goblinbob.mobends.platform;

import goblinbob.mobends.api.rendering.IRenderLayer;
import goblinbob.mobends.api.rendering.IRenderLayerProvider;
import goblinbob.mobends.api.resource.IResourcePath;
import net.minecraft.client.renderer.RenderType;

public class McRenderLayerProvider implements IRenderLayerProvider
{
    @Override
    public IRenderLayer entitySolid(IResourcePath texture)
    {
        return new McRenderLayer(RenderType.entitySolid(McResourcePath.toLocation(texture)));
    }

    @Override
    public IRenderLayer entityCutout(IResourcePath texture)
    {
        return new McRenderLayer(RenderType.entityCutout(McResourcePath.toLocation(texture)));
    }

    @Override
    public IRenderLayer entityCutoutNoCull(IResourcePath texture)
    {
        return new McRenderLayer(RenderType.entityCutoutNoCull(McResourcePath.toLocation(texture)));
    }

    @Override
    public IRenderLayer entityTranslucent(IResourcePath texture)
    {
        return new McRenderLayer(RenderType.entityTranslucent(McResourcePath.toLocation(texture)));
    }

    @Override
    public IRenderLayer entityTranslucentCull(IResourcePath texture)
    {
        return new McRenderLayer(RenderType.entityTranslucentCull(McResourcePath.toLocation(texture)));
    }

    @Override
    public IRenderLayer armorCutoutNoCull(IResourcePath texture)
    {
        return new McRenderLayer(RenderType.armorCutoutNoCull(McResourcePath.toLocation(texture)));
    }

    @Override
    public IRenderLayer entitySmoothCutout(IResourcePath texture)
    {
        return new McRenderLayer(RenderType.entitySmoothCutout(McResourcePath.toLocation(texture)));
    }

    @Override
    public IRenderLayer eyes(IResourcePath texture)
    {
        return new McRenderLayer(RenderType.eyes(McResourcePath.toLocation(texture)));
    }

    @Override
    public IRenderLayer lines()
    {
        return new McRenderLayer(RenderType.lines());
    }
}
