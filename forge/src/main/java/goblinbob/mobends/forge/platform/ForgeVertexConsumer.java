package goblinbob.mobends.forge.platform;

import com.mojang.blaze3d.vertex.VertexConsumer;
import goblinbob.mobends.api.rendering.IVertexConsumer;

public class ForgeVertexConsumer implements IVertexConsumer
{
    private final VertexConsumer consumer;

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
        overlayV = 10;
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

        if (hasVertex)
        {
            flushVertex();
        }
        return this;
    }

    private void flushVertex()
    {
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
