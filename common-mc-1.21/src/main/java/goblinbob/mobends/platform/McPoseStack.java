package goblinbob.mobends.platform;

import com.mojang.blaze3d.vertex.PoseStack;
import goblinbob.mobends.api.rendering.IPoseStack;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

public class McPoseStack implements IPoseStack
{
    private final PoseStack poseStack;

    public McPoseStack(PoseStack poseStack)
    {
        this.poseStack = poseStack;
    }

    public McPoseStack()
    {
        this.poseStack = new PoseStack();
    }

    @Override
    public void pushPose()
    {
        poseStack.pushPose();
    }

    @Override
    public void popPose()
    {
        poseStack.popPose();
    }

    @Override
    public void translate(double x, double y, double z)
    {
        poseStack.translate(x, y, z);
    }

    @Override
    public void translate(float x, float y, float z)
    {
        poseStack.translate(x, y, z);
    }

    @Override
    public void scale(float x, float y, float z)
    {
        poseStack.scale(x, y, z);
    }

    @Override
    public void rotateX(float angle)
    {
        poseStack.mulPose(new Quaternionf().rotateX(angle));
    }

    @Override
    public void rotateY(float angle)
    {
        poseStack.mulPose(new Quaternionf().rotateY(angle));
    }

    @Override
    public void rotateZ(float angle)
    {
        poseStack.mulPose(new Quaternionf().rotateZ(angle));
    }

    @Override
    public void mulPoseQuaternion(float x, float y, float z, float w)
    {
        poseStack.mulPose(new Quaternionf(x, y, z, w));
    }

    @Override
    public void mulPoseMatrix(float[] matrix)
    {
        Matrix4f m = new Matrix4f();
        m.set(matrix);
        VersionAdapter.Holder.get().mulPoseMatrix(poseStack, m);
    }

    @Override
    public void setIdentity()
    {
        poseStack.setIdentity();
    }

    @Override
    public void getPose(float[] dest)
    {
        Matrix4f pose = poseStack.last().pose();
        pose.get(dest);
    }

    @Override
    public void getNormal(float[] dest)
    {
        Matrix3f normal = poseStack.last().normal();
        if (dest.length >= 9)
        {
            normal.get(dest);
        }
    }

    @Override
    public Object getNative()
    {
        return poseStack;
    }

    public PoseStack getPoseStack()
    {
        return poseStack;
    }

    @Override
    public boolean isEmpty()
    {
        return false;
    }

    @Override
    public void clear()
    {
        while (!poseStack.clear())
        {
        }
        poseStack.setIdentity();
    }
}
