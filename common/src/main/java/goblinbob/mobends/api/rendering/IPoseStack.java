package goblinbob.mobends.api.rendering;

/**
 * Platform-agnostic pose stack abstraction.
 * Wraps Minecraft's PoseStack/MatrixStack for transformation operations.
 */
public interface IPoseStack
{
    /**
     * Pushes a new pose onto the stack
     */
    void pushPose();

    /**
     * Pops the top pose from the stack
     */
    void popPose();

    /**
     * Translates the current pose
     * @param x X translation
     * @param y Y translation
     * @param z Z translation
     */
    void translate(double x, double y, double z);

    /**
     * Translates the current pose
     * @param x X translation
     * @param y Y translation
     * @param z Z translation
     */
    void translate(float x, float y, float z);

    /**
     * Scales the current pose uniformly
     * @param scale The scale factor
     */
    default void scale(float scale)
    {
        scale(scale, scale, scale);
    }

    /**
     * Scales the current pose
     * @param x X scale
     * @param y Y scale
     * @param z Z scale
     */
    void scale(float x, float y, float z);

    /**
     * Rotates the current pose around the X axis
     * @param angle The angle in radians
     */
    void rotateX(float angle);

    /**
     * Rotates the current pose around the Y axis
     * @param angle The angle in radians
     */
    void rotateY(float angle);

    /**
     * Rotates the current pose around the Z axis
     * @param angle The angle in radians
     */
    void rotateZ(float angle);

    /**
     * Rotates the current pose using quaternion components
     * @param x X component of the quaternion
     * @param y Y component of the quaternion
     * @param z Z component of the quaternion
     * @param w W component of the quaternion
     */
    void mulPoseQuaternion(float x, float y, float z, float w);

    /**
     * Multiplies the current pose by a 4x4 matrix
     * @param matrix The matrix values in column-major order (16 floats)
     */
    void mulPoseMatrix(float[] matrix);

    /**
     * Sets the identity matrix for the current pose
     */
    void setIdentity();

    /**
     * Gets the current pose matrix
     * @param dest Destination array for the matrix (16 floats, column-major)
     */
    void getPose(float[] dest);

    /**
     * Gets the current normal matrix
     * @param dest Destination array for the matrix (9 floats for 3x3, or 16 for padded 4x4)
     */
    void getNormal(float[] dest);

    /**
     * @return The native platform PoseStack object
     */
    Object getNative();

    /**
     * @return Whether the stack is empty (only identity pose)
     */
    boolean isEmpty();

    /**
     * Clears the stack to just the identity pose
     */
    void clear();
}
