package goblinbob.mobends.api.rendering;

import goblinbob.mobends.api.resource.IResourcePath;

/**
 * Provider for creating render layers.
 * Used to obtain render types for entity rendering.
 */
public interface IRenderLayerProvider
{
    /**
     * Gets the entity solid render layer for a texture
     * @param texture The texture location
     * @return The render layer
     */
    IRenderLayer entitySolid(IResourcePath texture);

    /**
     * Gets the entity cutout render layer for a texture
     * @param texture The texture location
     * @return The render layer
     */
    IRenderLayer entityCutout(IResourcePath texture);

    /**
     * Gets the entity cutout no-cull render layer for a texture
     * @param texture The texture location
     * @return The render layer
     */
    IRenderLayer entityCutoutNoCull(IResourcePath texture);

    /**
     * Gets the entity translucent render layer for a texture
     * @param texture The texture location
     * @return The render layer
     */
    IRenderLayer entityTranslucent(IResourcePath texture);

    /**
     * Gets the entity translucent cull render layer for a texture
     * @param texture The texture location
     * @return The render layer
     */
    IRenderLayer entityTranslucentCull(IResourcePath texture);

    /**
     * Gets the armor cutout no-cull render layer for a texture
     * @param texture The texture location
     * @return The render layer
     */
    IRenderLayer armorCutoutNoCull(IResourcePath texture);

    /**
     * Gets the entity smooth cutout render layer for a texture
     * @param texture The texture location
     * @return The render layer
     */
    IRenderLayer entitySmoothCutout(IResourcePath texture);

    /**
     * Gets the eyes render layer for a texture (glowing eyes)
     * @param texture The texture location
     * @return The render layer
     */
    IRenderLayer eyes(IResourcePath texture);

    /**
     * Gets the lines render layer
     * @return The render layer
     */
    IRenderLayer lines();
}
