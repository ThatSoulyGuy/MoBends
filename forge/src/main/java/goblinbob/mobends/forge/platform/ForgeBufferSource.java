package goblinbob.mobends.forge.platform;

import goblinbob.mobends.api.rendering.IBufferSource;
import goblinbob.mobends.api.rendering.IRenderLayer;
import goblinbob.mobends.api.rendering.IVertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

/**
 * Forge 1.20.1 implementation of IBufferSource.
 * Wraps Minecraft's MultiBufferSource.
 */
public class ForgeBufferSource implements IBufferSource
{
    private final MultiBufferSource bufferSource;

    public ForgeBufferSource(MultiBufferSource bufferSource)
    {
        this.bufferSource = bufferSource;
    }

    @Override
    public IVertexConsumer getBuffer(IRenderLayer renderLayer)
    {
        RenderType renderType = (RenderType) renderLayer.getNative();
        return new ForgeVertexConsumer(bufferSource.getBuffer(renderType));
    }

    @Override
    public Object getNative()
    {
        return bufferSource;
    }

    public MultiBufferSource getBufferSource()
    {
        return bufferSource;
    }
}
