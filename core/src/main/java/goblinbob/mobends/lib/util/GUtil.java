package goblinbob.mobends.lib.util;

import goblinbob.mobends.lib.math.Quaternion;
import goblinbob.mobends.lib.math.QuaternionUtils;
import goblinbob.mobends.lib.math.vector.IVec3f;

public class GUtil
{

    public static final float PI = (float) Math.PI;
    public static final float RAD_TO_DEG = 180.0F / PI;

    public static float clamp(float value, float min, float max)
    {
        return Math.min(Math.max(value, min), max);
    }

    public static float divideOr(float value, float divisor)
    {
        return divisor == 0.0F ? value : value / divisor;
    }

    public static double angleFromCoordinates(double x, double z)
    {
        return Math.atan2(x, z) / Math.PI * 180.0;
    }

    public static double getRadianDifference(double a, double b)
    {
        a = wrapRadians(a);
        b = wrapRadians(b);
        double d = Math.abs(a - b);
        d = d > Math.PI ? Math.PI * 2 - d : d;
        return d;
    }

    public static double wrapRadians(double a)
    {
        a = a % (Math.PI * 2);

        if (a >= Math.PI)
        {
            a -= Math.PI * 2;
        }
        else if (a < -Math.PI)
        {
            a += Math.PI * 2;
        }

        return a;
    }

    public static IVec3f translate(IVec3f vector, float x, float y, float z)
    {
        vector.add(x, y, z);
        return vector;
    }

    public static IVec3f scale(IVec3f vector, float x, float y, float z)
    {
        vector.scale(x, y, z);
        return vector;
    }

    public static void rotate(IVec3f[] points, Quaternion rotation)
    {
        for (IVec3f point : points)
        {
            QuaternionUtils.multiply(point, rotation, point);
        }
    }

    public static float lerp(float a, float b, float slide)
    {
        return a + (b - a) * slide;
    }

    public static IVec3f[] translate(IVec3f[] vectors, float x, float y, float z)
    {
        for (IVec3f vector : vectors)
        {
            translate(vector, x, y, z);
        }
        return vectors;
    }

    public static IVec3f[] scale(IVec3f[] vectors, float x, float y, float z)
    {
        for (IVec3f vector : vectors)
        {
            scale(vector, x, y, z);
        }
        return vectors;
    }

}
