package goblinbob.mobends.standard.client.model.armor;

import java.util.ArrayList;
import java.util.List;

public class QuadSlicer
{
    public SliceResult slice(CapturedVertex[] vertices, JointPlane plane)
    {
        if (vertices == null || vertices.length != 4)
        {
            return SliceResult.empty();
        }

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

        List<SliceResult.SlicedVertex> upperVertices = new ArrayList<>();
        List<SliceResult.SlicedVertex> lowerVertices = new ArrayList<>();
        List<SliceResult.SlicedVertex> edgeVertices = new ArrayList<>();

        for (int i = 0; i < 4; i++)
        {
            int next = (i + 1) % 4;
            CapturedVertex v1 = vertices[i];
            CapturedVertex v2 = vertices[next];

            SliceResult.SlicedVertex sv1 = SliceResult.SlicedVertex.from(v1);

            if (above[i])
            {
                upperVertices.add(sv1);
            }
            else
            {
                lowerVertices.add(sv1);
            }

            if (above[i] != above[next])
            {
                float t = calculateIntersectionT(distances[i], distances[next]);
                SliceResult.SlicedVertex intersection = SliceResult.SlicedVertex.lerp(v1, v2, t);

                upperVertices.add(intersection);
                lowerVertices.add(intersection);
                edgeVertices.add(intersection);
            }
        }

        return SliceResult.sliced(upperVertices, lowerVertices, edgeVertices);
    }

    public List<SliceResult> sliceAll(List<CapturedVertex[]> quads, JointPlane plane)
    {
        List<SliceResult> results = new ArrayList<>(quads.size());
        for (CapturedVertex[] quad : quads)
        {
            results.add(slice(quad, plane));
        }
        return results;
    }

    private float calculateIntersectionT(float dist1, float dist2)
    {
        float denominator = dist1 - dist2;
        if (Math.abs(denominator) < 1e-6f)
        {
            return 0.5f;
        }
        return dist1 / denominator;
    }

    public List<SliceResult.SlicedVertex[]> triangulate(List<SliceResult.SlicedVertex> vertices)
    {
        List<SliceResult.SlicedVertex[]> triangles = new ArrayList<>();

        if (vertices.size() < 3)
        {
            return triangles;
        }

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
