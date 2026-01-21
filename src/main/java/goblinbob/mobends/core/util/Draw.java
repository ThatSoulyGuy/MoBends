package goblinbob.mobends.core.util;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.GameRenderer;
import org.lwjgl.opengl.GL11;

public class Draw
{

    public static void rectangle(int left, int top, int width, int height, int color)
    {
        float alpha = (float) (color >> 24 & 255) / 255.0F;
        float red = (float) (color >> 16 & 255) / 255.0F;
        float green = (float) (color >> 8 & 255) / 255.0F;
        float blue = (float) (color & 255) / 255.0F;

        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.defaultBlendFunc();
        BufferBuilder bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferBuilder.addVertex((float) left, (float) (top + height), 0.0F).setColor(red, green, blue, alpha);
        bufferBuilder.addVertex((float) (left + width), (float) (top + height), 0.0F).setColor(red, green, blue, alpha);
        bufferBuilder.addVertex((float) (left + width), (float) top, 0.0F).setColor(red, green, blue, alpha);
        bufferBuilder.addVertex((float) left, (float) top, 0.0F).setColor(red, green, blue, alpha);
        BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
        RenderSystem.disableBlend();
    }

    public static void rectangle(float left, float top, float width, float height)
    {
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionShader);
        RenderSystem.defaultBlendFunc();
        BufferBuilder bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        bufferBuilder.addVertex(left, top + height, 0.0F);
        bufferBuilder.addVertex(left + width, top + height, 0.0F);
        bufferBuilder.addVertex(left + width, top, 0.0F);
        bufferBuilder.addVertex(left, top, 0.0F);
        BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
        RenderSystem.disableBlend();
    }

    public static void rectangle(float x, float y, float w, float h, int color)
    {
        float a = (float) (color >> 24 & 255) / 255.0F;
        float r = (float) (color >> 16 & 255) / 255.0F;
        float g = (float) (color >> 8 & 255) / 255.0F;
        float b = (float) (color & 255) / 255.0F;

        RenderSystem.setShaderColor(r, g, b, a);
        rectangle(x, y, w, h);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static void rectangleHorizontalGradient(float x, float y, float w, float h, IColorRead color0, IColorRead color1)
    {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        BufferBuilder bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferBuilder.addVertex(x, y + h, 0.0F).setColor(color0.getR(), color0.getG(), color0.getB(), color0.getA());
        bufferBuilder.addVertex(x + w, y + h, 0.0F).setColor(color1.getR(), color1.getG(), color1.getB(), color1.getA());
        bufferBuilder.addVertex(x + w, y, 0.0F).setColor(color1.getR(), color1.getG(), color1.getB(), color1.getA());
        bufferBuilder.addVertex(x, y, 0.0F).setColor(color0.getR(), color0.getG(), color0.getB(), color0.getA());
        BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());

        RenderSystem.disableBlend();
    }

    public static void rectangleVerticalGradient(float x, float y, float w, float h, IColorRead color0, IColorRead color1)
    {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        BufferBuilder bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferBuilder.addVertex(x + w, y, 0.0F).setColor(color0.getR(), color0.getG(), color0.getB(), color0.getA());
        bufferBuilder.addVertex(x, y, 0.0F).setColor(color0.getR(), color0.getG(), color0.getB(), color0.getA());
        bufferBuilder.addVertex(x, y + h, 0.0F).setColor(color1.getR(), color1.getG(), color1.getB(), color1.getA());
        bufferBuilder.addVertex(x + w, y + h, 0.0F).setColor(color1.getR(), color1.getG(), color1.getB(), color1.getA());
        BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());

        RenderSystem.disableBlend();
    }

    public static void rectangleHorizontalGradient(float x, float y, float width, float height, int color0, int color1)
    {
        float a0 = (float) (color0 >> 24 & 255) / 255.0F;
        float r0 = (float) (color0 >> 16 & 255) / 255.0F;
        float g0 = (float) (color0 >> 8 & 255) / 255.0F;
        float b0 = (float) (color0 & 255) / 255.0F;
        float a1 = (float) (color1 >> 24 & 255) / 255.0F;
        float r1 = (float) (color1 >> 16 & 255) / 255.0F;
        float g1 = (float) (color1 >> 8 & 255) / 255.0F;
        float b1 = (float) (color1 & 255) / 255.0F;

        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.defaultBlendFunc();
        BufferBuilder bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferBuilder.addVertex(x, y + height, 0.0F).setColor(r0, g0, b0, a0);
        bufferBuilder.addVertex(x + width, y + height, 0.0F).setColor(r1, g1, b1, a1);
        bufferBuilder.addVertex(x + width, y, 0.0F).setColor(r1, g1, b1, a1);
        bufferBuilder.addVertex(x, y, 0.0F).setColor(r0, g0, b0, a0);
        BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
        RenderSystem.disableBlend();
    }

    public static void rectangleVerticalGradient(float x, float y, float w, float h, int color0, int color1)
    {
        RenderSystem.enableBlend();

        float a0 = (float) (color0 >> 24 & 255) / 255.0F;
        float r0 = (float) (color0 >> 16 & 255) / 255.0F;
        float g0 = (float) (color0 >> 8 & 255) / 255.0F;
        float b0 = (float) (color0 & 255) / 255.0F;
        float a1 = (float) (color1 >> 24 & 255) / 255.0F;
        float r1 = (float) (color1 >> 16 & 255) / 255.0F;
        float g1 = (float) (color1 >> 8 & 255) / 255.0F;
        float b1 = (float) (color1 & 255) / 255.0F;

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.defaultBlendFunc();
        BufferBuilder bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferBuilder.addVertex(x, y + h, 0.0F).setColor(r1, g1, b1, a1);
        bufferBuilder.addVertex(x + w, y + h, 0.0F).setColor(r1, g1, b1, a1);
        bufferBuilder.addVertex(x + w, y, 0.0F).setColor(r0, g0, b0, a0);
        bufferBuilder.addVertex(x, y, 0.0F).setColor(r0, g0, b0, a0);
        BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
        RenderSystem.disableBlend();
    }

    public static void circle(double x, double y, double radius, int vertices)
    {
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionShader);
        RenderSystem.defaultBlendFunc();
        BufferBuilder bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION);

        for (int i = 0; i <= vertices; i++)
        {
            double angle = ((double) (i % vertices) / vertices) * Math.PI * 2;
            bufferBuilder.addVertex((float) (x + Math.cos(angle) * radius), (float) (y + Math.sin(angle) * radius), 0.0F);
        }

        BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
        RenderSystem.disableBlend();
    }

    public static void circle(double x, double y, double radius)
    {
        circle(x, y, radius, 100);
    }

	public static void cube(double minX, double minY, double minZ, double maxX, double maxY, double maxZ, IColorRead color)
	{
		final float r = color.getR();
		final float g = color.getG();
		final float b = color.getB();
		final float a = color.getA();

        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        BufferBuilder bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        // NEG_X
        bufferBuilder.addVertex((float) minX, (float) maxY, (float) minZ).setUv(0, 0).setColor(r, g, b, a);
        bufferBuilder.addVertex((float) minX, (float) minY, (float) minZ).setUv(0, 0).setColor(r, g, b, a);
        bufferBuilder.addVertex((float) minX, (float) minY, (float) maxZ).setUv(0, 0).setColor(r, g, b, a);
        bufferBuilder.addVertex((float) minX, (float) maxY, (float) maxZ).setUv(0, 0).setColor(r, g, b, a);
        // POS_X
        bufferBuilder.addVertex((float) maxX, (float) maxY, (float) maxZ).setUv(0, 0).setColor(r, g, b, a);
        bufferBuilder.addVertex((float) maxX, (float) minY, (float) maxZ).setUv(0, 0).setColor(r, g, b, a);
        bufferBuilder.addVertex((float) maxX, (float) minY, (float) minZ).setUv(0, 0).setColor(r, g, b, a);
        bufferBuilder.addVertex((float) maxX, (float) maxY, (float) minZ).setUv(0, 0).setColor(r, g, b, a);
        // NEG_Z
        bufferBuilder.addVertex((float) maxX, (float) maxY, (float) minZ).setUv(0, 0).setColor(r, g, b, a);
        bufferBuilder.addVertex((float) maxX, (float) minY, (float) minZ).setUv(0, 0).setColor(r, g, b, a);
        bufferBuilder.addVertex((float) minX, (float) minY, (float) minZ).setUv(0, 0).setColor(r, g, b, a);
        bufferBuilder.addVertex((float) minX, (float) maxY, (float) minZ).setUv(0, 0).setColor(r, g, b, a);
        // POS_Z
        bufferBuilder.addVertex((float) minX, (float) maxY, (float) maxZ).setUv(0, 0).setColor(r, g, b, a);
        bufferBuilder.addVertex((float) minX, (float) minY, (float) maxZ).setUv(0, 0).setColor(r, g, b, a);
        bufferBuilder.addVertex((float) maxX, (float) minY, (float) maxZ).setUv(0, 0).setColor(r, g, b, a);
        bufferBuilder.addVertex((float) maxX, (float) maxY, (float) maxZ).setUv(0, 0).setColor(r, g, b, a);
        // NEG_Y
        bufferBuilder.addVertex((float) minX, (float) minY, (float) maxZ).setUv(0, 0).setColor(r, g, b, a);
        bufferBuilder.addVertex((float) minX, (float) minY, (float) minZ).setUv(0, 0).setColor(r, g, b, a);
        bufferBuilder.addVertex((float) maxX, (float) minY, (float) minZ).setUv(0, 0).setColor(r, g, b, a);
        bufferBuilder.addVertex((float) maxX, (float) minY, (float) maxZ).setUv(0, 0).setColor(r, g, b, a);
        // POS_Y
        bufferBuilder.addVertex((float) maxX, (float) maxY, (float) maxZ).setUv(0, 0).setColor(r, g, b, a);
        bufferBuilder.addVertex((float) maxX, (float) maxY, (float) minZ).setUv(0, 0).setColor(r, g, b, a);
        bufferBuilder.addVertex((float) minX, (float) maxY, (float) minZ).setUv(0, 0).setColor(r, g, b, a);
        bufferBuilder.addVertex((float) minX, (float) maxY, (float) maxZ).setUv(0, 0).setColor(r, g, b, a);
        BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
	}

	public static void texturedModalRect(int x, int y, int textureX, int textureY, int width, int height)
    {
        float f = 0.00390625F;
        float f1 = 0.00390625F;
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        BufferBuilder bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferBuilder.addVertex((float)(x + 0), (float)(y + height), 0).setUv((float)(textureX + 0) * 0.00390625F, (float)(textureY + height) * 0.00390625F);
        bufferBuilder.addVertex((float)(x + width), (float)(y + height), 0).setUv((float)(textureX + width) * 0.00390625F, (float)(textureY + height) * 0.00390625F);
        bufferBuilder.addVertex((float)(x + width), (float)(y + 0), 0).setUv((float)(textureX + width) * 0.00390625F, (float)(textureY + 0) * 0.00390625F);
        bufferBuilder.addVertex((float)(x + 0), (float)(y + 0), 0).setUv((float)(textureX + 0) * 0.00390625F, (float)(textureY + 0) * 0.00390625F);
        BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
        RenderSystem.disableBlend();
    }

	public static void texturedModalRect(int x, int y, int width, int height, int textureX, int textureY, int textureWidth, int textureHeight)
    {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        BufferBuilder bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferBuilder.addVertex((float)(x + 0), (float)(y + height), 0).setUv((float)(textureX + 0) * 0.00390625F, (float)(textureY + textureHeight) * 0.00390625F);
        bufferBuilder.addVertex((float)(x + width), (float)(y + height), 0).setUv((float)(textureX + textureWidth) * 0.00390625F, (float)(textureY + textureHeight) * 0.00390625F);
        bufferBuilder.addVertex((float)(x + width), (float)(y + 0), 0).setUv((float)(textureX + textureWidth) * 0.00390625F, (float)(textureY + 0) * 0.00390625F);
        bufferBuilder.addVertex((float)(x + 0), (float)(y + 0), 0).setUv((float)(textureX + 0) * 0.00390625F, (float)(textureY + 0) * 0.00390625F);
        BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
        RenderSystem.disableBlend();
    }

    public static void texturedRectangle(int x, int y, int width, int height, float textureX, float textureY, float textureWidth, float textureHeight)
    {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        BufferBuilder bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferBuilder.addVertex((float) (x + 0), (float) (y + height), 0).setUv(textureX, textureY + textureHeight);
        bufferBuilder.addVertex((float) (x + width), (float) (y + height), 0).setUv(textureX + textureWidth, textureY + textureHeight);
        bufferBuilder.addVertex((float) (x + width), (float) (y + 0), 0).setUv(textureX + textureWidth, textureY);
        bufferBuilder.addVertex((float) (x + 0), (float) (y + 0), 0).setUv(textureX, textureY);
        BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
        RenderSystem.disableBlend();
    }
    //Trimmed - HalfStretched

    public static void thsPuzzle(int x, int y, int left, int middle, int right, int height, int textureX, int textureY)
    {
        Draw.texturedModalRect(x, y, textureX, textureY, left, height);
        Draw.texturedModalRect(x + left, y, middle, height, textureX + left, textureY, 1, height);
        Draw.texturedModalRect(x + left + middle, y, textureX + left + 1, textureY, right, height);
    }

    public static void borderBox(int x, int y, int width, int height, int border, int textureX, int textureY)
    {
		/* Top-Left		*/ Draw.texturedModalRect(x-border, y-border, textureX, textureY, border, border);
		/* Top 			*/ Draw.texturedModalRect(x, y-border, width, border, textureX+border, textureY, 1, border);
		/* Top-Right 	*/ Draw.texturedModalRect(x+width, y-border, textureX+border+1, textureY, border, border);
		/* Right 		*/ Draw.texturedModalRect(x+width, y, border, height, textureX+border+1, textureY+border, border, 1);
		/* Bottom-Right */ Draw.texturedModalRect(x+width, y+height, textureX+border+1, textureY+border+1, border, border);
		/* Bottom		*/ Draw.texturedModalRect(x, y+height, width, border, textureX+border, textureY+border+1, 1, border);
		/* Bottom-Left	*/ Draw.texturedModalRect(x-border, y+height, textureX, textureY+border+1, border, border);
		/* Left 		*/ Draw.texturedModalRect(x-border, y, border, height, textureX, textureY+border, border, 1);
		/* Inside		*/ Draw.texturedModalRect(x, y, width, height, textureX+border, textureY+border, 1, 1);
	}

    public static void line(double minX, double minY, double minZ, double maxX, double maxY, double maxZ, IColorRead color)
    {
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);

        bufferBuilder.addVertex((float) minX, (float) minY, (float) minZ).setColor(color.getR(), color.getG(), color.getB(), color.getA());
        bufferBuilder.addVertex((float) maxX, (float) maxY, (float) maxZ).setColor(color.getR(), color.getG(), color.getB(), color.getA());

        BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
    }

}
