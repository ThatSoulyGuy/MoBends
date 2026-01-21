package goblinbob.mobends.core.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;

public class CustomFontRenderer
{

    protected CustomFont font;
    protected int characterSpacing = 1;

    public void setFont(CustomFont font)
    {
        this.font = font;
    }

    protected void drawSymbol(CustomFont.Symbol symbol, BufferBuilder bufferBuilder, int x, int y)
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
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        BufferBuilder bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        int nextCharX = x;
        for (int i = 0; i < textToDraw.length(); ++i)
        {
            CustomFont.Symbol symbol = this.font.getSymbol(textToDraw.charAt(i));

            if (symbol == null)
                symbol = new CustomFont.Symbol(10, 10, 5, 5, 0, 0);

            this.drawSymbol(symbol, bufferBuilder, nextCharX, y);
            nextCharX += symbol.width + characterSpacing;
        }
        BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
        RenderSystem.disableBlend();
    }

    public void drawCenteredText(String textToDraw, int x, int y)
    {
        int width = this.getTextWidth(textToDraw);
        this.drawText(textToDraw, x - width / 2, y);
    }

}
