package goblinbob.mobends.api.rendering;

/**
 * Platform-agnostic model rendering helper.
 *
 * renderToBuffer:
 * - MC 1.21.1: renderToBuffer(PoseStack, VertexConsumer, int light, int overlay, int color)
 * - MC 1.20.1: renderToBuffer(PoseStack, VertexConsumer, int light, int overlay, float r, float g, float b, float a)
 *
 * getArmorFoilBuffer:
 * - MC 1.21.1: ItemRenderer.getArmorFoilBuffer(MultiBufferSource, RenderType, boolean hasFoil)
 * - MC 1.20.1: ItemRenderer.getArmorFoilBuffer(MultiBufferSource, RenderType, boolean isEntity, boolean hasFoil)
 */
public interface IModelRenderHelper
{
    /**
     * Renders a model to a buffer.
     *
     * @param model The model to render (EntityModel, HumanoidModel, etc.)
     * @param poseStack The PoseStack
     * @param vertexConsumer The VertexConsumer
     * @param packedLight Light value
     * @param packedOverlay Overlay value
     * @param color The color as packed ARGB int (0xFFFFFFFF for white)
     */
    void renderModelToBuffer(Object model, Object poseStack, Object vertexConsumer, int packedLight, int packedOverlay, int color);

    /**
     * Gets the armor foil buffer.
     *
     * @param bufferSource The MultiBufferSource
     * @param renderType The RenderType
     * @param hasFoil Whether the item has foil (enchantment glint)
     * @return The VertexConsumer
     */
    Object getArmorFoilBuffer(Object bufferSource, Object renderType, boolean hasFoil);

    /**
     * Singleton holder for the platform-specific implementation.
     */
    class Holder
    {
        private static IModelRenderHelper helper;

        public static void setHelper(IModelRenderHelper helper)
        {
            Holder.helper = helper;
        }

        public static IModelRenderHelper getHelper()
        {
            return helper;
        }
    }
}
