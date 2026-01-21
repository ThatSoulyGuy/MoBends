package goblinbob.mobends.standard.client.model.armor.tier1;

import com.mojang.blaze3d.vertex.PoseStack;
import goblinbob.mobends.core.client.model.ModelPartTransform;
import goblinbob.mobends.core.math.Quaternion;
import goblinbob.mobends.core.math.SmoothOrientation;
import goblinbob.mobends.standard.client.model.armor.BoneRegion;
import goblinbob.mobends.standard.client.model.armor.BoneTransformProvider;
import goblinbob.mobends.standard.data.BipedEntityData;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Quaternionf;

/**
 * Injects Mo'Bends transforms into HumanoidModel armor rendering.
 * This is the core of Tier 1 rendering - it modifies the armor model's
 * transforms to match the animated entity's bone positions.
 */
@OnlyIn(Dist.CLIENT)
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

    /**
     * Inject Mo'Bends transforms into a HumanoidModel.
     * This REPLACES the model's part rotations with Mo'Bends animated values.
     *
     * Note: Body rotation is NOT injected here - it's applied via PoseStack
     * in applyBodyTransform() to avoid double-application.
     *
     * @param model The armor model to modify
     * @param entityData The entity's animation data
     */
    public void injectTransforms(HumanoidModel<?> model, BipedEntityData<?> entityData)
    {
        if (model == null || entityData == null)
        {
            return;
        }

        // Inject transforms for each body part
        // Body rotation is applied via PoseStack, so only inject offset for body
        injectPartTransform(model.head, entityData.head, true);
        injectPartTransform(model.body, entityData.body, false);  // No rotation - applied via PoseStack
        injectPartTransform(model.leftArm, entityData.leftArm, true);
        injectPartTransform(model.rightArm, entityData.rightArm, true);
        injectPartTransform(model.leftLeg, entityData.leftLeg, true);
        injectPartTransform(model.rightLeg, entityData.rightLeg, true);
    }

    /**
     * Inject transform for a single model part.
     * This SETS (replaces) the rotation, not adds to it.
     *
     * @param part The ModelPart to modify
     * @param transform The Mo'Bends transform to apply
     * @param applyRotation Whether to apply Mo'Bends rotation (false for body since it's on PoseStack)
     */
    private void injectPartTransform(ModelPart part, ModelPartTransform transform, boolean applyRotation)
    {
        if (part == null || transform == null)
        {
            return;
        }

        // SET rotation from Mo'Bends animation (replacing vanilla poses)
        if (applyRotation)
        {
            SmoothOrientation orientation = transform.rotation;
            if (orientation != null)
            {
                Quaternion q = orientation.getSmooth();
                if (q != null)
                {
                    // Convert quaternion to Euler angles and SET (not add) part rotation
                    float[] euler = quaternionToEuler(q);
                    part.xRot = euler[0];
                    part.yRot = euler[1];
                    part.zRot = euler[2];
                }
                else
                {
                    // Null quaternion - reset to identity
                    part.xRot = 0;
                    part.yRot = 0;
                    part.zRot = 0;
                }
            }
            else
            {
                // No orientation data - reset to identity
                part.xRot = 0;
                part.yRot = 0;
                part.zRot = 0;
            }
        }
        else
        {
            // Body rotation is applied via PoseStack, so reset part rotation to identity
            // This prevents double-application of rotation (vanilla on part + Mo'Bends on PoseStack)
            part.xRot = 0;
            part.yRot = 0;
            part.zRot = 0;
        }

        // Apply position offset (add to default position)
        if (transform.offset.x != 0 || transform.offset.y != 0 || transform.offset.z != 0)
        {
            part.x += transform.offset.x;
            part.y += transform.offset.y;
            part.z += transform.offset.z;
        }
    }

    /**
     * Apply Mo'Bends body rotation to the entire model via PoseStack.
     * Call this before rendering to apply the base body transform.
     *
     * Note: We only apply body ROTATION here, not position translations.
     * The position translations (globalOffset, body.position, body.offset) are
     * already accounted for in the entity's render position by the player model.
     * Applying them again would cause double-offset.
     *
     * @param poseStack The pose stack to modify
     * @param entityData The entity's animation data
     */
    public void applyBodyTransform(PoseStack poseStack, BipedEntityData<?> entityData)
    {
        if (entityData == null || entityData.body == null)
        {
            return;
        }

        ModelPartTransform body = entityData.body;

        // Only apply body rotation - position offsets are already applied by player rendering
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

    /**
     * Get the rotation quaternion for a specific bone.
     *
     * @param region The bone region
     * @param entityData The entity's animation data
     * @return The rotation quaternion for the bone
     */
    public Quaternionf getBoneRotation(BoneRegion region, BipedEntityData<?> entityData)
    {
        return transformProvider.getRotation(region, entityData);
    }

    /**
     * Apply a bone's full transform to a PoseStack.
     *
     * @param poseStack The pose stack to modify
     * @param region The bone region
     * @param entityData The entity's animation data
     */
    public void applyBoneTransform(PoseStack poseStack, BoneRegion region, BipedEntityData<?> entityData)
    {
        transformProvider.applyBoneTransform(poseStack, region, entityData);
    }

    /**
     * Convert a Mo'Bends quaternion to Euler angles (XYZ order).
     *
     * @param q The quaternion
     * @return Array of [xRot, yRot, zRot] in radians
     */
    private float[] quaternionToEuler(Quaternion q)
    {
        float[] angles = new float[3];

        float qx = (float) q.x;
        float qy = (float) q.y;
        float qz = (float) q.z;
        float qw = (float) q.w;

        // Roll (x-axis rotation)
        float sinr_cosp = 2 * (qw * qx + qy * qz);
        float cosr_cosp = 1 - 2 * (qx * qx + qy * qy);
        angles[0] = (float) Math.atan2(sinr_cosp, cosr_cosp);

        // Pitch (y-axis rotation)
        float sinp = 2 * (qw * qy - qz * qx);
        if (Math.abs(sinp) >= 1)
        {
            angles[1] = (float) Math.copySign(Math.PI / 2, sinp);
        }
        else
        {
            angles[1] = (float) Math.asin(sinp);
        }

        // Yaw (z-axis rotation)
        float siny_cosp = 2 * (qw * qz + qx * qy);
        float cosy_cosp = 1 - 2 * (qy * qy + qz * qz);
        angles[2] = (float) Math.atan2(siny_cosp, cosy_cosp);

        return angles;
    }

    /**
     * Get the underlying transform provider.
     */
    public BoneTransformProvider getTransformProvider()
    {
        return transformProvider;
    }
}
