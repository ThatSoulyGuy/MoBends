package goblinbob.mobends.standard.client.model.armor;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Result of slicing a quad at a joint plane.
 * Contains the upper and lower portions of the geometry with interpolated UVs.
 * Immutable after construction.
 */
@OnlyIn(Dist.CLIENT)
public class SliceResult
{
    /**
     * Represents a vertex in sliced geometry with all interpolated attributes.
     */
    public static class SlicedVertex
    {
        public final float x, y, z;
        public final float u, v;
        public final float normalX, normalY, normalZ;
        public final float red, green, blue, alpha;
        public final int overlayUV;
        public final int lightmapUV;

        public SlicedVertex(float x, float y, float z,
                           float u, float v,
                           float normalX, float normalY, float normalZ,
                           float red, float green, float blue, float alpha,
                           int overlayUV, int lightmapUV)
        {
            this.x = x;
            this.y = y;
            this.z = z;
            this.u = u;
            this.v = v;
            this.normalX = normalX;
            this.normalY = normalY;
            this.normalZ = normalZ;
            this.red = red;
            this.green = green;
            this.blue = blue;
            this.alpha = alpha;
            this.overlayUV = overlayUV;
            this.lightmapUV = lightmapUV;
        }

        /**
         * Create a SlicedVertex from a CapturedVertex.
         */
        public static SlicedVertex from(CapturedVertex captured)
        {
            return new SlicedVertex(
                captured.x, captured.y, captured.z,
                captured.u, captured.v,
                captured.normalX, captured.normalY, captured.normalZ,
                captured.red, captured.green, captured.blue, captured.alpha,
                captured.overlayUV, captured.lightmapUV
            );
        }

        /**
         * Linearly interpolate between two vertices.
         * @param a First vertex
         * @param b Second vertex
         * @param t Interpolation factor (0 = a, 1 = b)
         * @return Interpolated vertex
         */
        public static SlicedVertex lerp(SlicedVertex a, SlicedVertex b, float t)
        {
            float oneMinusT = 1.0f - t;
            return new SlicedVertex(
                a.x * oneMinusT + b.x * t,
                a.y * oneMinusT + b.y * t,
                a.z * oneMinusT + b.z * t,
                a.u * oneMinusT + b.u * t,
                a.v * oneMinusT + b.v * t,
                a.normalX * oneMinusT + b.normalX * t,
                a.normalY * oneMinusT + b.normalY * t,
                a.normalZ * oneMinusT + b.normalZ * t,
                a.red * oneMinusT + b.red * t,
                a.green * oneMinusT + b.green * t,
                a.blue * oneMinusT + b.blue * t,
                a.alpha * oneMinusT + b.alpha * t,
                a.overlayUV, // UV coords don't interpolate well, use first
                a.lightmapUV  // Lightmap doesn't interpolate well, use first
            );
        }

        /**
         * Interpolate between two CapturedVertices.
         */
        public static SlicedVertex lerp(CapturedVertex a, CapturedVertex b, float t)
        {
            return lerp(from(a), from(b), t);
        }
    }

    private final List<SlicedVertex> upperVertices;
    private final List<SlicedVertex> lowerVertices;
    private final List<SlicedVertex> edgeVertices;
    private final boolean wasSliced;

    private SliceResult(List<SlicedVertex> upperVertices,
                       List<SlicedVertex> lowerVertices,
                       List<SlicedVertex> edgeVertices,
                       boolean wasSliced)
    {
        this.upperVertices = Collections.unmodifiableList(new ArrayList<>(upperVertices));
        this.lowerVertices = Collections.unmodifiableList(new ArrayList<>(lowerVertices));
        this.edgeVertices = Collections.unmodifiableList(new ArrayList<>(edgeVertices));
        this.wasSliced = wasSliced;
    }

    /**
     * Create a slice result where the quad was actually sliced.
     */
    public static SliceResult sliced(List<SlicedVertex> upperVertices,
                                     List<SlicedVertex> lowerVertices,
                                     List<SlicedVertex> edgeVertices)
    {
        return new SliceResult(upperVertices, lowerVertices, edgeVertices, true);
    }

    /**
     * Create a result indicating the quad was entirely above the plane.
     */
    public static SliceResult entirelyAbove(List<SlicedVertex> vertices)
    {
        return new SliceResult(vertices, Collections.emptyList(), Collections.emptyList(), false);
    }

    /**
     * Create a result indicating the quad was entirely below the plane.
     */
    public static SliceResult entirelyBelow(List<SlicedVertex> vertices)
    {
        return new SliceResult(Collections.emptyList(), vertices, Collections.emptyList(), false);
    }

    /**
     * Create an empty slice result (for degenerate cases).
     */
    public static SliceResult empty()
    {
        return new SliceResult(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), false);
    }

    /**
     * Vertices belonging to the upper segment (above the joint plane).
     * These will be transformed with the upper bone.
     */
    public List<SlicedVertex> getUpperVertices()
    {
        return upperVertices;
    }

    /**
     * Vertices belonging to the lower segment (below the joint plane).
     * These will be transformed with the lower bone.
     */
    public List<SlicedVertex> getLowerVertices()
    {
        return lowerVertices;
    }

    /**
     * Vertices that lie exactly on the slice edge.
     * Can be used for seam handling if needed.
     */
    public List<SlicedVertex> getEdgeVertices()
    {
        return edgeVertices;
    }

    /**
     * Returns true if the quad was actually sliced (crossed the plane).
     * False if the quad was entirely on one side.
     */
    public boolean wasSliced()
    {
        return wasSliced;
    }

    /**
     * Returns true if there are any upper vertices.
     */
    public boolean hasUpperGeometry()
    {
        return !upperVertices.isEmpty();
    }

    /**
     * Returns true if there are any lower vertices.
     */
    public boolean hasLowerGeometry()
    {
        return !lowerVertices.isEmpty();
    }
}
