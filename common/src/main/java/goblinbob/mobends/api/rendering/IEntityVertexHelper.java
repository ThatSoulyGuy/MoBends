package goblinbob.mobends.api.rendering;

/**
 * Platform-agnostic entity vertex emission helper.
 *
 * VertexConsumer API:
 * - MC 1.21.1: addVertex(x,y,z).setColor(int).setUv(u,v).setOverlay(int).setLight(int).setNormal(x,y,z)
 * - MC 1.20.1: vertex(x,y,z).color(r,g,b,a).uv(u,v).overlayCoords(int).uv2(int).normal(x,y,z).endVertex()
 */
public interface IEntityVertexHelper
{
    /**
     * Emits an entity vertex with all attributes.
     *
     * @param vertexConsumer The native VertexConsumer
     * @param x Position X
     * @param y Position Y
     * @param z Position Z
     * @param color Packed ARGB color
     * @param u Texture U
     * @param v Texture V
     * @param overlay Packed overlay value
     * @param light Packed light value
     * @param normalX Normal X
     * @param normalY Normal Y
     * @param normalZ Normal Z
     */
    void emitVertex(Object vertexConsumer, float x, float y, float z,
                    int color, float u, float v,
                    int overlay, int light,
                    float normalX, float normalY, float normalZ);

    /**
     * Singleton holder for the platform-specific implementation.
     */
    class Holder
    {
        private static IEntityVertexHelper helper;

        public static void setHelper(IEntityVertexHelper helper)
        {
            Holder.helper = helper;
        }

        public static IEntityVertexHelper getHelper()
        {
            return helper;
        }
    }
}
