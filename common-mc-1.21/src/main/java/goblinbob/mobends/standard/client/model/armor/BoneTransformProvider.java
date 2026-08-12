package goblinbob.mobends.standard.client.model.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import goblinbob.mobends.core.client.model.ModelPartTransform;
import goblinbob.mobends.standard.data.BipedEntityData;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import javax.annotation.Nullable;

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

    public Quaternionf getRotation(BoneRegion region, BipedEntityData<?> entityData)
    {
        ModelPartTransform transform = getTransform(region, entityData);
        if (transform == null || transform.rotation == null)
        {
            return new Quaternionf();
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

    public Matrix4f getFullTransform(BoneRegion region, BipedEntityData<?> entityData)
    {
        return coordinateSpaceManager.getFullBoneTransform(region, entityData);
    }

    public void applyBoneTransform(PoseStack poseStack, BoneRegion region, BipedEntityData<?> entityData)
    {
        ModelPartTransform transform = getTransform(region, entityData);
        if (transform != null)
        {
            coordinateSpaceManager.applyToPoseStack(poseStack, transform);
        }
    }

    public void applyFullBoneTransform(PoseStack poseStack, BoneRegion region, BipedEntityData<?> entityData)
    {
        java.util.List<BoneRegion> chain = new java.util.ArrayList<>();
        BoneRegion current = region;
        while (current != BoneRegion.ROOT)
        {
            chain.add(0, current);
            current = getParentRegion(current);
        }

        for (BoneRegion boneRegion : chain)
        {
            applyBoneTransform(poseStack, boneRegion, entityData);
        }
    }

    public boolean hasAnimation(BoneRegion region, BipedEntityData<?> entityData)
    {
        ModelPartTransform transform = getTransform(region, entityData);
        if (transform == null)
        {
            return false;
        }

        var smoothRot = transform.rotation.getSmooth();
        if (smoothRot != null)
        {
            if (Math.abs(smoothRot.w - 1.0) > 0.001 ||
                Math.abs(smoothRot.x) > 0.001 ||
                Math.abs(smoothRot.y) > 0.001 ||
                Math.abs(smoothRot.z) > 0.001)
            {
                return true;
            }
        }

        if (Math.abs(transform.offset.x) > 0.001 ||
            Math.abs(transform.offset.y) > 0.001 ||
            Math.abs(transform.offset.z) > 0.001)
        {
            return true;
        }

        return false;
    }

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

    public boolean isArmRegion(BoneRegion region)
    {
        return switch (region)
        {
            case LEFT_ARM_UPPER, LEFT_ARM_LOWER,
                 RIGHT_ARM_UPPER, RIGHT_ARM_LOWER -> true;
            default -> false;
        };
    }

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
