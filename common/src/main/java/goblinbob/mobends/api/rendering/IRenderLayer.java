package goblinbob.mobends.api.rendering;

import goblinbob.mobends.api.resource.IResourcePath;

/**
 * Platform-agnostic render layer abstraction.
 * Wraps Minecraft's RenderType for defining how geometry is rendered.
 */
public interface IRenderLayer
{
    /**
     * @return The name/identifier of this render layer
     */
    String name();

    /**
     * @return The native platform RenderType object
     */
    Object getNative();
}
