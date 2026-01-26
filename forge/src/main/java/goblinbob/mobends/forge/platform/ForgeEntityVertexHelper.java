package goblinbob.mobends.forge.platform;

import com.mojang.blaze3d.vertex.VertexConsumer;
import goblinbob.mobends.api.rendering.IEntityVertexHelper;

/**
 * Forge 1.20.1 implementation of IEntityVertexHelper.
 *
 * MC 1.20.1 uses the full vertex method that takes all parameters at once,
 * similar to how vanilla ModelPart.Cube.compile() works.
 */
public class ForgeEntityVertexHelper implements IEntityVertexHelper
{
    @Override
    public void emitVertex(Object vertexConsumer, float x, float y, float z,
                           int color, float u, float v,
                           int overlay, int light,
                           float normalX, float normalY, float normalZ)
    {
        VertexConsumer consumer = (VertexConsumer) vertexConsumer;

        // Unpack ARGB color to floats (0.0-1.0 range)
        float alpha = ((color >> 24) & 0xFF) / 255.0f;
        float red = ((color >> 16) & 0xFF) / 255.0f;
        float green = ((color >> 8) & 0xFF) / 255.0f;
        float blue = (color & 0xFF) / 255.0f;

        // Use the full vertex method that MC 1.20.1 uses for entity rendering.
        // This matches how vanilla ModelPart.Cube.compile() emits vertices.
        consumer.vertex(x, y, z, red, green, blue, alpha, u, v, overlay, light, normalX, normalY, normalZ);
    }
}
