package goblinbob.mobends.standard.client.model.armor.tier2;

import com.mojang.blaze3d.vertex.PoseStack;
import goblinbob.mobends.core.client.model.ModelPartTransform;
import goblinbob.mobends.standard.client.model.armor.BoneRegion;
import goblinbob.mobends.standard.client.model.armor.BoneTransformProvider;
import goblinbob.mobends.standard.client.model.armor.tier.PartClassification;
import goblinbob.mobends.standard.data.BipedEntityData;
import net.minecraft.client.model.geom.ModelPart;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Quaternionf;

/**
 * Transforms ModelPart poses for Tier 2 rendering.
 * Takes classified parts and applies Mo'Bends animation transforms to them.
 *
 * This works by modifying the ModelPart's rotation/position values before
 * the vanilla render, rather than capturing and re-transforming vertices.
 */
@OnlyIn(Dist.CLIENT)
public class ModelPartTransformer
{
    private final BoneTransformProvider transformProvider;

    // Store original values for restoration
    private float originalX, originalY, originalZ;
    private float originalXRot, originalYRot, originalZRot;
    private boolean hasStoredOriginal = false;

    public ModelPartTransformer()
    {
        this.transformProvider = new BoneTransformProvider();
    }

    public ModelPartTransformer(BoneTransformProvider transformProvider)
    {
        this.transformProvider = transformProvider;
    }

    /**
     * Apply Mo'Bends animation transforms to a classified model part.
     * This SETS (replaces) the rotation, not adds to it.
     *
     * @param part The ModelPart to transform
     * @param classification The part's bone region classification
     * @param entityData The entity's animation data
     */
    public void applyTransform(ModelPart part, PartClassification classification, BipedEntityData<?> entityData)
    {
        if (part == null || classification == null || entityData == null)
        {
            return;
        }

        BoneRegion region = classification.getBoneRegion();
        if (region == BoneRegion.ROOT)
        {
            return; // No transform for root/unknown parts
        }

        // Store original values
        storeOriginal(part);

        // Get the animation transform for this bone
        ModelPartTransform boneTransform = transformProvider.getTransform(region, entityData);
        if (boneTransform == null)
        {
            return;
        }

        // SET rotation from Mo'Bends (replacing vanilla poses)
        // Note: For BODY region, rotation may be applied via PoseStack instead
        Quaternionf rotation = transformProvider.getRotation(region, entityData);
        if (rotation != null)
        {
            // Convert quaternion to Euler angles and SET (not add)
            float[] eulerAngles = quaternionToEuler(rotation);
            part.xRot = eulerAngles[0];
            part.yRot = eulerAngles[1];
            part.zRot = eulerAngles[2];
        }
        else
        {
            // No rotation data - reset to identity
            part.xRot = 0;
            part.yRot = 0;
            part.zRot = 0;
        }

        // Apply position offset (add to default position)
        if (boneTransform.offset.x != 0 || boneTransform.offset.y != 0 || boneTransform.offset.z != 0)
        {
            part.x += boneTransform.offset.x;
            part.y += boneTransform.offset.y;
            part.z += boneTransform.offset.z;
        }
    }

    /**
     * Apply transform to a PoseStack instead of modifying the ModelPart.
     * Use this when you need to preserve the original ModelPart state.
     *
     * @param poseStack The pose stack to modify
     * @param classification The part's bone region classification
     * @param entityData The entity's animation data
     */
    public void applyTransformToPoseStack(PoseStack poseStack, PartClassification classification, BipedEntityData<?> entityData)
    {
        if (classification == null || entityData == null)
        {
            return;
        }

        BoneRegion region = classification.getBoneRegion();
        if (region == BoneRegion.ROOT)
        {
            return;
        }

        transformProvider.applyBoneTransform(poseStack, region, entityData);
    }

    /**
     * Apply full bone transform chain to a PoseStack.
     * This includes all parent transforms.
     *
     * @param poseStack The pose stack to modify
     * @param classification The part's bone region classification
     * @param entityData The entity's animation data
     */
    public void applyFullTransformToPoseStack(PoseStack poseStack, PartClassification classification, BipedEntityData<?> entityData)
    {
        if (classification == null || entityData == null)
        {
            return;
        }

        BoneRegion region = classification.getBoneRegion();
        if (region == BoneRegion.ROOT)
        {
            return;
        }

        transformProvider.applyFullBoneTransform(poseStack, region, entityData);
    }

    /**
     * Restore the original values of a ModelPart.
     * Call this after rendering to return the part to its original state.
     *
     * @param part The ModelPart to restore
     */
    public void restoreOriginal(ModelPart part)
    {
        if (hasStoredOriginal && part != null)
        {
            part.x = originalX;
            part.y = originalY;
            part.z = originalZ;
            part.xRot = originalXRot;
            part.yRot = originalYRot;
            part.zRot = originalZRot;
            hasStoredOriginal = false;
        }
    }

    /**
     * Store the original values of a ModelPart for later restoration.
     */
    private void storeOriginal(ModelPart part)
    {
        originalX = part.x;
        originalY = part.y;
        originalZ = part.z;
        originalXRot = part.xRot;
        originalYRot = part.yRot;
        originalZRot = part.zRot;
        hasStoredOriginal = true;
    }

    /**
     * Convert a quaternion to Euler angles (XYZ order).
     * Note: This is an approximation and may have gimbal lock issues.
     *
     * @param q The quaternion
     * @return Array of [xRot, yRot, zRot] in radians
     */
    private float[] quaternionToEuler(Quaternionf q)
    {
        float[] angles = new float[3];

        // Roll (x-axis rotation)
        float sinr_cosp = 2 * (q.w * q.x + q.y * q.z);
        float cosr_cosp = 1 - 2 * (q.x * q.x + q.y * q.y);
        angles[0] = (float) Math.atan2(sinr_cosp, cosr_cosp);

        // Pitch (y-axis rotation)
        float sinp = 2 * (q.w * q.y - q.z * q.x);
        if (Math.abs(sinp) >= 1)
        {
            angles[1] = (float) Math.copySign(Math.PI / 2, sinp); // Use 90 degrees if out of range
        }
        else
        {
            angles[1] = (float) Math.asin(sinp);
        }

        // Yaw (z-axis rotation)
        float siny_cosp = 2 * (q.w * q.z + q.x * q.y);
        float cosy_cosp = 1 - 2 * (q.y * q.y + q.z * q.z);
        angles[2] = (float) Math.atan2(siny_cosp, cosy_cosp);

        return angles;
    }

    /**
     * Check if a part has any animation applied.
     *
     * @param classification The part's classification
     * @param entityData The entity's animation data
     * @return true if the bone has non-identity transforms
     */
    public boolean hasAnimation(PartClassification classification, BipedEntityData<?> entityData)
    {
        if (classification == null || entityData == null)
        {
            return false;
        }
        return transformProvider.hasAnimation(classification.getBoneRegion(), entityData);
    }

    /**
     * Get the bone transform provider.
     */
    public BoneTransformProvider getTransformProvider()
    {
        return transformProvider;
    }
}
