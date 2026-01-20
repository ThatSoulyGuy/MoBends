package goblinbob.mobends.standard.client.model.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import goblinbob.mobends.core.client.model.ModelPartTransform;
import goblinbob.mobends.standard.data.BipedEntityData;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import javax.annotation.Nullable;

/**
 * Provides bone transforms from entity animation data.
 * Acts as a bridge between BipedEntityData and the armor rendering system.
 */
@OnlyIn(Dist.CLIENT)
public class BoneTransformProvider
{
    private static final float MODEL_SCALE = 1.0f / 16.0f;

    private final CoordinateSpaceManager coordinateSpaceManager;

    public BoneTransformProvider()
    {
        this.coordinateSpaceManager = new CoordinateSpaceManager();
    }

    public BoneTransformProvider(CoordinateSpaceManager coordinateSpaceManager)
    {
        this.coordinateSpaceManager = coordinateSpaceManager;
    }

    /**
     * Get the ModelPartTransform for a specific bone region.
     *
     * @param region The bone region
     * @param entityData The entity's animation data
     * @return The transform, or null if not found
     */
    @Nullable
    public ModelPartTransform getTransform(BoneRegion region, BipedEntityData<?> entityData)
    {
        if (entityData == null) return null;

        return switch (region)
        {
            case HEAD -> entityData.head;
            case BODY -> entityData.body;
            case LEFT_ARM_UPPER -> entityData.leftArm;
            case LEFT_ARM_LOWER -> entityData.leftForeArm;
            case RIGHT_ARM_UPPER -> entityData.rightArm;
            case RIGHT_ARM_LOWER -> entityData.rightForeArm;
            case LEFT_LEG_UPPER -> entityData.leftLeg;
            case LEFT_LEG_LOWER -> entityData.leftForeLeg;
            case RIGHT_LEG_UPPER -> entityData.rightLeg;
            case RIGHT_LEG_LOWER -> entityData.rightForeLeg;
            case ROOT -> null;
        };
    }

    /**
     * Get the parent bone region for a given region.
     *
     * @param region The bone region
     * @return The parent region, or ROOT if this is a root-level bone
     */
    public BoneRegion getParentRegion(BoneRegion region)
    {
        return switch (region)
        {
            case HEAD -> BoneRegion.BODY;
            case LEFT_ARM_UPPER, RIGHT_ARM_UPPER -> BoneRegion.BODY;
            case LEFT_ARM_LOWER -> BoneRegion.LEFT_ARM_UPPER;
            case RIGHT_ARM_LOWER -> BoneRegion.RIGHT_ARM_UPPER;
            case LEFT_LEG_LOWER -> BoneRegion.LEFT_LEG_UPPER;
            case RIGHT_LEG_LOWER -> BoneRegion.RIGHT_LEG_UPPER;
            default -> BoneRegion.ROOT;
        };
    }

    /**
     * Check if a bone region is a child of another region.
     *
     * @param child Potential child region
     * @param parent Potential parent region
     * @return true if child is a descendant of parent
     */
    public boolean isChildOf(BoneRegion child, BoneRegion parent)
    {
        if (child == parent) return false;

        BoneRegion current = child;
        while (current != BoneRegion.ROOT)
        {
            current = getParentRegion(current);
            if (current == parent) return true;
        }
        return false;
    }

    /**
     * Get the rotation for a bone as a Quaternion.
     *
     * @param region The bone region
     * @param entityData The entity's animation data
     * @return The rotation quaternion, or identity if not found
     */
    public Quaternionf getRotation(BoneRegion region, BipedEntityData<?> entityData)
    {
        ModelPartTransform transform = getTransform(region, entityData);
        if (transform == null || transform.rotation == null)
        {
            return new Quaternionf(); // Identity
        }

        var smoothRot = transform.rotation.getSmooth();
        if (smoothRot == null)
        {
            return new Quaternionf();
        }

        return new Quaternionf(
            (float) smoothRot.x,
            (float) smoothRot.y,
            (float) smoothRot.z,
            (float) smoothRot.w
        );
    }

    /**
     * Get the position offset for a bone.
     *
     * @param region The bone region
     * @param entityData The entity's animation data
     * @return The position offset in model space
     */
    public Vector3f getPosition(BoneRegion region, BipedEntityData<?> entityData)
    {
        ModelPartTransform transform = getTransform(region, entityData);
        if (transform == null)
        {
            return new Vector3f();
        }

        return new Vector3f(
            transform.position.x * MODEL_SCALE,
            transform.position.y * MODEL_SCALE,
            transform.position.z * MODEL_SCALE
        );
    }

    /**
     * Get the full world-space transform matrix for a bone.
     *
     * @param region The bone region
     * @param entityData The entity's animation data
     * @return Full transform matrix including parent chain
     */
    public Matrix4f getFullTransform(BoneRegion region, BipedEntityData<?> entityData)
    {
        return coordinateSpaceManager.getFullBoneTransform(region, entityData);
    }

    /**
     * Apply a bone's transform to a PoseStack.
     *
     * @param poseStack The pose stack to modify
     * @param region The bone region
     * @param entityData The entity's animation data
     */
    public void applyBoneTransform(PoseStack poseStack, BoneRegion region, BipedEntityData<?> entityData)
    {
        ModelPartTransform transform = getTransform(region, entityData);
        if (transform != null)
        {
            coordinateSpaceManager.applyToPoseStack(poseStack, transform);
        }
    }

    /**
     * Apply the full transform chain for a bone to a PoseStack.
     * This applies all parent transforms in order.
     *
     * @param poseStack The pose stack to modify
     * @param region The bone region
     * @param entityData The entity's animation data
     */
    public void applyFullBoneTransform(PoseStack poseStack, BoneRegion region, BipedEntityData<?> entityData)
    {
        // Build parent chain
        java.util.List<BoneRegion> chain = new java.util.ArrayList<>();
        BoneRegion current = region;
        while (current != BoneRegion.ROOT)
        {
            chain.add(0, current); // Add to front
            current = getParentRegion(current);
        }

        // Apply transforms in order
        for (BoneRegion boneRegion : chain)
        {
            applyBoneTransform(poseStack, boneRegion, entityData);
        }
    }

    /**
     * Check if a bone has any significant animation applied.
     *
     * @param region The bone region
     * @param entityData The entity's animation data
     * @return true if the bone has non-identity rotation or non-zero position offset
     */
    public boolean hasAnimation(BoneRegion region, BipedEntityData<?> entityData)
    {
        ModelPartTransform transform = getTransform(region, entityData);
        if (transform == null)
        {
            return false;
        }

        // Check for rotation
        var smoothRot = transform.rotation.getSmooth();
        if (smoothRot != null)
        {
            // Check if rotation is non-identity (w != 1 or any xyz != 0)
            if (Math.abs(smoothRot.w - 1.0) > 0.001 ||
                Math.abs(smoothRot.x) > 0.001 ||
                Math.abs(smoothRot.y) > 0.001 ||
                Math.abs(smoothRot.z) > 0.001)
            {
                return true;
            }
        }

        // Check for position offset
        if (Math.abs(transform.offset.x) > 0.001 ||
            Math.abs(transform.offset.y) > 0.001 ||
            Math.abs(transform.offset.z) > 0.001)
        {
            return true;
        }

        return false;
    }

    /**
     * Get the corresponding upper limb region for a lower limb region.
     *
     * @param lowerRegion A lower limb region (forearm or lower leg)
     * @return The corresponding upper limb region, or the input if not a lower limb
     */
    public BoneRegion getUpperLimbRegion(BoneRegion lowerRegion)
    {
        return switch (lowerRegion)
        {
            case LEFT_ARM_LOWER -> BoneRegion.LEFT_ARM_UPPER;
            case RIGHT_ARM_LOWER -> BoneRegion.RIGHT_ARM_UPPER;
            case LEFT_LEG_LOWER -> BoneRegion.LEFT_LEG_UPPER;
            case RIGHT_LEG_LOWER -> BoneRegion.RIGHT_LEG_UPPER;
            default -> lowerRegion;
        };
    }

    /**
     * Get the corresponding lower limb region for an upper limb region.
     *
     * @param upperRegion An upper limb region (upper arm or upper leg)
     * @return The corresponding lower limb region, or the input if not an upper limb
     */
    public BoneRegion getLowerLimbRegion(BoneRegion upperRegion)
    {
        return switch (upperRegion)
        {
            case LEFT_ARM_UPPER -> BoneRegion.LEFT_ARM_LOWER;
            case RIGHT_ARM_UPPER -> BoneRegion.RIGHT_ARM_LOWER;
            case LEFT_LEG_UPPER -> BoneRegion.LEFT_LEG_LOWER;
            case RIGHT_LEG_UPPER -> BoneRegion.RIGHT_LEG_LOWER;
            default -> upperRegion;
        };
    }

    /**
     * Check if a region is part of a limb (arm or leg).
     */
    public boolean isLimbRegion(BoneRegion region)
    {
        return switch (region)
        {
            case LEFT_ARM_UPPER, LEFT_ARM_LOWER,
                 RIGHT_ARM_UPPER, RIGHT_ARM_LOWER,
                 LEFT_LEG_UPPER, LEFT_LEG_LOWER,
                 RIGHT_LEG_UPPER, RIGHT_LEG_LOWER -> true;
            default -> false;
        };
    }

    /**
     * Check if a region is an arm.
     */
    public boolean isArmRegion(BoneRegion region)
    {
        return switch (region)
        {
            case LEFT_ARM_UPPER, LEFT_ARM_LOWER,
                 RIGHT_ARM_UPPER, RIGHT_ARM_LOWER -> true;
            default -> false;
        };
    }

    /**
     * Check if a region is a leg.
     */
    public boolean isLegRegion(BoneRegion region)
    {
        return switch (region)
        {
            case LEFT_LEG_UPPER, LEFT_LEG_LOWER,
                 RIGHT_LEG_UPPER, RIGHT_LEG_LOWER -> true;
            default -> false;
        };
    }
}
