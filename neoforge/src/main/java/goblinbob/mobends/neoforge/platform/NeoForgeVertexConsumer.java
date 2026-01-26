package goblinbob.mobends.neoforge.platform;

import com.mojang.blaze3d.vertex.VertexConsumer;
import goblinbob.mobends.api.rendering.IVertexConsumer;

/**
 * NeoForge 1.21.1 implementation of IVertexConsumer.
 * Uses the MC 1.21+ vertex API with addVertex() and no endVertex().
 */
public class NeoForgeVertexConsumer implements IVertexConsumer
{
    private final VertexConsumer consumer;

    public NeoForgeVertexConsumer(VertexConsumer consumer)
    {
        this.consumer = consumer;
    }

    @Override
    public IVertexConsumer addVertex(float x, float y, float z)
    {
        consumer.addVertex(x, y, z);
        return this;
    }

    @Override
    public IVertexConsumer setColor(int red, int green, int blue, int alpha)
    {
        consumer.setColor(red, green, blue, alpha);
        return this;
    }

    @Override
    public IVertexConsumer setUv(float u, float v)
    {
        consumer.setUv(u, v);
        return this;
    }

    @Override
    public IVertexConsumer setOverlay(int u, int v)
    {
        // Pack u,v into single int: u | (v << 16)
        consumer.setOverlay(u | (v << 16));
        return this;
    }

    @Override
    public IVertexConsumer setOverlay(int overlay)
    {
        consumer.setOverlay(overlay);
        return this;
    }

    @Override
    public IVertexConsumer setLight(int u, int v)
    {
        // Pack u,v into single int: u | (v << 16)
        consumer.setLight(u | (v << 16));
        return this;
    }

    @Override
    public IVertexConsumer setLight(int light)
    {
        consumer.setLight(light);
        return this;
    }

    @Override
    public IVertexConsumer setNormal(float x, float y, float z)
    {
        consumer.setNormal(x, y, z);
        return this;
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
