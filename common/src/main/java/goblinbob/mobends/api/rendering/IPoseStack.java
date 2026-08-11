package goblinbob.mobends.api.rendering;

public interface IPoseStack
{
    void pushPose();

    void popPose();

    void translate(double x, double y, double z);

    void translate(float x, float y, float z);

    default void scale(float scale)
    {
        scale(scale, scale, scale);
    }

    void scale(float x, float y, float z);

    void rotateX(float angle);

    void rotateY(float angle);

    void rotateZ(float angle);

    void mulPoseQuaternion(float x, float y, float z, float w);

    void mulPoseMatrix(float[] matrix);

    void setIdentity();

    void getPose(float[] dest);

    void getNormal(float[] dest);

    Object getNative();

    boolean isEmpty();

    void clear();
}
