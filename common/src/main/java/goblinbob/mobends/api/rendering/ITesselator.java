package goblinbob.mobends.api.rendering;

import goblinbob.mobends.api.platform.PlatformServices;

public interface ITesselator
{
    IBufferBuilder begin(DrawMode mode, VertexFormatType format);

    void endAndDraw(IBufferBuilder builder);

    Object getNative();

    static ITesselator getInstance()
    {
        return PlatformServices.get().getTesselator();
    }
}
