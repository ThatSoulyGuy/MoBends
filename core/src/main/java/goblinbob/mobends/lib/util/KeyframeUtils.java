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

    /**
     * Composes the tweened keyframe rotation ONTO the target, rather than adding its components.
     *
     * <p>This is what an additive layer needs, and it is not what
     * {@link #tweenOrientationAdditive} does. That method sums raw quaternion components, which is
     * only ever correct because the caller zeroed the target first — adding onto zero is the same
     * as setting. Skip the zeroing and the sums stop being unit quaternions: two layers writing
     * one bone leave {@code |q| = 2}, and the renderer scales geometry by {@code |q|^2}.
     *
     * <p>So the rotation is normalised, converted to axis-angle, and handed to
     * {@link SmoothOrientation#rotateInstant}, which multiplies it onto the existing orientation
     * the same way {@code ProceduralLayerState} composes its additive layers. Multiplying unit
     * quaternions yields a unit quaternion, so the result stays valid however many layers write.
     *
     * <p>{@code amount} scales the ANGLE, giving a partial rotation from identity — the natural
     * meaning for a cross-fade weight or a blend weight.
     */
    public static void tweenOrientationMultiplicative(SmoothOrientation target, float[] rotationA, float[] rotationB, float tween, float amount)
    {
        float x = rotationA[0] + (rotationB[0] - rotationA[0]) * tween;
        float y = rotationA[1] + (rotationB[1] - rotationA[1]) * tween;
        float z = rotationA[2] + (rotationB[2] - rotationA[2]) * tween;
        float w = rotationA[3] + (rotationB[3] - rotationA[3]) * tween;

        // Component-wise interpolation does not preserve unit length.
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

        // An identity rotation has no axis to speak of, and composing it is a no-op anyway.
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
