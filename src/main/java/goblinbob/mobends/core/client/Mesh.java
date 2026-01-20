package goblinbob.mobends.core.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import goblinbob.mobends.core.util.IColorRead;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class Mesh
{

	private VertexFormat vertexFormat;
	private BufferBuilder bufferBuilder;
	private VertexFormat.Mode drawMode;

	public Mesh(VertexFormat vertexFormat, int maxVertices)
	{
		this.vertexFormat = vertexFormat;
		this.bufferBuilder = Tesselator.getInstance().getBuilder();
	}

	/**
	 * Legacy constructor for backward compatibility.
	 * Uses POSITION_TEX_COLOR_NORMAL format.
	 */
	public Mesh(int maxVertices)
	{
		this(DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL, maxVertices);
	}

	public void beginDrawing(VertexFormat.Mode mode)
	{
		this.drawMode = mode;
		this.bufferBuilder.begin(mode, this.vertexFormat);
	}

	/**
	 * Legacy beginDrawing for backward compatibility.
	 * Uses QUADS mode by default.
	 */
	public void beginDrawing()
	{
		this.beginDrawing(VertexFormat.Mode.QUADS);
	}

	public void finishDrawing()
	{
		BufferUploader.drawWithShader(this.bufferBuilder.end());
	}

	public Mesh pos(double x, double y, double z)
	{
		this.bufferBuilder.vertex(x, y, z);
		return this;
	}

	public Mesh normal(float x, float y, float z)
	{
		this.bufferBuilder.normal(x, y, z);
		return this;
	}

	public Mesh tex(double u, double v)
	{
		this.bufferBuilder.uv((float) u, (float) v);
		return this;
	}

	public Mesh color(IColorRead color)
	{
		this.bufferBuilder.color(color.getR(), color.getG(), color.getB(), color.getA());
		return this;
	}

	public void endVertex()
	{
		this.bufferBuilder.endVertex();
	}

	public void display()
	{
		// In 1.20.1, drawing is done automatically via finishDrawing()
		// This method is kept for compatibility but mesh should use finishDrawing() instead
	}

	/**
	 * Render the mesh using MultiBufferSource.
	 * TODO: This needs proper implementation with stored vertex data.
	 * Currently a stub that does nothing since mesh data isn't stored after finishDrawing().
	 */
	public void render(PoseStack poseStack, MultiBufferSource bufferSource, ResourceLocation texture, int packedLight)
	{
		// TODO: Implement proper mesh rendering with stored vertex data
		// For now, this is a no-op as the mesh rendering system needs redesign for 1.20.1
		// The mesh data is consumed by finishDrawing() and not stored for later rendering.
	}

	public BufferBuilder getBufferBuilder()
	{
		return this.bufferBuilder;
	}

}
