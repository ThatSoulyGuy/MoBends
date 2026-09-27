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

    public boolean isAbovePlane(float x, float y, float z)
    {
        return signedDistance(x, y, z) > 0;
    }

    public float signedDistance(float x, float y, float z)
    {
        float toVertexX = x - point.x;
        float toVertexY = y - point.y;
        float toVertexZ = z - point.z;

        return toVertexX * normal.x + toVertexY * normal.y + toVertexZ * normal.z;
    }
}
