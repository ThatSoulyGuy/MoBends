package goblinbob.mobends.neoforge.platform;

import com.mojang.blaze3d.vertex.VertexConsumer;
import goblinbob.mobends.api.rendering.IEntityVertexHelper;

public class NeoForgeEntityVertexHelper implements IEntityVertexHelper
{
    @Override
    public void emitVertex(Object vertexConsumer, float x, float y, float z,
                           int color, float u, float v,
                           int overlay, int light,
                           float normalX, float normalY, float normalZ)
    {
        VertexConsumer consumer = (VertexConsumer) vertexConsumer;
        consumer.addVertex(x, y, z)
                .setColor(color)
                .setUv(u, v)
                .setOverlay(overlay)
                .setLight(light)
                .setNormal(normalX, normalY, normalZ);
    }
}
