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

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.defaultBlendFunc();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferBuilder.vertex((double) left, (double) top + height, 0.0D).color(red, green, blue, alpha).endVertex();
        bufferBuilder.vertex((double) left + width, (double) top + height, 0.0D).color(red, green, blue, alpha).endVertex();
        bufferBuilder.vertex((double) left + width, (double) top, 0.0D).color(red, green, blue, alpha).endVertex();
        bufferBuilder.vertex((double) left, (double) top, 0.0D).color(red, green, blue, alpha).endVertex();
        BufferUploader.drawWithShader(bufferBuilder.end());
        RenderSystem.disableBlend();
    }

    public static void rectangle(float left, float top, float width, float height)
    {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionShader);
        RenderSystem.defaultBlendFunc();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        bufferBuilder.vertex((double) left, (double) top + height, 0.0D).endVertex();
        bufferBuilder.vertex((double) left + width, (double) top + height, 0.0D).endVertex();
        bufferBuilder.vertex((double) left + width, (double) top, 0.0D).endVertex();
        bufferBuilder.vertex((double) left, (double) top, 0.0D).endVertex();
        BufferUploader.drawWithShader(bufferBuilder.end());
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

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferBuilder.vertex((double) x, (double) (y + h), 0.0D).color(color0.getR(), color0.getG(), color0.getB(), color0.getA()).endVertex();
        bufferBuilder.vertex((double) (x + w), (double) (y + h), 0.0D).color(color1.getR(), color1.getG(), color1.getB(), color1.getA()).endVertex();
        bufferBuilder.vertex((double) (x + w), (double) y, 0.0D).color(color1.getR(), color1.getG(), color1.getB(), color1.getA()).endVertex();
        bufferBuilder.vertex((double) x, (double) y, 0.0D).color(color0.getR(), color0.getG(), color0.getB(), color0.getA()).endVertex();
        BufferUploader.drawWithShader(bufferBuilder.end());

        RenderSystem.disableBlend();
    }

    public static void rectangleVerticalGradient(float x, float y, float w, float h, IColorRead color0, IColorRead color1)
    {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferBuilder.vertex((double) (x + w), (double) y, 0.0D).color(color0.getR(), color0.getG(), color0.getB(), color0.getA()).endVertex();
        bufferBuilder.vertex((double) x, (double) y, 0.0D).color(color0.getR(), color0.getG(), color0.getB(), color0.getA()).endVertex();
        bufferBuilder.vertex((double) x, (double) (y + h), 0.0D).color(color1.getR(), color1.getG(), color1.getB(), color1.getA()).endVertex();
        bufferBuilder.vertex((double) (x + w), (double) (y + h), 0.0D).color(color1.getR(), color1.getG(), color1.getB(), color1.getA()).endVertex();
        BufferUploader.drawWithShader(bufferBuilder.end());

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

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.defaultBlendFunc();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferBuilder.vertex((double) x, (double) y + height, 0.0D).color(r0, g0, b0, a0).endVertex();
        bufferBuilder.vertex((double) x + width, (double) y + height, 0.0D).color(r1, g1, b1, a1).endVertex();
        bufferBuilder.vertex((double) x + width, (double) y, 0.0D).color(r1, g1, b1, a1).endVertex();
        bufferBuilder.vertex((double) x, (double) y, 0.0D).color(r0, g0, b0, a0).endVertex();
        BufferUploader.drawWithShader(bufferBuilder.end());
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

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.defaultBlendFunc();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferBuilder.vertex((double) x, (double) y + h, 0.0D).color(r1, g1, b1, a1).endVertex();
        bufferBuilder.vertex((double) x + w, (double) y + h, 0.0D).color(r1, g1, b1, a1).endVertex();
        bufferBuilder.vertex((double) x + w, (double) y, 0.0D).color(r0, g0, b0, a0).endVertex();
        bufferBuilder.vertex((double) x, (double) y, 0.0D).color(r0, g0, b0, a0).endVertex();
        BufferUploader.drawWithShader(bufferBuilder.end());
        RenderSystem.disableBlend();
    }

    public static void circle(double x, double y, double radius, int vertices)
    {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionShader);
        RenderSystem.defaultBlendFunc();
        bufferBuilder.begin(VertexFormat.Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION);

        for (int i = 0; i <= vertices; i++)
        {
            double angle = ((double) (i % vertices) / vertices) * Math.PI * 2;
            bufferBuilder.vertex((double) (x + Math.cos(angle) * radius), (double) (y + Math.sin(angle) * radius), 0.0D).endVertex();
        }

        BufferUploader.drawWithShader(bufferBuilder.end());
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

		Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionTexColorNormalShader);
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL);
        // NEG_X
        bufferBuilder.vertex(minX, maxY, minZ).uv(0, 0).color(r, g, b, a).normal(-1, 0, 0).endVertex();
        bufferBuilder.vertex(minX, minY, minZ).uv(0, 0).color(r, g, b, a).normal(-1, 0, 0).endVertex();
        bufferBuilder.vertex(minX, minY, maxZ).uv(0, 0).color(r, g, b, a).normal(-1, 0, 0).endVertex();
        bufferBuilder.vertex(minX, maxY, maxZ).uv(0, 0).color(r, g, b, a).normal(-1, 0, 0).endVertex();
        // POS_X
        bufferBuilder.vertex(maxX, maxY, maxZ).uv(0, 0).color(r, g, b, a).normal(1, 0, 0).endVertex();
        bufferBuilder.vertex(maxX, minY, maxZ).uv(0, 0).color(r, g, b, a).normal(1, 0, 0).endVertex();
        bufferBuilder.vertex(maxX, minY, minZ).uv(0, 0).color(r, g, b, a).normal(1, 0, 0).endVertex();
        bufferBuilder.vertex(maxX, maxY, minZ).uv(0, 0).color(r, g, b, a).normal(1, 0, 0).endVertex();
        // NEG_Z
        bufferBuilder.vertex(maxX, maxY, minZ).uv(0, 0).color(r, g, b, a).normal(0, 0, -1).endVertex();
        bufferBuilder.vertex(maxX, minY, minZ).uv(0, 0).color(r, g, b, a).normal(0, 0, -1).endVertex();
        bufferBuilder.vertex(minX, minY, minZ).uv(0, 0).color(r, g, b, a).normal(0, 0, -1).endVertex();
        bufferBuilder.vertex(minX, maxY, minZ).uv(0, 0).color(r, g, b, a).normal(0, 0, -1).endVertex();
        // POS_Z
        bufferBuilder.vertex(minX, maxY, maxZ).uv(0, 0).color(r, g, b, a).normal(0, 0, 1).endVertex();
        bufferBuilder.vertex(minX, minY, maxZ).uv(0, 0).color(r, g, b, a).normal(0, 0, 1).endVertex();
        bufferBuilder.vertex(maxX, minY, maxZ).uv(0, 0).color(r, g, b, a).normal(0, 0, 1).endVertex();
        bufferBuilder.vertex(maxX, maxY, maxZ).uv(0, 0).color(r, g, b, a).normal(0, 0, 1).endVertex();
        // NEG_Y
        bufferBuilder.vertex(minX, minY, maxZ).uv(0, 0).color(r, g, b, a).normal(0, -1, 0).endVertex();
        bufferBuilder.vertex(minX, minY, minZ).uv(0, 0).color(r, g, b, a).normal(0, -1, 0).endVertex();
        bufferBuilder.vertex(maxX, minY, minZ).uv(0, 0).color(r, g, b, a).normal(0, -1, 0).endVertex();
        bufferBuilder.vertex(maxX, minY, maxZ).uv(0, 0).color(r, g, b, a).normal(0, -1, 0).endVertex();
        // POS_Y
        bufferBuilder.vertex(maxX, maxY, maxZ).uv(0, 0).color(r, g, b, a).normal(0, 1, 0).endVertex();
        bufferBuilder.vertex(maxX, maxY, minZ).uv(0, 0).color(r, g, b, a).normal(0, 1, 0).endVertex();
        bufferBuilder.vertex(minX, maxY, minZ).uv(0, 0).color(r, g, b, a).normal(0, 1, 0).endVertex();
        bufferBuilder.vertex(minX, maxY, maxZ).uv(0, 0).color(r, g, b, a).normal(0, 1, 0).endVertex();
        BufferUploader.drawWithShader(bufferBuilder.end());
	}

	public static void texturedModalRect(int x, int y, int textureX, int textureY, int width, int height)
    {
        float f = 0.00390625F;
        float f1 = 0.00390625F;
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferBuilder.vertex((double)(x + 0), (double)(y + height), 0).uv((float)(textureX + 0) * 0.00390625F, (float)(textureY + height) * 0.00390625F).endVertex();
        bufferBuilder.vertex((double)(x + width), (double)(y + height), 0).uv((float)(textureX + width) * 0.00390625F, (float)(textureY + height) * 0.00390625F).endVertex();
        bufferBuilder.vertex((double)(x + width), (double)(y + 0), 0).uv((float)(textureX + width) * 0.00390625F, (float)(textureY + 0) * 0.00390625F).endVertex();
        bufferBuilder.vertex((double)(x + 0), (double)(y + 0), 0).uv((float)(textureX + 0) * 0.00390625F, (float)(textureY + 0) * 0.00390625F).endVertex();
        BufferUploader.drawWithShader(bufferBuilder.end());
        RenderSystem.disableBlend();
    }

	public static void texturedModalRect(int x, int y, int width, int height, int textureX, int textureY, int textureWidth, int textureHeight)
    {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferBuilder.vertex((double)(x + 0), (double)(y + height), 0).uv((float)(textureX + 0) * 0.00390625F, (float)(textureY + textureHeight) * 0.00390625F).endVertex();
        bufferBuilder.vertex((double)(x + width), (double)(y + height), 0).uv((float)(textureX + textureWidth) * 0.00390625F, (float)(textureY + textureHeight) * 0.00390625F).endVertex();
        bufferBuilder.vertex((double)(x + width), (double)(y + 0), 0).uv((float)(textureX + textureWidth) * 0.00390625F, (float)(textureY + 0) * 0.00390625F).endVertex();
        bufferBuilder.vertex((double)(x + 0), (double)(y + 0), 0).uv((float)(textureX + 0) * 0.00390625F, (float)(textureY + 0) * 0.00390625F).endVertex();
        BufferUploader.drawWithShader(bufferBuilder.end());
        RenderSystem.disableBlend();
    }

    public static void texturedRectangle(int x, int y, int width, int height, float textureX, float textureY, float textureWidth, float textureHeight)
    {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferBuilder.vertex((double) (x + 0), (double) (y + height), 0).uv(textureX, textureY + textureHeight).endVertex();
        bufferBuilder.vertex((double) (x + width), (double) (y + height), 0).uv(textureX + textureWidth, textureY + textureHeight).endVertex();
        bufferBuilder.vertex((double) (x + width), (double) (y + 0), 0).uv(textureX + textureWidth, textureY).endVertex();
        bufferBuilder.vertex((double) (x + 0), (double) (y + 0), 0).uv(textureX, textureY).endVertex();
        BufferUploader.drawWithShader(bufferBuilder.end());
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
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bufferBuilder.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);

        bufferBuilder.vertex(minX, minY, minZ).color(color.getR(), color.getG(), color.getB(), color.getA()).endVertex();
        bufferBuilder.vertex(maxX, maxY, maxZ).color(color.getR(), color.getG(), color.getB(), color.getA()).endVertex();

        BufferUploader.drawWithShader(bufferBuilder.end());
    }

}
