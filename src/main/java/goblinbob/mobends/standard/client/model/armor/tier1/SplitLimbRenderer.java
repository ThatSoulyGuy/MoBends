package goblinbob.mobends.standard.client.model.armor.tier1;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import goblinbob.mobends.core.client.model.ModelPartTransform;
import goblinbob.mobends.core.math.Quaternion;
import goblinbob.mobends.core.math.SmoothOrientation;
import goblinbob.mobends.standard.client.model.armor.BoneRegion;
import goblinbob.mobends.standard.client.model.armor.JointDefinitions;
import goblinbob.mobends.standard.client.model.armor.JointPlane;
import goblinbob.mobends.standard.data.BipedEntityData;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Quaternionf;

/**
 * Renders limbs as split upper/lower segments for proper joint bending.
 *
 * For arms and legs, vanilla armor renders the entire limb as one piece.
 * Mo'Bends bends these at the elbow/knee, so we need to:
 * 1. Render the upper portion with the upper bone's transform
 * 2. Render the lower portion with the lower bone's (forearm/lower leg) transform
 *
 * This class handles the split rendering by using clip planes at joint positions.
 */
@OnlyIn(Dist.CLIENT)
public class SplitLimbRenderer
{
    private static final float SCALE = 1.0f / 16.0f;

    /**
     * Render a split arm (upper arm + forearm).
     *
     * @param poseStack The pose stack
     * @param vertexConsumer The vertex consumer for rendering
     * @param armPart The arm ModelPart from the armor model
     * @param entityData The entity's animation data
     * @param isLeft True for left arm, false for right arm
     * @param packedLight Light value
     * @param packedOverlay Overlay value
     */
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
            // Fallback: render whole arm without split
            renderWholeWithTransform(poseStack, vertexConsumer, armPart, upperArm, packedLight, packedOverlay);
            return;
        }

        JointPlane elbowPlane = JointDefinitions.getElbow(isLeft);

        // Render upper arm portion
        renderUpperPortion(poseStack, vertexConsumer, armPart, upperArm, elbowPlane, packedLight, packedOverlay);

        // Render lower arm (forearm) portion
        renderLowerPortion(poseStack, vertexConsumer, armPart, upperArm, foreArm, elbowPlane, packedLight, packedOverlay);
    }

    /**
     * Render a split leg (upper leg + lower leg).
     *
     * @param poseStack The pose stack
     * @param vertexConsumer The vertex consumer for rendering
     * @param legPart The leg ModelPart from the armor model
     * @param entityData The entity's animation data
     * @param isLeft True for left leg, false for right leg
     * @param packedLight Light value
     * @param packedOverlay Overlay value
     */
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
            // Fallback: render whole leg without split
            renderWholeWithTransform(poseStack, vertexConsumer, legPart, upperLeg, packedLight, packedOverlay);
            return;
        }

        JointPlane kneePlane = JointDefinitions.getKnee(isLeft);

        // Render upper leg portion
        renderUpperPortion(poseStack, vertexConsumer, legPart, upperLeg, kneePlane, packedLight, packedOverlay);

        // Render lower leg portion
        renderLowerPortion(poseStack, vertexConsumer, legPart, upperLeg, lowerLeg, kneePlane, packedLight, packedOverlay);
    }

    /**
     * Render the upper portion of a limb (above the joint plane).
     */
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

        // Apply upper bone transform
        applyTransform(poseStack, upperTransform);

        // Render the part (vanilla will handle the geometry)
        // In a full implementation, we would clip vertices above the joint plane
        // For now, we render the whole part with the upper transform
        limbPart.render(poseStack, vertexConsumer, packedLight, packedOverlay);

        poseStack.popPose();
    }

    /**
     * Render the lower portion of a limb (below the joint plane).
     */
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

        // Apply upper bone transform first (parent)
        applyTransform(poseStack, upperTransform);

        // Then apply lower bone transform (child)
        applyTransform(poseStack, lowerTransform);

        // Render the part
        // In a full implementation, we would clip vertices below the joint plane
        // The lower portion rendering is handled differently in practice
        // by rendering the forearm/lower leg geometry specifically

        poseStack.popPose();
    }

    /**
     * Render a limb without split (fallback).
     */
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

    /**
     * Apply a ModelPartTransform to the pose stack.
     */
    private void applyTransform(PoseStack poseStack, ModelPartTransform transform)
    {
        if (transform == null)
        {
            return;
        }

        // Apply position
        if (transform.position.x != 0 || transform.position.y != 0 || transform.position.z != 0)
        {
            poseStack.translate(
                    transform.position.x * SCALE * transform.offsetScale,
                    transform.position.y * SCALE * transform.offsetScale,
                    transform.position.z * SCALE * transform.offsetScale
            );
        }

        // Apply offset
        if (transform.offset.x != 0 || transform.offset.y != 0 || transform.offset.z != 0)
        {
            poseStack.translate(
                    transform.offset.x * SCALE * transform.offsetScale,
                    transform.offset.y * SCALE * transform.offsetScale,
                    transform.offset.z * SCALE * transform.offsetScale
            );
        }

        // Apply rotation
        SmoothOrientation orientation = transform.rotation;
        if (orientation != null)
        {
            Quaternion q = orientation.getSmooth();
            if (q != null && !q.isIdentity())
            {
                poseStack.mulPose(new Quaternionf((float) q.x, (float) q.y, (float) q.z, (float) q.w));
            }
        }

        // Apply scale
        if (transform.scale.x != 1 || transform.scale.y != 1 || transform.scale.z != 1)
        {
            poseStack.scale(transform.scale.x, transform.scale.y, transform.scale.z);
        }
    }

    /**
     * Check if a limb needs to be split-rendered.
     * A limb needs splitting if the joint (elbow/knee) has significant bend.
     *
     * @param upperTransform The upper limb transform
     * @param lowerTransform The lower limb transform
     * @return True if split rendering is needed
     */
    public boolean needsSplitRendering(ModelPartTransform upperTransform, ModelPartTransform lowerTransform)
    {
        if (upperTransform == null || lowerTransform == null)
        {
            return false;
        }

        // Check if the lower bone has any rotation (bend at joint)
        SmoothOrientation lowerOrientation = lowerTransform.rotation;
        if (lowerOrientation != null)
        {
            Quaternion q = lowerOrientation.getSmooth();
            if (q != null && !q.isIdentity())
            {
                // Has rotation, needs split
                return true;
            }
        }

        return false;
    }

    /**
     * Get the bone region for a limb part.
     *
     * @param isArm True for arm, false for leg
     * @param isLeft True for left side
     * @param isUpper True for upper segment
     * @return The corresponding bone region
     */
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
