package goblinbob.mobends.neoforge.platform;

import goblinbob.mobends.api.rendering.IBufferSource;
import goblinbob.mobends.api.rendering.IRenderLayer;
import goblinbob.mobends.api.rendering.IVertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

public class NeoForgeBufferSource implements IBufferSource
{
    private final MultiBufferSource bufferSource;

    public NeoForgeBufferSource(MultiBufferSource bufferSource)
    {
        this.bufferSource = bufferSource;
    }

    @Override
    public IVertexConsumer getBuffer(IRenderLayer renderLayer)
    {
        RenderType renderType = (RenderType) renderLayer.getNative();
        return new NeoForgeVertexConsumer(bufferSource.getBuffer(renderType));
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
