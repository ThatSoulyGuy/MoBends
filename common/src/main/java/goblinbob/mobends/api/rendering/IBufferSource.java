package goblinbob.mobends.api.rendering;

/**
 * Platform-agnostic buffer source abstraction.
 * Wraps Minecraft's MultiBufferSource for obtaining vertex consumers.
 */
public interface IBufferSource
{
    /**
     * Gets a vertex consumer for the specified render layer
     * @param renderLayer The render layer
     * @return The vertex consumer
     */
    IVertexConsumer getBuffer(IRenderLayer renderLayer);

    /**
     * @return The native platform MultiBufferSource object
     */
    Object getNative();
}
