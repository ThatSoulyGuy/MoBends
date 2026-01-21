package goblinbob.mobends.standard.client.model.armor;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Vector3f;

/**
 * Defines a plane at a joint that divides "upper" from "lower" segments.
 * Used for rigid bone assignment of armor vertices.
 */
@OnlyIn(Dist.CLIENT)
public class JointPlane
{
    private final Vector3f point;
    private final Vector3f normal;

    /**
     * Create a joint plane.
     * @param point The joint position (e.g., elbow/knee location)
     * @param normal Direction along the limb, pointing toward the "upper" segment
     */
    public JointPlane(Vector3f point, Vector3f normal)
    {
        this.point = new Vector3f(point);
        this.normal = new Vector3f(normal).normalize();
    }

    /**
     * Create a joint plane from coordinates.
     */
    public JointPlane(float px, float py, float pz, float nx, float ny, float nz)
    {
        this.point = new Vector3f(px, py, pz);
        this.normal = new Vector3f(nx, ny, nz).normalize();
    }

    /**
     * Test if a vertex position is above (on the normal side of) the plane.
     * @param vertex The vertex position to test
     * @return true if the vertex is above the plane (belongs to upper segment)
     */
    public boolean isAbovePlane(Vector3f vertex)
    {
        // Vector from plane point to vertex
        float toVertexX = vertex.x - point.x;
        float toVertexY = vertex.y - point.y;
        float toVertexZ = vertex.z - point.z;

        // Dot product with normal
        float dot = toVertexX * normal.x + toVertexY * normal.y + toVertexZ * normal.z;

        return dot > 0;
    }

    /**
     * Test if a vertex position is above the plane.
     */
    public boolean isAbovePlane(float x, float y, float z)
    {
        float toVertexX = x - point.x;
        float toVertexY = y - point.y;
        float toVertexZ = z - point.z;

        float dot = toVertexX * normal.x + toVertexY * normal.y + toVertexZ * normal.z;

        return dot > 0;
    }

    /**
     * Get the signed distance from a point to the plane.
     * Positive = above plane (normal side), negative = below plane.
     */
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

    /**
     * Calculate the intersection point between a line segment and this plane.
     * @param p1 Start point of the segment
     * @param p2 End point of the segment
     * @return The interpolation factor t where intersection occurs (0 = p1, 1 = p2),
     *         or -1 if no intersection (segment is parallel to plane)
     */
    public float intersectSegment(Vector3f p1, Vector3f p2)
    {
        return intersectSegment(p1.x, p1.y, p1.z, p2.x, p2.y, p2.z);
    }

    /**
     * Calculate the intersection point between a line segment and this plane.
     * @return The interpolation factor t where intersection occurs (0 = start, 1 = end),
     *         or -1 if no intersection (segment is parallel to plane)
     */
    public float intersectSegment(float x1, float y1, float z1, float x2, float y2, float z2)
    {
        // Direction of the segment
        float dirX = x2 - x1;
        float dirY = y2 - y1;
        float dirZ = z2 - z1;

        // Dot product of direction with plane normal
        float denom = dirX * normal.x + dirY * normal.y + dirZ * normal.z;

        // Check if segment is parallel to plane
        if (Math.abs(denom) < 1e-6f)
        {
            return -1.0f;
        }

        // Vector from segment start to plane point
        float toPlaneX = point.x - x1;
        float toPlaneY = point.y - y1;
        float toPlaneZ = point.z - z1;

        // Calculate t parameter
        float t = (toPlaneX * normal.x + toPlaneY * normal.y + toPlaneZ * normal.z) / denom;

        // Check if intersection is within segment bounds
        if (t < 0.0f || t > 1.0f)
        {
            return -1.0f;
        }

        return t;
    }

    /**
     * Get the intersection point between a line segment and this plane.
     * @param p1 Start point of the segment
     * @param p2 End point of the segment
     * @param dest Vector to store the result (or null to create new)
     * @return The intersection point, or null if no intersection
     */
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

    /**
     * Test if an edge crosses this plane.
     * @return true if the two endpoints are on opposite sides of the plane
     */
    public boolean crossesPlane(float x1, float y1, float z1, float x2, float y2, float z2)
    {
        float dist1 = signedDistance(x1, y1, z1);
        float dist2 = signedDistance(x2, y2, z2);
        return (dist1 > 0) != (dist2 > 0);
    }

    /**
     * Test if an edge crosses this plane.
     */
    public boolean crossesPlane(Vector3f p1, Vector3f p2)
    {
        return crossesPlane(p1.x, p1.y, p1.z, p2.x, p2.y, p2.z);
    }
}
