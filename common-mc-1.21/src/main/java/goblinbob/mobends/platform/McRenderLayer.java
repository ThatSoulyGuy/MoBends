package goblinbob.mobends.platform;

import goblinbob.mobends.api.rendering.IRenderLayer;
import net.minecraft.client.renderer.RenderType;

/**
 * Shared implementation of IRenderLayer.
 * Wraps Minecraft's RenderType with a platform-agnostic interface.
 */
public class McRenderLayer implements IRenderLayer
{
    private final RenderType renderType;

    public McRenderLayer(RenderType renderType)
    {
        this.renderType = renderType;
    }

    @Override
    public String name()
    {
        return renderType.toString();
    }

    @Override
    public Object getNative()
    {
        return renderType;
    }

    public RenderType getRenderType()
    {
        return renderType;
    }
}
