package goblinbob.mobends.core.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import goblinbob.mobends.api.rendering.DrawMode;
import goblinbob.mobends.api.rendering.IBufferBuilder;
import goblinbob.mobends.api.rendering.ITesselator;
import goblinbob.mobends.api.rendering.VertexFormatType;
import goblinbob.mobends.core.util.IColorRead;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class Mesh
{

	private VertexFormatType vertexFormat;
	private IBufferBuilder bufferBuilder;
	private DrawMode drawMode;

	public Mesh(VertexFormatType vertexFormat, int maxVertices)
	{
		this.vertexFormat = vertexFormat;
		// BufferBuilder is now created in beginDrawing()
		this.bufferBuilder = null;
	}

	/**
	 * Legacy constructor for backward compatibility.
	 * Uses POSITION_TEX_COLOR_NORMAL format.
	 */
	public Mesh(int maxVertices)
	{
		this(VertexFormatType.POSITION_TEX_COLOR_NORMAL, maxVertices);
	}

	public void beginDrawing(DrawMode mode)
	{
		this.drawMode = mode;
		ITesselator tesselator = ITesselator.getInstance();
		this.bufferBuilder = tesselator.begin(mode, this.vertexFormat);
	}

	/**
	 * Legacy beginDrawing for backward compatibility.
	 * Uses QUADS mode by default.
	 */
	public void beginDrawing()
	{
		this.beginDrawing(DrawMode.QUADS);
	}

	public void finishDrawing()
	{
		ITesselator tesselator = ITesselator.getInstance();
		tesselator.endAndDraw(this.bufferBuilder);
	}

	// Temporary vertex data for building
	private float pendingX, pendingY, pendingZ;
	private float pendingU, pendingV;
	private float pendingR = 1.0f, pendingG = 1.0f, pendingB = 1.0f, pendingA = 1.0f;
	private float pendingNX, pendingNY, pendingNZ;

	public Mesh pos(double x, double y, double z)
	{
		this.pendingX = (float) x;
		this.pendingY = (float) y;
		this.pendingZ = (float) z;
		return this;
	}

	public Mesh normal(float x, float y, float z)
	{
		this.pendingNX = x;
		this.pendingNY = y;
		this.pendingNZ = z;
		return this;
	}

	public Mesh tex(double u, double v)
	{
		this.pendingU = (float) u;
		this.pendingV = (float) v;
		return this;
	}

	public Mesh color(IColorRead color)
	{
		this.pendingR = color.getR();
		this.pendingG = color.getG();
		this.pendingB = color.getB();
		this.pendingA = color.getA();
		return this;
	}

	public void endVertex()
	{
		this.bufferBuilder.addVertex(pendingX, pendingY, pendingZ)
				.setUv(pendingU, pendingV)
				.setColor(pendingR, pendingG, pendingB, pendingA)
				.setNormal(pendingNX, pendingNY, pendingNZ);
		// Reset to defaults
		pendingR = pendingG = pendingB = 1.0f;
		pendingA = 1.0f;
		pendingNX = pendingNY = pendingNZ = 0.0f;
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

	public IBufferBuilder getBufferBuilder()
	{
		return this.bufferBuilder;
	}

}
