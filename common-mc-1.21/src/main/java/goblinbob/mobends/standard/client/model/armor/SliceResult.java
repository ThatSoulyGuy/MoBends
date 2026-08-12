package goblinbob.mobends.standard.client.model.armor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SliceResult
{
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
                a.overlayUV,
                a.lightmapUV
            );
        }

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

    public static SliceResult sliced(List<SlicedVertex> upperVertices,
                                     List<SlicedVertex> lowerVertices,
                                     List<SlicedVertex> edgeVertices)
    {
        return new SliceResult(upperVertices, lowerVertices, edgeVertices, true);
    }

    public static SliceResult entirelyAbove(List<SlicedVertex> vertices)
    {
        return new SliceResult(vertices, Collections.emptyList(), Collections.emptyList(), false);
    }

    public static SliceResult entirelyBelow(List<SlicedVertex> vertices)
    {
        return new SliceResult(Collections.emptyList(), vertices, Collections.emptyList(), false);
    }

    public static SliceResult empty()
    {
        return new SliceResult(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), false);
    }

    public List<SlicedVertex> getUpperVertices()
    {
        return upperVertices;
    }

    public List<SlicedVertex> getLowerVertices()
    {
        return lowerVertices;
    }

    public List<SlicedVertex> getEdgeVertices()
    {
        return edgeVertices;
    }

    public boolean wasSliced()
    {
        return wasSliced;
    }

    public boolean hasUpperGeometry()
    {
        return !upperVertices.isEmpty();
    }

    public boolean hasLowerGeometry()
    {
        return !lowerVertices.isEmpty();
    }
}
