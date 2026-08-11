package goblinbob.mobends.standard.client.model.armor;

import org.joml.Vector3f;

public class JointPlane
{
    private final Vector3f point;
    private final Vector3f normal;

    public JointPlane(Vector3f point, Vector3f normal)
    {
        this.point = new Vector3f(point);
        this.normal = new Vector3f(normal).normalize();
    }

    public JointPlane(float px, float py, float pz, float nx, float ny, float nz)
    {
        this.point = new Vector3f(px, py, pz);
        this.normal = new Vector3f(nx, ny, nz).normalize();
    }

    public boolean isAbovePlane(Vector3f vertex)
    {
        float toVertexX = vertex.x - point.x;
        float toVertexY = vertex.y - point.y;
        float toVertexZ = vertex.z - point.z;

        float dot = toVertexX * normal.x + toVertexY * normal.y + toVertexZ * normal.z;

        return dot > 0;
    }

    public boolean isAbovePlane(float x, float y, float z)
    {
        float toVertexX = x - point.x;
        float toVertexY = y - point.y;
        float toVertexZ = z - point.z;

        float dot = toVertexX * normal.x + toVertexY * normal.y + toVertexZ * normal.z;

        return dot > 0;
    }

    public float signedDistance(float x, float y, float z)
    {
        float toVertexX = x - point.x;
        float toVertexY = y - point.y;
        float toVertexZ = z - point.z;

        return toVertexX * normal.x + toVertexY * normal.y + toVertexZ * normal.z;
    }

    public Vector3f getPoint()
    {
        return point;
    }

    public Vector3f getNormal()
    {
        return normal;
    }

    public float intersectSegment(Vector3f p1, Vector3f p2)
    {
        return intersectSegment(p1.x, p1.y, p1.z, p2.x, p2.y, p2.z);
    }

    public float intersectSegment(float x1, float y1, float z1, float x2, float y2, float z2)
    {
        float dirX = x2 - x1;
        float dirY = y2 - y1;
        float dirZ = z2 - z1;

        float denom = dirX * normal.x + dirY * normal.y + dirZ * normal.z;

        if (Math.abs(denom) < 1e-6f)
        {
            return -1.0f;
        }

        float toPlaneX = point.x - x1;
        float toPlaneY = point.y - y1;
        float toPlaneZ = point.z - z1;

        float t = (toPlaneX * normal.x + toPlaneY * normal.y + toPlaneZ * normal.z) / denom;

        if (t < 0.0f || t > 1.0f)
        {
            return -1.0f;
        }

        return t;
    }

    public Vector3f getIntersectionPoint(Vector3f p1, Vector3f p2, Vector3f dest)
    {
        float t = intersectSegment(p1, p2);
        if (t < 0.0f)
        {
            return null;
        }

        if (dest == null)
        {
            dest = new Vector3f();
        }

        dest.x = p1.x + t * (p2.x - p1.x);
        dest.y = p1.y + t * (p2.y - p1.y);
        dest.z = p1.z + t * (p2.z - p1.z);

        return dest;
    }

    public boolean crossesPlane(float x1, float y1, float z1, float x2, float y2, float z2)
    {
        float dist1 = signedDistance(x1, y1, z1);
        float dist2 = signedDistance(x2, y2, z2);
        return (dist1 > 0) != (dist2 > 0);
    }

    public boolean crossesPlane(Vector3f p1, Vector3f p2)
    {
        return crossesPlane(p1.x, p1.y, p1.z, p2.x, p2.y, p2.z);
    }
}
