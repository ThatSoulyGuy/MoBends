package goblinbob.mobends.forge.platform;

import com.mojang.blaze3d.vertex.VertexConsumer;
import goblinbob.mobends.api.rendering.IEntityVertexHelper;

public class ForgeEntityVertexHelper implements IEntityVertexHelper
{
    @Override
    public void emitVertex(Object vertexConsumer, float x, float y, float z,
                           int color, float u, float v,
                           int overlay, int light,
                           float normalX, float normalY, float normalZ)
    {
        VertexConsumer consumer = (VertexConsumer) vertexConsumer;

        float alpha = ((color >> 24) & 0xFF) / 255.0f;
        float red = ((color >> 16) & 0xFF) / 255.0f;
        float green = ((color >> 8) & 0xFF) / 255.0f;
        float blue = (color & 0xFF) / 255.0f;

        consumer.vertex(x, y, z, red, green, blue, alpha, u, v, overlay, light, normalX, normalY, normalZ);
    }
}
