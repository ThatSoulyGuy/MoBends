package goblinbob.mobends.lib.math.vector;

public class VectorUtils
{

    public static void normalize(IVec3fRead v, IVec3f dest) throws IllegalArgumentException
    {
        float length = v.length();
        if (length == 0)
            throw new IllegalArgumentException("A zero vector cannot be normalized.");
        dest.set(v.getX() / length, v.getY() / length, v.getZ() / length);
    }

    public static void normalize(IVec3f vec) throws IllegalArgumentException
    {
        normalize(vec, vec);
    }

    public static float dot(IVec3fRead left, IVec3fRead right)
    {
        return left.getX() * right.getX() + left.getY() * right.getY() + left.getZ() * right.getZ();
    }

    public static IVec3fRead cross(IVec3fRead left, IVec3fRead right, IVec3f dest)
    {
        dest.set(
                left.getY() * right.getZ() - left.getZ() * right.getY(),
                right.getX() * left.getZ() - right.getZ() * left.getX(),
                left.getX() * right.getY() - left.getY() * right.getX()
        );

        return dest;
    }

    public static IVec3fRead cross(float lx, float ly, float lz, float rx, float ry, float rz, IVec3f dest)
    {
        dest.set(
                ly * rz - lz * ry,
                rx * lz - rz * lx,
                lx * ry - ly * rx
        );

        return dest;
    }

}
