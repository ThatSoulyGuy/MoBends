package goblinbob.mobends.api.rendering;

public interface IBufferBuilder
{
    IBufferBuilder addVertex(float x, float y, float z);

    IBufferBuilder setColor(float r, float g, float b, float a);

    IBufferBuilder setColor(int r, int g, int b, int a);

    IBufferBuilder setColorPacked(int packedColor);

    IBufferBuilder setUv(float u, float v);

    IBufferBuilder setOverlay(int overlay);

    IBufferBuilder setLight(int light);

    IBufferBuilder setNormal(float x, float y, float z);

    Object getNative();
}
