package goblinbob.mobends.neoforge.platform;

import com.mojang.blaze3d.vertex.BufferBuilder;
import goblinbob.mobends.api.rendering.IBufferBuilder;

/**
 * NeoForge implementation of IBufferBuilder.
 * Direct passthrough to MC 1.21.1's BufferBuilder API.
 */
public class NeoForgeBufferBuilder implements IBufferBuilder
{
    private final BufferBuilder builder;

    public NeoForgeBufferBuilder(BufferBuilder builder)
    {
        this.builder = builder;
    }

    @Override
    public IBufferBuilder addVertex(float x, float y, float z)
    {
        builder.addVertex(x, y, z);
        return this;
    }

    @Override
    public IBufferBuilder setColor(float r, float g, float b, float a)
    {
        builder.setColor(r, g, b, a);
        return this;
    }

    @Override
    public IBufferBuilder setColor(int r, int g, int b, int a)
    {
        builder.setColor(r, g, b, a);
        return this;
    }

    @Override
    public IBufferBuilder setColorPacked(int packedColor)
    {
        builder.setColor(packedColor);
        return this;
    }

    @Override
    public IBufferBuilder setUv(float u, float v)
    {
        builder.setUv(u, v);
        return this;
    }

    @Override
    public IBufferBuilder setOverlay(int overlay)
    {
        builder.setOverlay(overlay);
        return this;
    }

    @Override
    public IBufferBuilder setLight(int light)
    {
        builder.setLight(light);
        return this;
    }

    @Override
    public IBufferBuilder setNormal(float x, float y, float z)
    {
        builder.setNormal(x, y, z);
        return this;
    }

    @Override
    public Object getNative()
    {
        return builder;
    }
}
