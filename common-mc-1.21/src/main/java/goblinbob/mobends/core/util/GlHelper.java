package goblinbob.mobends.core.util;

import com.mojang.blaze3d.vertex.PoseStack;
import goblinbob.mobends.lib.math.Quaternion;
import goblinbob.mobends.lib.math.matrix.IMat4x4d;
import goblinbob.mobends.lib.math.vector.IVec3dRead;
import goblinbob.mobends.lib.math.vector.IVec3fRead;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

/**
 * Helper class for rendering operations.
 * Updated for 1.20.1 to use PoseStack instead of GlStateManager.
 */
public class GlHelper
{
    /**
     * Apply a quaternion rotation to a PoseStack.
     * This is the primary method for 1.20.1 rendering.
     */
    public static void rotate(PoseStack poseStack, Quaternion quaternionIn)
    {
        if (quaternionIn == null) return;

        // Convert custom Quaternion to JOML Quaternionf
        Quaternionf jomlQuat = new Quaternionf(
                quaternionIn.x,
                quaternionIn.y,
                quaternionIn.z,
                quaternionIn.w
        );

        poseStack.mulPose(jomlQuat);
    }

    /**
     * Apply a JOML quaternion rotation to a PoseStack.
     */
    public static void rotate(PoseStack poseStack, Quaternionf quaternionIn)
    {
        if (quaternionIn == null) return;
        poseStack.mulPose(quaternionIn);
    }

    /**
     * Apply a custom matrix transformation to a PoseStack.
     */
    public static void transform(PoseStack poseStack, IMat4x4d matrixIn)
    {
        if (matrixIn == null) return;

        // Convert custom matrix to JOML Matrix4f
        Matrix4f jomlMatrix = new Matrix4f(
                (float) matrixIn.get(0, 0), (float) matrixIn.get(0, 1), (float) matrixIn.get(0, 2), (float) matrixIn.get(0, 3),
                (float) matrixIn.get(1, 0), (float) matrixIn.get(1, 1), (float) matrixIn.get(1, 2), (float) matrixIn.get(1, 3),
                (float) matrixIn.get(2, 0), (float) matrixIn.get(2, 1), (float) matrixIn.get(2, 2), (float) matrixIn.get(2, 3),
                (float) matrixIn.get(3, 0), (float) matrixIn.get(3, 1), (float) matrixIn.get(3, 2), (float) matrixIn.get(3, 3)
        );

        poseStack.last().pose().mul(jomlMatrix);
    }

    /**
     * Translate a PoseStack by a vector.
     */
    public static void translate(PoseStack poseStack, IVec3fRead vector)
    {
        if (vector == null) return;
        poseStack.translate(vector.getX(), vector.getY(), vector.getZ());
    }

    /**
     * Translate a PoseStack by a vector (double precision).
     */
    public static void translate(PoseStack poseStack, IVec3dRead vector)
    {
        if (vector == null) return;
        poseStack.translate(vector.getX(), vector.getY(), vector.getZ());
    }

    /**
     * Translate a PoseStack.
     */
    public static void translate(PoseStack poseStack, float x, float y, float z)
    {
        poseStack.translate(x, y, z);
    }

    /**
     * Scale a PoseStack uniformly.
     */
    public static void scale(PoseStack poseStack, float scale)
    {
        poseStack.scale(scale, scale, scale);
    }

    /**
     * Scale a PoseStack.
     */
    public static void scale(PoseStack poseStack, float x, float y, float z)
    {
        poseStack.scale(x, y, z);
    }

    /**
     * Create a JOML Quaternionf from axis-angle.
     */
    public static Quaternionf quaternionFromAxisAngle(float x, float y, float z, float angleDegrees)
    {
        float angleRadians = (float) Math.toRadians(angleDegrees);
        float halfAngle = angleRadians * 0.5f;
        float sinHalfAngle = (float) Math.sin(halfAngle);
        float cosHalfAngle = (float) Math.cos(halfAngle);

        // Normalize axis
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

    /**
     * Convert custom Quaternion to JOML Quaternionf.
     */
    public static Quaternionf toJomlQuaternion(Quaternion quat)
    {
        if (quat == null) return new Quaternionf();
        return new Quaternionf(quat.x, quat.y, quat.z, quat.w);
    }

    /**
     * Convert JOML Quaternionf to custom Quaternion.
     */
    public static Quaternion fromJomlQuaternion(Quaternionf quat)
    {
        if (quat == null) return new Quaternion();
        return new Quaternion(quat.x(), quat.y(), quat.z(), quat.w());
    }

    // ============================================
    // Legacy methods (deprecated, kept for compatibility)
    // ============================================

    /**
     * @deprecated Use rotate(PoseStack, Quaternion) instead for 1.20.1
     */
    @Deprecated
    public static void rotate(Quaternion quaternionIn)
    {
        // Legacy GlStateManager rotation no longer available in 1.20.1
        // This method is kept for compilation compatibility but does nothing
    }

    /**
     * @deprecated Use transform(PoseStack, IMat4x4d) instead for 1.20.1
     */
    @Deprecated
    public static void transform(IMat4x4d matrixIn)
    {
        // Legacy GlStateManager transformation no longer available in 1.20.1
        // This method is kept for compilation compatibility but does nothing
    }

    /**
     * @deprecated Immediate mode vertex submission is not used in 1.20.1
     */
    @Deprecated
    public static void vertex(IVec3fRead vector)
    {
        // Legacy GL11.glVertex3f no longer used in modern rendering
    }

    /**
     * @deprecated Immediate mode vertex submission is not used in 1.20.1
     */
    @Deprecated
    public static void vertex(IVec3dRead vector)
    {
        // Legacy GL11.glVertex3d no longer used in modern rendering
    }
}
