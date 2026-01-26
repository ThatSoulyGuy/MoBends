package goblinbob.mobends.forge.platform;

import com.mojang.blaze3d.vertex.VertexConsumer;
import goblinbob.mobends.api.rendering.IVertexConsumer;

/**
 * Forge 1.20.1 implementation of IVertexConsumer.
 * Translates 1.21.1-style API calls to 1.20.1-style.
 *
 * Key differences from 1.21.1:
 * - Uses vertex() instead of addVertex()
 * - Requires endVertex() call after each vertex
 * - Color takes separate RGBA components
 * - Uses overlayCoords() and uv2() instead of setOverlay()/setLight()
 */
public class ForgeVertexConsumer implements IVertexConsumer
{
    private final VertexConsumer consumer;

    // Temporary storage for vertex attributes until endVertex() is called
    private float posX, posY, posZ;
    private int colorR, colorG, colorB, colorA;
    private float texU, texV;
    private int overlayU, overlayV;
    private int lightU, lightV;
    private float normalX, normalY, normalZ;
    private boolean hasVertex = false;

    public ForgeVertexConsumer(VertexConsumer consumer)
    {
        this.consumer = consumer;
        resetVertex();
    }

    private void resetVertex()
    {
        colorR = 255;
        colorG = 255;
        colorB = 255;
        colorA = 255;
        texU = 0;
        texV = 0;
        overlayU = 0;
        overlayV = 10; // Default overlay (NO_OVERLAY)
        lightU = 0;
        lightV = 0;
        normalX = 0;
        normalY = 1;
        normalZ = 0;
        hasVertex = false;
    }

    @Override
    public IVertexConsumer addVertex(float x, float y, float z)
    {
        // If we have a pending vertex, flush it first
        if (hasVertex)
        {
            flushVertex();
        }

        posX = x;
        posY = y;
        posZ = z;
        hasVertex = true;
        return this;
    }

    @Override
    public IVertexConsumer setColor(int red, int green, int blue, int alpha)
    {
        colorR = red;
        colorG = green;
        colorB = blue;
        colorA = alpha;
        return this;
    }

    @Override
    public IVertexConsumer setUv(float u, float v)
    {
        texU = u;
        texV = v;
        return this;
    }

    @Override
    public IVertexConsumer setOverlay(int u, int v)
    {
        overlayU = u;
        overlayV = v;
        return this;
    }

    @Override
    public IVertexConsumer setOverlay(int overlay)
    {
        // Unpack overlay
        overlayU = overlay & 0xFFFF;
        overlayV = (overlay >> 16) & 0xFFFF;
        return this;
    }

    @Override
    public IVertexConsumer setLight(int u, int v)
    {
        lightU = u;
        lightV = v;
        return this;
    }

    @Override
    public IVertexConsumer setLight(int light)
    {
        // Unpack light
        lightU = light & 0xFFFF;
        lightV = (light >> 16) & 0xFFFF;
        return this;
    }

    @Override
    public IVertexConsumer setNormal(float x, float y, float z)
    {
        normalX = x;
        normalY = y;
        normalZ = z;

        // In 1.20.1, setNormal is typically the last call, so flush the vertex
        if (hasVertex)
        {
            flushVertex();
        }
        return this;
    }

    /**
     * Flushes the current vertex to the consumer using 1.20.1 API.
     */
    private void flushVertex()
    {
        // Forge 1.20.1 vertex format:
        // vertex(x, y, z).color(r, g, b, a).uv(u, v).overlayCoords(ou, ov).uv2(lu, lv).normal(nx, ny, nz).endVertex()
        consumer.vertex(posX, posY, posZ)
                .color(colorR, colorG, colorB, colorA)
                .uv(texU, texV)
                .overlayCoords(overlayU, overlayV)
                .uv2(lightU, lightV)
                .normal(normalX, normalY, normalZ)
                .endVertex();

        resetVertex();
    }

    @Override
    public Object getNative()
    {
        return consumer;
    }

    public VertexConsumer getConsumer()
    {
        return consumer;
    }
}
