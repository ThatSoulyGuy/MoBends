package goblinbob.mobends.standard.client.model.armor.tier1;

import com.mojang.blaze3d.vertex.PoseStack;
import goblinbob.mobends.core.client.model.ModelPartTransform;
import goblinbob.mobends.lib.math.Quaternion;
import goblinbob.mobends.lib.math.SmoothOrientation;
import goblinbob.mobends.standard.client.model.armor.BoneRegion;
import goblinbob.mobends.standard.client.model.armor.BoneTransformProvider;
import goblinbob.mobends.standard.data.BipedEntityData;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import org.joml.Quaternionf;

public class TransformInjector
{
    private static final float SCALE = 1.0f / 16.0f;

    private final BoneTransformProvider transformProvider;

    public TransformInjector()
    {
        this.transformProvider = new BoneTransformProvider();
    }

    public TransformInjector(BoneTransformProvider transformProvider)
    {
        this.transformProvider = transformProvider;
    }

    public void injectTransforms(HumanoidModel<?> model, BipedEntityData<?> entityData)
    {
        if (model == null || entityData == null)
        {
            return;
        }

        injectPartTransform(model.head, entityData.head, true);
        injectPartTransform(model.body, entityData.body, false);
        injectPartTransform(model.leftArm, entityData.leftArm, true);
        injectPartTransform(model.rightArm, entityData.rightArm, true);
        injectPartTransform(model.leftLeg, entityData.leftLeg, true);
        injectPartTransform(model.rightLeg, entityData.rightLeg, true);
    }

    private void injectPartTransform(ModelPart part, ModelPartTransform transform, boolean applyRotation)
    {
        if (part == null || transform == null)
        {
            return;
        }

        if (applyRotation)
        {
            SmoothOrientation orientation = transform.rotation;
            if (orientation != null)
            {
                Quaternion q = orientation.getSmooth();
                if (q != null)
                {
                    float[] euler = quaternionToEuler(q);
                    part.xRot = euler[0];
                    part.yRot = euler[1];
                    part.zRot = euler[2];
                }
                else
                {
                    part.xRot = 0;
                    part.yRot = 0;
                    part.zRot = 0;
                }
            }
            else
            {
                part.xRot = 0;
                part.yRot = 0;
                part.zRot = 0;
            }
        }
        else
        {
            part.xRot = 0;
            part.yRot = 0;
            part.zRot = 0;
        }

        if (transform.offset.x != 0 || transform.offset.y != 0 || transform.offset.z != 0)
        {
            part.x += transform.offset.x;
            part.y += transform.offset.y;
            part.z += transform.offset.z;
        }
    }

    public void applyBodyTransform(PoseStack poseStack, BipedEntityData<?> entityData)
    {
        if (entityData == null || entityData.body == null)
        {
            return;
        }

        ModelPartTransform body = entityData.body;

        SmoothOrientation orientation = body.rotation;
        if (orientation != null)
        {
            Quaternion q = orientation.getSmooth();
            if (q != null && !q.isIdentity())
            {
                poseStack.mulPose(new Quaternionf((float) q.x, (float) q.y, (float) q.z, (float) q.w));
            }
        }
    }

    public Quaternionf getBoneRotation(BoneRegion region, BipedEntityData<?> entityData)
    {
        return transformProvider.getRotation(region, entityData);
    }

    public void applyBoneTransform(PoseStack poseStack, BoneRegion region, BipedEntityData<?> entityData)
    {
        transformProvider.applyBoneTransform(poseStack, region, entityData);
    }

    private float[] quaternionToEuler(Quaternion q)
    {
        float[] angles = new float[3];

        float qx = (float) q.x;
        float qy = (float) q.y;
        float qz = (float) q.z;
        float qw = (float) q.w;

        float sinr_cosp = 2 * (qw * qx + qy * qz);
        float cosr_cosp = 1 - 2 * (qx * qx + qy * qy);
        angles[0] = (float) Math.atan2(sinr_cosp, cosr_cosp);

        float sinp = 2 * (qw * qy - qz * qx);
        if (Math.abs(sinp) >= 1)
        {
            angles[1] = (float) Math.copySign(Math.PI / 2, sinp);
        }
        else
        {
            angles[1] = (float) Math.asin(sinp);
        }

        float siny_cosp = 2 * (qw * qz + qx * qy);
        float cosy_cosp = 1 - 2 * (qy * qy + qz * qz);
        angles[2] = (float) Math.atan2(siny_cosp, cosy_cosp);

        return angles;
    }

    public BoneTransformProvider getTransformProvider()
    {
        return transformProvider;
    }
}
