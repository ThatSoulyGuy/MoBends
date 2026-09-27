package goblinbob.mobends.standard.client.model.armor;

import java.util.ArrayList;
import java.util.Arrays;
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
            return SliceResult.entirelyBelow(Arrays.asList(vertices));
        }
        else if (aboveCount == 4)
        {
            return SliceResult.entirelyAbove(Arrays.asList(vertices));
        }

        List<CapturedVertex> upperVertices = new ArrayList<>();
        List<CapturedVertex> lowerVertices = new ArrayList<>();

        for (int i = 0; i < 4; i++)
        {
            int next = (i + 1) % 4;
            CapturedVertex v1 = vertices[i];
            CapturedVertex v2 = vertices[next];

            if (above[i])
            {
                upperVertices.add(v1);
            }
            else
            {
                lowerVertices.add(v1);
            }

            if (above[i] != above[next])
            {
                float t = calculateIntersectionT(distances[i], distances[next]);
                CapturedVertex intersection = lerp(v1, v2, t);

                upperVertices.add(intersection);
                lowerVertices.add(intersection);
            }
        }

        return SliceResult.sliced(upperVertices, lowerVertices);
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

    private static CapturedVertex lerp(CapturedVertex a, CapturedVertex b, float t)
    {
        float oneMinusT = 1.0f - t;
        return new CapturedVertex(
            a.x * oneMinusT + b.x * t,
            a.y * oneMinusT + b.y * t,
            a.z * oneMinusT + b.z * t,
            a.red * oneMinusT + b.red * t,
            a.green * oneMinusT + b.green * t,
            a.blue * oneMinusT + b.blue * t,
            a.alpha * oneMinusT + b.alpha * t,
            a.u * oneMinusT + b.u * t,
            a.v * oneMinusT + b.v * t,
            a.overlayUV,
            a.lightmapUV,
            a.normalX * oneMinusT + b.normalX * t,
            a.normalY * oneMinusT + b.normalY * t,
            a.normalZ * oneMinusT + b.normalZ * t
        );
    }
}
