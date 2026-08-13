package goblinbob.mobends.api.rendering;

public interface IVertexConsumer
{
    IVertexConsumer addVertex(float x, float y, float z);

    IVertexConsumer setColor(int red, int green, int blue, int alpha);

    default IVertexConsumer setColor(int color)
    {
        return setColor(
                (color >> 16) & 0xFF,
                (color >> 8) & 0xFF,
                color & 0xFF,
                (color >> 24) & 0xFF
        );
    }

    IVertexConsumer setUv(float u, float v);

    IVertexConsumer setOverlay(int u, int v);

    IVertexConsumer setOverlay(int overlay);

    IVertexConsumer setLight(int u, int v);

    IVertexConsumer setLight(int light);

    IVertexConsumer setNormal(float x, float y, float z);

    Object getNative();
}
