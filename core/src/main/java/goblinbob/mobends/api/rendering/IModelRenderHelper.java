package goblinbob.mobends.api.rendering;

public interface IModelRenderHelper
{
    void renderModelToBuffer(Object model, Object poseStack, Object vertexConsumer, int packedLight, int packedOverlay, int color);

    Object getArmorFoilBuffer(Object bufferSource, Object renderType, boolean hasFoil);

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
