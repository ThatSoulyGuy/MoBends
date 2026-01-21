package goblinbob.mobends.standard.client.model.armor;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;

/**
 * Slices quads (4-vertex faces) at joint planes with proper UV interpolation.
 * Used for splitting armor geometry at elbow/knee joints.
 */
@OnlyIn(Dist.CLIENT)
public class QuadSlicer
{
    /**
     * Slice a quad at the given joint plane.
     * The quad is defined by 4 vertices in order (typically winding order for proper face culling).
     *
     * @param vertices The 4 captured vertices of the quad
     * @param plane The joint plane to slice at
     * @return SliceResult containing upper and lower portions with interpolated UVs
     */
    public SliceResult slice(CapturedVertex[] vertices, JointPlane plane)
    {
        if (vertices == null || vertices.length != 4)
        {
            return SliceResult.empty();
        }

        // Determine which vertices are above/below the plane
        boolean[] above = new boolean[4];
        float[] distances = new float[4];
        int aboveCount = 0;

        for (int i = 0; i < 4; i++)
        {
            distances[i] = plane.signedDistance(vertices[i].x, vertices[i].y, vertices[i].z);
            above[i] = distances[i] > 0;
            if (above[i])
            {
                aboveCount++;
            }
        }

        // If all vertices are on one side, no slicing needed
        if (aboveCount == 0)
        {
            List<SliceResult.SlicedVertex> lower = new ArrayList<>(4);
            for (CapturedVertex v : vertices)
            {
                lower.add(SliceResult.SlicedVertex.from(v));
            }
            return SliceResult.entirelyBelow(lower);
        }
        else if (aboveCount == 4)
        {
            List<SliceResult.SlicedVertex> upper = new ArrayList<>(4);
            for (CapturedVertex v : vertices)
            {
                upper.add(SliceResult.SlicedVertex.from(v));
            }
            return SliceResult.entirelyAbove(upper);
        }

        // Need to slice - find intersection points
        List<SliceResult.SlicedVertex> upperVertices = new ArrayList<>();
        List<SliceResult.SlicedVertex> lowerVertices = new ArrayList<>();
        List<SliceResult.SlicedVertex> edgeVertices = new ArrayList<>();

        // Process each edge of the quad
        for (int i = 0; i < 4; i++)
        {
            int next = (i + 1) % 4;
            CapturedVertex v1 = vertices[i];
            CapturedVertex v2 = vertices[next];

            SliceResult.SlicedVertex sv1 = SliceResult.SlicedVertex.from(v1);

            // Add current vertex to appropriate list
            if (above[i])
            {
                upperVertices.add(sv1);
            }
            else
            {
                lowerVertices.add(sv1);
            }

            // Check if edge crosses the plane
            if (above[i] != above[next])
            {
                // Calculate intersection point
                float t = calculateIntersectionT(distances[i], distances[next]);
                SliceResult.SlicedVertex intersection = SliceResult.SlicedVertex.lerp(v1, v2, t);

                // Add intersection to both upper and lower lists
                upperVertices.add(intersection);
                lowerVertices.add(intersection);
                edgeVertices.add(intersection);
            }
        }

        return SliceResult.sliced(upperVertices, lowerVertices, edgeVertices);
    }

    /**
     * Slice a list of quads at the given joint plane.
     *
     * @param quads List of quads, each represented as an array of 4 CapturedVertex
     * @param plane The joint plane to slice at
     * @return List of SliceResults, one per input quad
     */
    public List<SliceResult> sliceAll(List<CapturedVertex[]> quads, JointPlane plane)
    {
        List<SliceResult> results = new ArrayList<>(quads.size());
        for (CapturedVertex[] quad : quads)
        {
            results.add(slice(quad, plane));
        }
        return results;
    }

    /**
     * Calculate the interpolation factor for the intersection point.
     * Uses the signed distances from the plane to both endpoints.
     *
     * @param dist1 Signed distance from first point to plane
     * @param dist2 Signed distance from second point to plane
     * @return Interpolation factor t (0 = first point, 1 = second point)
     */
    private float calculateIntersectionT(float dist1, float dist2)
    {
        // Avoid division by zero for nearly-parallel edges
        float denominator = dist1 - dist2;
        if (Math.abs(denominator) < 1e-6f)
        {
            return 0.5f; // Fallback to midpoint
        }
        return dist1 / denominator;
    }

    /**
     * Triangulate a polygon (list of vertices) into triangles.
     * Uses simple fan triangulation which works for convex polygons.
     *
     * @param vertices List of vertices forming a convex polygon
     * @return List of triangles, each as an array of 3 SlicedVertex
     */
    public List<SliceResult.SlicedVertex[]> triangulate(List<SliceResult.SlicedVertex> vertices)
    {
        List<SliceResult.SlicedVertex[]> triangles = new ArrayList<>();

        if (vertices.size() < 3)
        {
            return triangles;
        }

        // Fan triangulation from first vertex
        SliceResult.SlicedVertex pivot = vertices.get(0);
        for (int i = 1; i < vertices.size() - 1; i++)
        {
            triangles.add(new SliceResult.SlicedVertex[] {
                pivot,
                vertices.get(i),
                vertices.get(i + 1)
            });
        }

        return triangles;
    }
}
