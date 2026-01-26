package goblinbob.mobends.core.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import goblinbob.mobends.api.platform.PlatformServices;
import goblinbob.mobends.api.rendering.DrawMode;
import goblinbob.mobends.api.rendering.IBufferBuilder;
import goblinbob.mobends.api.rendering.ITesselator;
import goblinbob.mobends.api.rendering.VertexFormatType;

public class CustomFontRenderer
{

    protected CustomFont font;
    protected int characterSpacing = 1;

    public void setFont(CustomFont font)
    {
        this.font = font;
    }

    protected void drawSymbol(CustomFont.Symbol symbol, IBufferBuilder bufferBuilder, int x, int y)
    {
        if (symbol == null)
            symbol = new CustomFont.Symbol(10, 10, 5, 5, 0, 0);

        x += symbol.offsetX;
        y += symbol.offsetY;
        int width = symbol.width;
        int height = symbol.height;
        float textureX = (float) symbol.u / this.font.atlasWidth;
        float textureY = (float) symbol.v / this.font.atlasHeight;
        float textureWidth = (float) width / this.font.atlasWidth;
        float textureHeight = (float) height / this.font.atlasHeight;

        bufferBuilder.addVertex((float) x, (float) (y), 0)
                .setUv(textureX, textureY + textureHeight);
        bufferBuilder.addVertex((float) (x + width), (float) (y), 0)
                .setUv(textureX + textureWidth, textureY + textureHeight);
        bufferBuilder.addVertex((float) (x + width), (float) (y - height), 0)
                .setUv(textureX + textureWidth, textureY);
        bufferBuilder.addVertex((float) x, (float) (y - height), 0)
                .setUv(textureX, textureY);
    }

    public int getTextWidth(String textToDraw)
    {
        int width = 0;
        for (int i = 0; i < textToDraw.length(); ++i)
        {
            CustomFont.Symbol symbol = this.font.getSymbol(textToDraw.charAt(i));

            if (symbol == null)
                width += 2;
            else
                width += symbol.width;

            if (i != textToDraw.length() - 1)
                width += characterSpacing;
        }
        return width;
    }

    public void drawText(String textToDraw, int x, int y)
    {
        if (this.font == null)
            return;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderTexture(0, this.font.resourceLocation);
        PlatformServices.get().setPositionTexShader();
        ITesselator tesselator = ITesselator.getInstance();
        IBufferBuilder bufferBuilder = tesselator.begin(DrawMode.QUADS, VertexFormatType.POSITION_TEX);
        int nextCharX = x;
        for (int i = 0; i < textToDraw.length(); ++i)
        {
            CustomFont.Symbol symbol = this.font.getSymbol(textToDraw.charAt(i));

            if (symbol == null)
                symbol = new CustomFont.Symbol(10, 10, 5, 5, 0, 0);

            this.drawSymbol(symbol, bufferBuilder, nextCharX, y);
            nextCharX += symbol.width + characterSpacing;
        }
        tesselator.endAndDraw(bufferBuilder);
        RenderSystem.disableBlend();
    }

    public void drawCenteredText(String textToDraw, int x, int y)
    {
        int width = this.getTextWidth(textToDraw);
        this.drawText(textToDraw, x - width / 2, y);
    }

}
