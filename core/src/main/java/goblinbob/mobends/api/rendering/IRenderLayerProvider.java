package goblinbob.mobends.api.rendering;

import goblinbob.mobends.api.resource.IResourcePath;

public interface IRenderLayerProvider
{
    IRenderLayer entitySolid(IResourcePath texture);

    IRenderLayer entityCutout(IResourcePath texture);

    IRenderLayer entityCutoutNoCull(IResourcePath texture);

    IRenderLayer entityTranslucent(IResourcePath texture);

    IRenderLayer entityTranslucentCull(IResourcePath texture);

    IRenderLayer armorCutoutNoCull(IResourcePath texture);

    IRenderLayer entitySmoothCutout(IResourcePath texture);

    IRenderLayer eyes(IResourcePath texture);

    IRenderLayer lines();
}
