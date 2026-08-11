package goblinbob.mobends.forge.platform;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import goblinbob.mobends.api.rendering.DrawMode;
import goblinbob.mobends.api.rendering.IBufferBuilder;
import goblinbob.mobends.api.rendering.ITesselator;
import goblinbob.mobends.api.rendering.VertexFormatType;

public class ForgeTesselator implements ITesselator
{
    private final Tesselator tesselator;

    public ForgeTesselator()
    {
        this.tesselator = Tesselator.getInstance();
    }

    @Override
    public IBufferBuilder begin(DrawMode mode, VertexFormatType format)
    {
        VertexFormat.Mode mcMode = mapDrawMode(mode);
        VertexFormat mcFormat = mapVertexFormat(format);

        BufferBuilder builder = tesselator.getBuilder();
        builder.begin(mcMode, mcFormat);
        return new ForgeBufferBuilder(builder);
    }

    @Override
    public void endAndDraw(IBufferBuilder builder)
    {
        ForgeBufferBuilder forgeBuilder = (ForgeBufferBuilder) builder;

        forgeBuilder.finishVertex();

        BufferBuilder nativeBuilder = (BufferBuilder) builder.getNative();
        BufferUploader.drawWithShader(nativeBuilder.end());
    }

    @Override
    public Object getNative()
    {
        return tesselator;
    }

    private VertexFormat.Mode mapDrawMode(DrawMode mode)
    {
        return switch (mode)
        {
            case QUADS -> VertexFormat.Mode.QUADS;
            case TRIANGLES -> VertexFormat.Mode.TRIANGLES;
            case TRIANGLE_STRIP -> VertexFormat.Mode.TRIANGLE_STRIP;
            case TRIANGLE_FAN -> VertexFormat.Mode.TRIANGLE_FAN;
            case LINES -> VertexFormat.Mode.LINES;
            case LINE_STRIP -> VertexFormat.Mode.LINE_STRIP;
            case DEBUG_LINES -> VertexFormat.Mode.DEBUG_LINES;
            case DEBUG_LINE_STRIP -> VertexFormat.Mode.DEBUG_LINE_STRIP;
        };
    }

    private VertexFormat mapVertexFormat(VertexFormatType format)
    {
        return switch (format)
        {
            case POSITION -> DefaultVertexFormat.POSITION;
            case POSITION_COLOR -> DefaultVertexFormat.POSITION_COLOR;
            case POSITION_TEX -> DefaultVertexFormat.POSITION_TEX;
            case POSITION_TEX_COLOR -> DefaultVertexFormat.POSITION_TEX_COLOR;
            case POSITION_TEX_COLOR_NORMAL -> DefaultVertexFormat.BLOCK;
        };
    }
}
