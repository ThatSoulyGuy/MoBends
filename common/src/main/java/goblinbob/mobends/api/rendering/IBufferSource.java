package goblinbob.mobends.api.rendering;

public interface IBufferSource
{
    IVertexConsumer getBuffer(IRenderLayer renderLayer);

    Object getNative();
}
