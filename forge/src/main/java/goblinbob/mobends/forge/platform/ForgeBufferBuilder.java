package goblinbob.mobends.forge.platform;

import com.mojang.blaze3d.vertex.BufferBuilder;
import goblinbob.mobends.api.rendering.IBufferBuilder;

/**
 * Forge 1.20.1 implementation of IBufferBuilder.
 * Translates 1.21.1-style API calls to 1.20.1 API.
 *
 * Key difference:
 * - MC 1.21.1: addVertex().setColor().setUv() (no endVertex)
 * - MC 1.20.1: vertex().color().uv().endVertex()
 *
 * This implementation tracks vertex state and calls endVertex() appropriately:
 * - When a new vertex is started (addVertex called again)
 * - When end() is called on the tesselator
 */
public class ForgeBufferBuilder implements IBufferBuilder
{
    private final BufferBuilder builder;
    private boolean vertexStarted = false;

    public ForgeBufferBuilder(BufferBuilder builder)
    {
        this.builder = builder;
    }

    @Override
    public IBufferBuilder addVertex(float x, float y, float z)
    {
        // End the previous vertex if one was in progress
        if (vertexStarted)
        {
            builder.endVertex();
        }

        // Start the new vertex (1.20.1 uses vertex() instead of addVertex())
        builder.vertex(x, y, z);
        vertexStarted = true;
        return this;
    }

    @Override
    public IBufferBuilder setColor(float r, float g, float b, float a)
    {
        builder.color(r, g, b, a);
        return this;
    }

    @Override
    public IBufferBuilder setColor(int r, int g, int b, int a)
    {
        builder.color(r, g, b, a);
        return this;
    }

    @Override
    public IBufferBuilder setColorPacked(int packedColor)
    {
        // Unpack ARGB color
        int a = (packedColor >> 24) & 0xFF;
        int r = (packedColor >> 16) & 0xFF;
        int g = (packedColor >> 8) & 0xFF;
        int b = packedColor & 0xFF;
        builder.color(r, g, b, a);
        return this;
    }

    @Override
    public IBufferBuilder setUv(float u, float v)
    {
        builder.uv(u, v);
        return this;
    }

    @Override
    public IBufferBuilder setOverlay(int overlay)
    {
        builder.overlayCoords(overlay);
        return this;
    }

    @Override
    public IBufferBuilder setLight(int light)
    {
        builder.uv2(light);
        return this;
    }

    @Override
    public IBufferBuilder setNormal(float x, float y, float z)
    {
        builder.normal(x, y, z);
        return this;
    }

    @Override
    public Object getNative()
    {
        return builder;
    }

    /**
     * Finalizes the current vertex if one is in progress.
     * Called by ForgeTesselator before ending the build.
     */
    void finishVertex()
    {
        if (vertexStarted)
        {
            builder.endVertex();
            vertexStarted = false;
        }
    }
}
