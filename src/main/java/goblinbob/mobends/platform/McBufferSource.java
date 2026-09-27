package goblinbob.mobends.platform;

import com.mojang.blaze3d.vertex.VertexConsumer;
import goblinbob.mobends.api.rendering.IBufferSource;
import goblinbob.mobends.api.rendering.IRenderLayer;
import goblinbob.mobends.api.rendering.IVertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

import java.util.function.Function;

public class McBufferSource implements IBufferSource
{
    private final MultiBufferSource bufferSource;
    private final Function<VertexConsumer, IVertexConsumer> vertexConsumerWrapper;

    public McBufferSource(MultiBufferSource bufferSource, Function<VertexConsumer, IVertexConsumer> vertexConsumerWrapper)
    {
        this.bufferSource = bufferSource;
        this.vertexConsumerWrapper = vertexConsumerWrapper;
    }

    @Override
    public IVertexConsumer getBuffer(IRenderLayer renderLayer)
    {
        RenderType renderType = (RenderType) renderLayer.getNative();
        return vertexConsumerWrapper.apply(bufferSource.getBuffer(renderType));
    }

    @Override
    public Object getNative()
    {
        return bufferSource;
    }
}
