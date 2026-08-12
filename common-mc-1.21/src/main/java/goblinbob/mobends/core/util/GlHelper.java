package goblinbob.mobends.core.util;

import com.mojang.blaze3d.vertex.PoseStack;
import goblinbob.mobends.lib.math.Quaternion;
import goblinbob.mobends.lib.math.matrix.IMat4x4d;
import goblinbob.mobends.lib.math.vector.IVec3dRead;
import goblinbob.mobends.lib.math.vector.IVec3fRead;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

public class GlHelper
{
    public static void rotate(PoseStack poseStack, Quaternion quaternionIn)
    {
        if (quaternionIn == null) return;

        if (quaternionIn.lengthSquared() < 1.0E-6F) return;

        Quaternionf jomlQuat = new Quaternionf(
                quaternionIn.x,
                quaternionIn.y,
                quaternionIn.z,
                quaternionIn.w
        );

        poseStack.mulPose(jomlQuat);
    }

    public static void rotate(PoseStack poseStack, Quaternionf quaternionIn)
    {
        if (quaternionIn == null) return;
        if (quaternionIn.lengthSquared() < 1.0E-6F) return;
        poseStack.mulPose(quaternionIn);
    }

    public static void transform(PoseStack poseStack, IMat4x4d matrixIn)
    {
        if (matrixIn == null) return;

        Matrix4f jomlMatrix = new Matrix4f(
                (float) matrixIn.get(0, 0), (float) matrixIn.get(0, 1), (float) matrixIn.get(0, 2), (float) matrixIn.get(0, 3),
                (float) matrixIn.get(1, 0), (float) matrixIn.get(1, 1), (float) matrixIn.get(1, 2), (float) matrixIn.get(1, 3),
                (float) matrixIn.get(2, 0), (float) matrixIn.get(2, 1), (float) matrixIn.get(2, 2), (float) matrixIn.get(2, 3),
                (float) matrixIn.get(3, 0), (float) matrixIn.get(3, 1), (float) matrixIn.get(3, 2), (float) matrixIn.get(3, 3)
        );

        poseStack.last().pose().mul(jomlMatrix);
    }

    public static void translate(PoseStack poseStack, IVec3fRead vector)
    {
        if (vector == null) return;
        poseStack.translate(vector.getX(), vector.getY(), vector.getZ());
    }

    public static void translate(PoseStack poseStack, IVec3dRead vector)
    {
        if (vector == null) return;
        poseStack.translate(vector.getX(), vector.getY(), vector.getZ());
    }

    public static void translate(PoseStack poseStack, float x, float y, float z)
    {
        poseStack.translate(x, y, z);
    }

    public static void scale(PoseStack poseStack, float scale)
    {
        poseStack.scale(scale, scale, scale);
    }

    public static void scale(PoseStack poseStack, float x, float y, float z)
    {
        poseStack.scale(x, y, z);
    }

    public static Quaternionf quaternionFromAxisAngle(float x, float y, float z, float angleDegrees)
    {
        float angleRadians = (float) Math.toRadians(angleDegrees);
        float halfAngle = angleRadians * 0.5f;
        float sinHalfAngle = (float) Math.sin(halfAngle);
        float cosHalfAngle = (float) Math.cos(halfAngle);

        float length = (float) Math.sqrt(x * x + y * y + z * z);
        if (length > 0)
        {
            x /= length;
            y /= length;
            z /= length;
        }

        return new Quaternionf(
                x * sinHalfAngle,
                y * sinHalfAngle,
                z * sinHalfAngle,
                cosHalfAngle
        );
    }

    public static Quaternionf toJomlQuaternion(Quaternion quat)
    {
        if (quat == null) return new Quaternionf();
        return new Quaternionf(quat.x, quat.y, quat.z, quat.w);
    }

    public static Quaternion fromJomlQuaternion(Quaternionf quat)
    {
        if (quat == null) return new Quaternion();
        return new Quaternion(quat.x(), quat.y(), quat.z(), quat.w());
    }

    @Deprecated
    public static void rotate(Quaternion quaternionIn)
    {
    }

    @Deprecated
    public static void transform(IMat4x4d matrixIn)
    {
    }

    @Deprecated
    public static void vertex(IVec3fRead vector)
    {
    }

    @Deprecated
    public static void vertex(IVec3dRead vector)
    {
    }
}
