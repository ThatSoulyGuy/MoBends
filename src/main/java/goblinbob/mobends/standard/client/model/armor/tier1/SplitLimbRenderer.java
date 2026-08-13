package goblinbob.mobends.standard.client.model.armor.tier1;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import goblinbob.mobends.core.client.model.ModelPartTransform;
import goblinbob.mobends.lib.math.Quaternion;
import goblinbob.mobends.lib.math.SmoothOrientation;
import goblinbob.mobends.standard.client.model.armor.BoneRegion;
import goblinbob.mobends.standard.client.model.armor.JointDefinitions;
import goblinbob.mobends.standard.client.model.armor.JointPlane;
import goblinbob.mobends.standard.data.BipedEntityData;
import net.minecraft.client.model.geom.ModelPart;
import org.joml.Quaternionf;

public class SplitLimbRenderer
{
    private static final float SCALE = 1.0f / 16.0f;

    public void renderSplitArm(
            PoseStack poseStack,
            VertexConsumer vertexConsumer,
            ModelPart armPart,
            BipedEntityData<?> entityData,
            boolean isLeft,
            int packedLight,
            int packedOverlay)
    {
        if (armPart == null || entityData == null)
        {
            return;
        }

        ModelPartTransform upperArm = isLeft ? entityData.leftArm : entityData.rightArm;
        ModelPartTransform foreArm = isLeft ? entityData.leftForeArm : entityData.rightForeArm;

        if (upperArm == null || foreArm == null)
        {
            renderWholeWithTransform(poseStack, vertexConsumer, armPart, upperArm, packedLight, packedOverlay);
            return;
        }

        JointPlane elbowPlane = JointDefinitions.getElbow(isLeft);

        renderUpperPortion(poseStack, vertexConsumer, armPart, upperArm, elbowPlane, packedLight, packedOverlay);

        renderLowerPortion(poseStack, vertexConsumer, armPart, upperArm, foreArm, elbowPlane, packedLight, packedOverlay);
    }

    public void renderSplitLeg(
            PoseStack poseStack,
            VertexConsumer vertexConsumer,
            ModelPart legPart,
            BipedEntityData<?> entityData,
            boolean isLeft,
            int packedLight,
            int packedOverlay)
    {
        if (legPart == null || entityData == null)
        {
            return;
        }

        ModelPartTransform upperLeg = isLeft ? entityData.leftLeg : entityData.rightLeg;
        ModelPartTransform lowerLeg = isLeft ? entityData.leftForeLeg : entityData.rightForeLeg;

        if (upperLeg == null || lowerLeg == null)
        {
            renderWholeWithTransform(poseStack, vertexConsumer, legPart, upperLeg, packedLight, packedOverlay);
            return;
        }

        JointPlane kneePlane = JointDefinitions.getKnee(isLeft);

        renderUpperPortion(poseStack, vertexConsumer, legPart, upperLeg, kneePlane, packedLight, packedOverlay);

        renderLowerPortion(poseStack, vertexConsumer, legPart, upperLeg, lowerLeg, kneePlane, packedLight, packedOverlay);
    }

    private void renderUpperPortion(
            PoseStack poseStack,
            VertexConsumer vertexConsumer,
            ModelPart limbPart,
            ModelPartTransform upperTransform,
            JointPlane jointPlane,
            int packedLight,
            int packedOverlay)
    {
        poseStack.pushPose();

        applyTransform(poseStack, upperTransform);

        limbPart.render(poseStack, vertexConsumer, packedLight, packedOverlay);

        poseStack.popPose();
    }

    private void renderLowerPortion(
            PoseStack poseStack,
            VertexConsumer vertexConsumer,
            ModelPart limbPart,
            ModelPartTransform upperTransform,
            ModelPartTransform lowerTransform,
            JointPlane jointPlane,
            int packedLight,
            int packedOverlay)
    {
        poseStack.pushPose();

        applyTransform(poseStack, upperTransform);

        applyTransform(poseStack, lowerTransform);

        poseStack.popPose();
    }

    private void renderWholeWithTransform(
            PoseStack poseStack,
            VertexConsumer vertexConsumer,
            ModelPart part,
            ModelPartTransform transform,
            int packedLight,
            int packedOverlay)
    {
        poseStack.pushPose();

        if (transform != null)
        {
            applyTransform(poseStack, transform);
        }

        part.render(poseStack, vertexConsumer, packedLight, packedOverlay);

        poseStack.popPose();
    }

    private void applyTransform(PoseStack poseStack, ModelPartTransform transform)
    {
        if (transform == null)
        {
            return;
        }

        if (transform.position.x != 0 || transform.position.y != 0 || transform.position.z != 0)
        {
            poseStack.translate(
                    transform.position.x * SCALE * transform.offsetScale,
                    transform.position.y * SCALE * transform.offsetScale,
                    transform.position.z * SCALE * transform.offsetScale
            );
        }

        if (transform.offset.x != 0 || transform.offset.y != 0 || transform.offset.z != 0)
        {
            poseStack.translate(
                    transform.offset.x * SCALE * transform.offsetScale,
                    transform.offset.y * SCALE * transform.offsetScale,
                    transform.offset.z * SCALE * transform.offsetScale
            );
        }

        SmoothOrientation orientation = transform.rotation;
        if (orientation != null)
        {
            Quaternion q = orientation.getSmooth();
            if (q != null && !q.isIdentity())
            {
                poseStack.mulPose(new Quaternionf((float) q.x, (float) q.y, (float) q.z, (float) q.w));
            }
        }

        if (transform.scale.x != 1 || transform.scale.y != 1 || transform.scale.z != 1)
        {
            poseStack.scale(transform.scale.x, transform.scale.y, transform.scale.z);
        }
    }

    public boolean needsSplitRendering(ModelPartTransform upperTransform, ModelPartTransform lowerTransform)
    {
        if (upperTransform == null || lowerTransform == null)
        {
            return false;
        }

        SmoothOrientation lowerOrientation = lowerTransform.rotation;
        if (lowerOrientation != null)
        {
            Quaternion q = lowerOrientation.getSmooth();
            if (q != null && !q.isIdentity())
            {
                return true;
            }
        }

        return false;
    }

    public BoneRegion getLimbRegion(boolean isArm, boolean isLeft, boolean isUpper)
    {
        if (isArm)
        {
            if (isLeft)
            {
                return isUpper ? BoneRegion.LEFT_ARM_UPPER : BoneRegion.LEFT_ARM_LOWER;
            }
            else
            {
                return isUpper ? BoneRegion.RIGHT_ARM_UPPER : BoneRegion.RIGHT_ARM_LOWER;
            }
        }
        else
        {
            if (isLeft)
            {
                return isUpper ? BoneRegion.LEFT_LEG_UPPER : BoneRegion.LEFT_LEG_LOWER;
            }
            else
            {
                return isUpper ? BoneRegion.RIGHT_LEG_UPPER : BoneRegion.RIGHT_LEG_LOWER;
            }
        }
    }
}
