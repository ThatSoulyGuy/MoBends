package goblinbob.mobends.standard.client.model.armor;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;

/**
 * A VertexConsumer that captures all vertex data instead of (or in addition to) rendering.
 * Used to intercept armor rendering and capture the vertices for bone assignment.
 */
@OnlyIn(Dist.CLIENT)
public class CapturingVertexConsumer implements VertexConsumer
{
    private final List<CapturedVertex> vertices = new ArrayList<>();

    // Current vertex being built
    private float x, y, z;
    private float red = 1.0f, green = 1.0f, blue = 1.0f, alpha = 1.0f;
    private float u, v;
    private int overlayUV;
    private int lightmapUV;
    private float normalX, normalY, normalZ;

    public CapturingVertexConsumer()
    {
    }

    /**
     * Get all captured vertices.
     */
    public List<CapturedVertex> getVertices()
    {
        return vertices;
    }

    /**
     * Clear captured vertices for reuse.
     */
    public void clear()
    {
        vertices.clear();
    }

    /**
     * Get the number of captured vertices.
     */
    public int getVertexCount()
    {
        return vertices.size();
    }

    @Override
    public VertexConsumer vertex(double x, double y, double z)
    {
        this.x = (float) x;
        this.y = (float) y;
        this.z = (float) z;
        return this;
    }

    @Override
    public VertexConsumer color(int red, int green, int blue, int alpha)
    {
        this.red = red / 255.0f;
        this.green = green / 255.0f;
        this.blue = blue / 255.0f;
        this.alpha = alpha / 255.0f;
        return this;
    }

    @Override
    public VertexConsumer uv(float u, float v)
    {
        this.u = u;
        this.v = v;
        return this;
    }

    @Override
    public VertexConsumer overlayCoords(int u, int v)
    {
        this.overlayUV = u | (v << 16);
        return this;
    }

    @Override
    public VertexConsumer uv2(int u, int v)
    {
        this.lightmapUV = u | (v << 16);
        return this;
    }

    @Override
    public VertexConsumer normal(float x, float y, float z)
    {
        this.normalX = x;
        this.normalY = y;
        this.normalZ = z;
        return this;
    }

    @Override
    public void endVertex()
    {
        vertices.add(new CapturedVertex(
                x, y, z,
                red, green, blue, alpha,
                u, v,
                overlayUV, lightmapUV,
                normalX, normalY, normalZ
        ));

        // Reset for next vertex
        red = green = blue = alpha = 1.0f;
    }

    @Override
    public void defaultColor(int red, int green, int blue, int alpha)
    {
        // Default color hint - we track color per vertex
    }

    @Override
    public void unsetDefaultColor()
    {
        // Nothing to do
    }

    /**
     * The compact vertex method used by Minecraft 1.20.1.
     * This is the primary method called during model rendering.
     */
    @Override
    public void vertex(float x, float y, float z,
                       float red, float green, float blue, float alpha,
                       float u, float v,
                       int overlayUV, int lightmapUV,
                       float normalX, float normalY, float normalZ)
    {
        vertices.add(new CapturedVertex(
                x, y, z,
                red, green, blue, alpha,
                u, v,
                overlayUV, lightmapUV,
                normalX, normalY, normalZ
        ));
    }
}
