package goblinbob.mobends.lib.math;

import goblinbob.mobends.lib.math.vector.IVec3f;
import goblinbob.mobends.lib.math.vector.Vec3f;
import goblinbob.mobends.lib.math.vector.VectorUtils;

public class QuaternionUtils
{

	public static void multiply(IVec3f vector, Quaternion quat, IVec3f dest)
	{
		Vec3f u = new Vec3f(-quat.x, quat.y, quat.z);
		Vec3f crossResult = new Vec3f();

		final float s = -quat.w;

	    final float x = vector.getX();
	    final float y = vector.getY();
	    final float z = vector.getZ();

	    final float dotUU = VectorUtils.dot(u, u);
	    final float dotUV = VectorUtils.dot(u, vector);
	    VectorUtils.cross(u, vector, crossResult);

	    dest.set(u);
	    dest.scale(2F * dotUV);
	    dest.add(x * (s*s - dotUU), y * (s*s - dotUU), z * (s*s - dotUU));
	    crossResult.scale(2 * s);
	    dest.add(crossResult);
	}

    public static float[] rotateVector(Quaternion q, float vx, float vy, float vz, float[] dest)
    {
        final float tx = 2.0F * (q.y * vz - q.z * vy);
        final float ty = 2.0F * (q.z * vx - q.x * vz);
        final float tz = 2.0F * (q.x * vy - q.y * vx);

        dest[0] = vx + q.w * tx + (q.y * tz - q.z * ty);
        dest[1] = vy + q.w * ty + (q.z * tx - q.x * tz);
        dest[2] = vz + q.w * tz + (q.x * ty - q.y * tx);
        return dest;
    }

}
