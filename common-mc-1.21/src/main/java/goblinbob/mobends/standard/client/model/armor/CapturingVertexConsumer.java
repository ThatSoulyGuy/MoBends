package goblinbob.mobends.standard.client.model.armor;

import com.mojang.blaze3d.vertex.VertexConsumer;

import java.util.ArrayList;
import java.util.List;

/**
 * A VertexConsumer that captures all vertex data instead of (or in addition to) rendering.
 * Used to intercept armor rendering and capture the vertices for bone assignment.
 *
 * This class supports both MC 1.20.1 and 1.21.1 APIs:
 * - 1.20.1: vertex().color().uv().overlayCoords().uv2().normal().endVertex()
 * - 1.21.1: addVertex().setColor().setUv().setOverlay().setLight().setNormal()
 *
 * Note: @Override annotations are omitted to allow compilation against either version.
 */
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

    // ===== MC 1.21.1 API =====

    // addVertex - MC 1.21.1 style (starts a new vertex)
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

    // setColor with packed int - MC 1.21.1 style
    public VertexConsumer setColor(int packedColor)
    {
        this.alpha = ((packedColor >> 24) & 0xFF) / 255.0f;
        this.red = ((packedColor >> 16) & 0xFF) / 255.0f;
        this.green = ((packedColor >> 8) & 0xFF) / 255.0f;
        this.blue = (packedColor & 0xFF) / 255.0f;
        return this;
    }

    // setColor with RGBA - MC 1.21.1 style
    public VertexConsumer setColor(int red, int green, int blue, int alpha)
    {
        this.red = red / 255.0f;
        this.green = green / 255.0f;
        this.blue = blue / 255.0f;
        this.alpha = alpha / 255.0f;
        return this;
    }

    // setUv - MC 1.21.1 style
    public VertexConsumer setUv(float u, float v)
    {
        this.u = u;
        this.v = v;
        return this;
    }

    // setOverlay - MC 1.21.1 style
    public VertexConsumer setOverlay(int overlay)
    {
        this.overlayUV = overlay;
        return this;
    }

    // setLight - MC 1.21.1 style
    public VertexConsumer setLight(int light)
    {
        this.lightmapUV = light;
        return this;
    }

    // setNormal - MC 1.21.1 style
    public VertexConsumer setNormal(float x, float y, float z)
    {
        this.normalX = x;
        this.normalY = y;
        this.normalZ = z;
        return this;
    }

    // setUv1 - MC 1.21.1 style
    public VertexConsumer setUv1(int u, int v)
    {
        // This is the overlay UV (alternative UV coordinates for overlays)
        this.overlayUV = (v << 16) | (u & 0xFFFF);
        return this;
    }

    // setUv2 - MC 1.21.1 style
    public VertexConsumer setUv2(int u, int v)
    {
        // This is the lightmap UV (alternative UV coordinates)
        this.lightmapUV = (v << 16) | (u & 0xFFFF);
        return this;
    }

    // ===== MC 1.20.1 API =====

    // vertex - MC 1.20.1 style (same as addVertex)
    public VertexConsumer vertex(double x, double y, double z)
    {
        return addVertex((float) x, (float) y, (float) z);
    }

    // color - MC 1.20.1 style (int version)
    public VertexConsumer color(int red, int green, int blue, int alpha)
    {
        return setColor(red, green, blue, alpha);
    }

    // color - MC 1.20.1 style (float version)
    public VertexConsumer color(float red, float green, float blue, float alpha)
    {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
        return this;
    }

    // uv - MC 1.20.1 style (same as setUv)
    public VertexConsumer uv(float u, float v)
    {
        return setUv(u, v);
    }

    // overlayCoords - MC 1.20.1 style
    public VertexConsumer overlayCoords(int u, int v)
    {
        return setUv1(u, v);
    }

    // overlayCoords - MC 1.20.1 style (packed)
    public VertexConsumer overlayCoords(int overlay)
    {
        return setOverlay(overlay);
    }

    // uv2 - MC 1.20.1 style (packed)
    public VertexConsumer uv2(int lightmap)
    {
        return setLight(lightmap);
    }

    // uv2 - MC 1.20.1 style (separate coords)
    public VertexConsumer uv2(int u, int v)
    {
        return setUv2(u, v);
    }

    // normal - MC 1.20.1 style (same as setNormal)
    public VertexConsumer normal(float x, float y, float z)
    {
        return setNormal(x, y, z);
    }

    // endVertex - MC 1.20.1 style (flushes current vertex)
    public void endVertex()
    {
        flushCurrentVertex();
    }

    // defaultColor - MC 1.20.1 (sets default color for subsequent vertices)
    public void defaultColor(int red, int green, int blue, int alpha)
    {
        // Not needed for capturing - just ignore
    }

    // unsetDefaultColor - MC 1.20.1 (clears default color)
    public void unsetDefaultColor()
    {
        // Not needed for capturing - just ignore
    }
}
