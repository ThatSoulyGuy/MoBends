package goblinbob.mobends.standard.client.model.armor;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;

/**
 * A VertexConsumer that captures all vertex data instead of (or in addition to) rendering.
 * Used to intercept armor rendering and capture the vertices for bone assignment.
 *
 * In 1.21.1, the pattern is: addVertex(x,y,z).setColor().setUv().setOverlay().setLight().setNormal()
 * The vertex is complete when the next addVertex() is called or when getVertices() is called.
 */
@OnlyIn(Dist.CLIENT)
public class CapturingVertexConsumer implements VertexConsumer
{
    private final List<CapturedVertex> vertices = new ArrayList<>();

    // Current vertex being built
    private boolean hasCurrentVertex = false;
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
     * Flush the current vertex to the list if one is pending.
     */
    private void flushCurrentVertex()
    {
        if (hasCurrentVertex)
        {
            vertices.add(new CapturedVertex(
                    x, y, z,
                    red, green, blue, alpha,
                    u, v,
                    overlayUV, lightmapUV,
                    normalX, normalY, normalZ
            ));
            hasCurrentVertex = false;
        }
    }

    /**
     * Get all captured vertices.
     */
    public List<CapturedVertex> getVertices()
    {
        // Flush any pending vertex before returning
        flushCurrentVertex();
        return vertices;
    }

    /**
     * Clear captured vertices for reuse.
     */
    public void clear()
    {
        vertices.clear();
        hasCurrentVertex = false;
        // Reset state
        red = green = blue = 1.0f;
        alpha = 1.0f;
        u = v = 0.0f;
        overlayUV = 0;
        lightmapUV = 0;
        normalX = normalY = normalZ = 0.0f;
    }

    /**
     * Get the number of captured vertices.
     */
    public int getVertexCount()
    {
        // Include pending vertex in count
        return vertices.size() + (hasCurrentVertex ? 1 : 0);
    }

    @Override
    public VertexConsumer addVertex(float x, float y, float z)
    {
        // Flush the previous vertex first
        flushCurrentVertex();

        // Start a new vertex
        this.x = x;
        this.y = y;
        this.z = z;

        // Reset other attributes to defaults for this new vertex
        this.red = 1.0f;
        this.green = 1.0f;
        this.blue = 1.0f;
        this.alpha = 1.0f;
        this.u = 0.0f;
        this.v = 0.0f;
        this.overlayUV = 0;
        this.lightmapUV = 0;
        this.normalX = 0.0f;
        this.normalY = 0.0f;
        this.normalZ = 0.0f;

        this.hasCurrentVertex = true;

        return this;
    }

    @Override
    public VertexConsumer setColor(int packedColor)
    {
        this.alpha = ((packedColor >> 24) & 0xFF) / 255.0f;
        this.red = ((packedColor >> 16) & 0xFF) / 255.0f;
        this.green = ((packedColor >> 8) & 0xFF) / 255.0f;
        this.blue = (packedColor & 0xFF) / 255.0f;
        return this;
    }

    @Override
    public VertexConsumer setColor(int red, int green, int blue, int alpha)
    {
        this.red = red / 255.0f;
        this.green = green / 255.0f;
        this.blue = blue / 255.0f;
        this.alpha = alpha / 255.0f;
        return this;
    }

    @Override
    public VertexConsumer setUv(float u, float v)
    {
        this.u = u;
        this.v = v;
        return this;
    }

    @Override
    public VertexConsumer setOverlay(int overlay)
    {
        this.overlayUV = overlay;
        return this;
    }

    @Override
    public VertexConsumer setLight(int light)
    {
        this.lightmapUV = light;
        return this;
    }

    @Override
    public VertexConsumer setNormal(float x, float y, float z)
    {
        this.normalX = x;
        this.normalY = y;
        this.normalZ = z;
        return this;
    }

    @Override
    public VertexConsumer setUv1(int u, int v)
    {
        // This is the overlay UV (alternative UV coordinates for overlays)
        this.overlayUV = (v << 16) | (u & 0xFFFF);
        return this;
    }

    @Override
    public VertexConsumer setUv2(int u, int v)
    {
        // This is the lightmap UV (alternative UV coordinates)
        this.lightmapUV = (v << 16) | (u & 0xFFFF);
        return this;
    }
}
