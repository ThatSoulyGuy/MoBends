package goblinbob.mobends.lib.util;

import goblinbob.mobends.lib.math.SmoothOrientation;
import goblinbob.mobends.lib.math.vector.IVec3f;
import goblinbob.mobends.lib.math.vector.SmoothVector3f;

public class KeyframeUtils
{

    public static void tweenVectorAdditive(SmoothVector3f target, float[] positionA, float[] positionB, float tween, float amount)
    {
        float x = positionA[0] + (positionB[0] - positionA[0]) * tween;
        float y = positionA[1] + (positionB[1] - positionA[1]) * tween;
        float z = positionA[2] + (positionB[2] - positionA[2]) * tween;

        target.add(x * amount, y * amount, z * amount);
        target.finish();
    }

    public static void tweenVectorAdditive(IVec3f target, float[] positionA, float[] positionB, float tween, float amount)
    {
        float x = positionA[0] + (positionB[0] - positionA[0]) * tween;
        float y = positionA[1] + (positionB[1] - positionA[1]) * tween;
        float z = positionA[2] + (positionB[2] - positionA[2]) * tween;

        target.add(x * amount, y * amount, z * amount);
    }

    public static void tweenVector(SmoothVector3f target, float[] positionA, float[] positionB, float tween)
    {
        float x = positionA[0] + (positionB[0] - positionA[0]) * tween;
        float y = positionA[1] + (positionB[1] - positionA[1]) * tween;
        float z = positionA[2] + (positionB[2] - positionA[2]) * tween;

        target.set(x, y, z);
    }

    public static void tweenVector(IVec3f target, float[] positionA, float[] positionB, float tween)
    {
        float x = positionA[0] + (positionB[0] - positionA[0]) * tween;
        float y = positionA[1] + (positionB[1] - positionA[1]) * tween;
        float z = positionA[2] + (positionB[2] - positionA[2]) * tween;

        target.set(x, y, z);
    }

    public static void tweenOrientationAdditive(SmoothOrientation target, float[] rotationA, float[] rotationB, float tween, float amount)
    {
        float x0 = rotationA[0];
        float y0 = rotationA[1];
        float z0 = rotationA[2];
        float w0 = rotationA[3];
        float x1 = rotationB[0];
        float y1 = rotationB[1];
        float z1 = rotationB[2];
        float w1 = rotationB[3];

        target.add((x0 + (x1 - x0) * tween) * amount,
                (y0 + (y1 - y0) * tween) * amount,
                (z0 + (z1 - z0) * tween) * amount,
                (w0 + (w1 - w0) * tween) * amount);
    }

    public static void tweenOrientationMultiplicative(SmoothOrientation target, float[] rotationA, float[] rotationB, float tween, float amount)
    {
        float x = rotationA[0] + (rotationB[0] - rotationA[0]) * tween;
        float y = rotationA[1] + (rotationB[1] - rotationA[1]) * tween;
        float z = rotationA[2] + (rotationB[2] - rotationA[2]) * tween;
        float w = rotationA[3] + (rotationB[3] - rotationA[3]) * tween;

        float length = (float) Math.sqrt(x * x + y * y + z * z + w * w);
        if (length < 1.0e-6F)
        {
            return;
        }
        x /= length;
        y /= length;
        z /= length;
        w /= length;

        float halfAngle = (float) Math.acos(Math.max(-1.0F, Math.min(1.0F, w)));
        float sinHalfAngle = (float) Math.sin(halfAngle);

        if (Math.abs(sinHalfAngle) < 1.0e-6F)
        {
            return;
        }

        float angleDegrees = (float) Math.toDegrees(2.0F * halfAngle) * amount;
        target.rotateInstant(angleDegrees, x / sinHalfAngle, y / sinHalfAngle, z / sinHalfAngle);
    }

    public static void tweenOrientation(SmoothOrientation target, float[] rotationA, float[] rotationB, float tween)
    {
        float x0 = rotationA[0];
        float y0 = rotationA[1];
        float z0 = rotationA[2];
        float w0 = rotationA[3];
        float x1 = rotationB[0];
        float y1 = rotationB[1];
        float z1 = rotationB[2];
        float w1 = rotationB[3];

        target.set(x0 + (x1 - x0) * tween,
                y0 + (y1 - y0) * tween,
                z0 + (z1 - z0) * tween,
                w0 + (w1 - w0) * tween);
    }

}
