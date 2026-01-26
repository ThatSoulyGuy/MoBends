package goblinbob.mobends.standard.client.model.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import goblinbob.mobends.core.client.model.ModelPartTransform;
import goblinbob.mobends.standard.data.BipedEntityData;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Manages coordinate space transformations for armor rendering.
 *
 * Coordinate spaces (from outer to inner):
 * 1. World Space - Global coordinates
 * 2. Entity Space - Relative to entity position/rotation
 * 3. Model Space - Minecraft's model coordinate system (Y up, 1/16 scale)
 * 4. Bone Space - Local to a specific bone's transform
 * 5. Animated Space - After Mo'Bends animation transforms applied
 */
public class CoordinateSpaceManager
{
    // Minecraft model scale (1 unit = 1/16 of a block)
    private static final float MODEL_SCALE = 1.0f / 16.0f;

    // Reusable matrices to avoid allocations
    private final Matrix4f tempMatrix = new Matrix4f();
    private final Matrix4f resultMatrix = new Matrix4f();
    private final Vector3f tempVector = new Vector3f();
    private final Quaternionf tempQuat = new Quaternionf();

    /**
     * Get the transform matrix for a specific bone region from entity data.
     * This matrix transforms from rest pose bone space to animated bone space.
     *
     * @param region The bone region to get the transform for
     * @param entityData The entity's animation data
     * @return Transform matrix for the bone
     */
    public Matrix4f getBoneTransform(BoneRegion region, BipedEntityData<?> entityData)
    {
        ModelPartTransform transform = getTransformForRegion(region, entityData);
        if (transform == null)
        {
            return new Matrix4f(); // Identity
        }

        return buildTransformMatrix(transform);
    }

    /**
     * Get the full transform chain for a bone, including parent transforms.
     *
     * @param region The bone region
     * @param entityData The entity's animation data
     * @return Full transform matrix including parent chain
     */
    public Matrix4f getFullBoneTransform(BoneRegion region, BipedEntityData<?> entityData)
    {
        resultMatrix.identity();

        // Build transform chain from root to target bone
        switch (region)
        {
            case HEAD:
                applyTransform(resultMatrix, entityData.body);
                applyTransform(resultMatrix, entityData.head);
                break;

            case BODY:
                applyTransform(resultMatrix, entityData.body);
                break;

            case LEFT_ARM_UPPER:
                applyTransform(resultMatrix, entityData.body);
                applyTransform(resultMatrix, entityData.leftArm);
                break;

            case LEFT_ARM_LOWER:
                applyTransform(resultMatrix, entityData.body);
                applyTransform(resultMatrix, entityData.leftArm);
                applyTransform(resultMatrix, entityData.leftForeArm);
                break;

            case RIGHT_ARM_UPPER:
                applyTransform(resultMatrix, entityData.body);
                applyTransform(resultMatrix, entityData.rightArm);
                break;

            case RIGHT_ARM_LOWER:
                applyTransform(resultMatrix, entityData.body);
                applyTransform(resultMatrix, entityData.rightArm);
                applyTransform(resultMatrix, entityData.rightForeArm);
                break;

            case LEFT_LEG_UPPER:
                applyTransform(resultMatrix, entityData.leftLeg);
                break;

            case LEFT_LEG_LOWER:
                applyTransform(resultMatrix, entityData.leftLeg);
                applyTransform(resultMatrix, entityData.leftForeLeg);
                break;

            case RIGHT_LEG_UPPER:
                applyTransform(resultMatrix, entityData.rightLeg);
                break;

            case RIGHT_LEG_LOWER:
                applyTransform(resultMatrix, entityData.rightLeg);
                applyTransform(resultMatrix, entityData.rightForeLeg);
                break;

            case ROOT:
            default:
                // Identity matrix
                break;
        }

        return new Matrix4f(resultMatrix);
    }

    /**
     * Get the inverse rest pose matrix for a bone.
     * Used to transform vertices from rest pose to bone-local space.
     *
     * @param region The bone region
     * @param entityData The entity's animation data (for rest positions)
     * @return Inverse rest pose matrix
     */
    public Matrix4f getInverseRestPose(BoneRegion region, BipedEntityData<?> entityData)
    {
        Matrix4f restPose = getRestPoseMatrix(region, entityData);
        return restPose.invert(new Matrix4f());
    }

    /**
     * Get the rest pose matrix for a bone (before animation).
     *
     * @param region The bone region
     * @param entityData The entity's animation data (for rest positions)
     * @return Rest pose matrix
     */
    public Matrix4f getRestPoseMatrix(BoneRegion region, BipedEntityData<?> entityData)
    {
        resultMatrix.identity();

        // Build rest pose transform (position only, no rotation)
        ModelPartTransform transform = getTransformForRegion(region, entityData);
        if (transform != null)
        {
            // Apply only position for rest pose
            tempVector.set(
                transform.position.x * MODEL_SCALE,
                transform.position.y * MODEL_SCALE,
                transform.position.z * MODEL_SCALE
            );
            resultMatrix.translate(tempVector);
        }

        return new Matrix4f(resultMatrix);
    }

    /**
     * Transform a vertex from one bone's space to another.
     *
     * @param vertex The vertex position to transform
     * @param fromRegion Source bone region
     * @param toRegion Target bone region
     * @param entityData The entity's animation data
     * @return Transformed vertex position
     */
    public Vector3f transformVertex(Vector3f vertex, BoneRegion fromRegion, BoneRegion toRegion,
                                   BipedEntityData<?> entityData)
    {
        // Get transforms for both bones
        Matrix4f fromTransform = getFullBoneTransform(fromRegion, entityData);
        Matrix4f toTransform = getFullBoneTransform(toRegion, entityData);

        // Transform: toInverse * from * vertex
        Matrix4f toInverse = toTransform.invert(new Matrix4f());
        Matrix4f combined = toInverse.mul(fromTransform, new Matrix4f());

        Vector3f result = new Vector3f(vertex);
        return combined.transformPosition(result);
    }

    /**
     * Apply a ModelPartTransform to a PoseStack.
     *
     * @param poseStack The pose stack to modify
     * @param transform The transform to apply
     */
    public void applyToPoseStack(PoseStack poseStack, ModelPartTransform transform)
    {
        if (transform == null) return;

        // Apply global offset
        if (transform.globalOffset.x != 0 || transform.globalOffset.y != 0 || transform.globalOffset.z != 0)
        {
            poseStack.translate(
                transform.globalOffset.x * MODEL_SCALE,
                transform.globalOffset.y * MODEL_SCALE,
                transform.globalOffset.z * MODEL_SCALE
            );
        }

        // Apply position
        if (transform.position.x != 0 || transform.position.y != 0 || transform.position.z != 0)
        {
            poseStack.translate(
                transform.position.x * MODEL_SCALE * transform.offsetScale,
                transform.position.y * MODEL_SCALE * transform.offsetScale,
                transform.position.z * MODEL_SCALE * transform.offsetScale
            );
        }

        // Apply offset
        if (transform.offset.x != 0 || transform.offset.y != 0 || transform.offset.z != 0)
        {
            poseStack.translate(
                transform.offset.x * MODEL_SCALE * transform.offsetScale,
                transform.offset.y * MODEL_SCALE * transform.offsetScale,
                transform.offset.z * MODEL_SCALE * transform.offsetScale
            );
        }

        // Apply rotation
        var smoothRot = transform.rotation.getSmooth();
        if (smoothRot != null)
        {
            poseStack.mulPose(new Quaternionf(
                (float) smoothRot.x,
                (float) smoothRot.y,
                (float) smoothRot.z,
                (float) smoothRot.w
            ));
        }

        // Apply scale
        if (transform.scale.x != 1 || transform.scale.y != 1 || transform.scale.z != 1)
        {
            poseStack.scale(transform.scale.x, transform.scale.y, transform.scale.z);
        }
    }

    /**
     * Get the ModelPartTransform for a specific bone region.
     */
    private ModelPartTransform getTransformForRegion(BoneRegion region, BipedEntityData<?> entityData)
    {
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
     * Apply a ModelPartTransform to a matrix.
     */
    private void applyTransform(Matrix4f matrix, ModelPartTransform transform)
    {
        if (transform == null) return;

        // Apply global offset
        if (transform.globalOffset.x != 0 || transform.globalOffset.y != 0 || transform.globalOffset.z != 0)
        {
            tempVector.set(
                transform.globalOffset.x * MODEL_SCALE,
                transform.globalOffset.y * MODEL_SCALE,
                transform.globalOffset.z * MODEL_SCALE
            );
            matrix.translate(tempVector);
        }

        // Apply position
        if (transform.position.x != 0 || transform.position.y != 0 || transform.position.z != 0)
        {
            tempVector.set(
                transform.position.x * MODEL_SCALE * transform.offsetScale,
                transform.position.y * MODEL_SCALE * transform.offsetScale,
                transform.position.z * MODEL_SCALE * transform.offsetScale
            );
            matrix.translate(tempVector);
        }

        // Apply offset
        if (transform.offset.x != 0 || transform.offset.y != 0 || transform.offset.z != 0)
        {
            tempVector.set(
                transform.offset.x * MODEL_SCALE * transform.offsetScale,
                transform.offset.y * MODEL_SCALE * transform.offsetScale,
                transform.offset.z * MODEL_SCALE * transform.offsetScale
            );
            matrix.translate(tempVector);
        }

        // Apply rotation
        var smoothRot = transform.rotation.getSmooth();
        if (smoothRot != null)
        {
            tempQuat.set((float) smoothRot.x, (float) smoothRot.y, (float) smoothRot.z, (float) smoothRot.w);
            matrix.rotate(tempQuat);
        }

        // Apply scale
        if (transform.scale.x != 1 || transform.scale.y != 1 || transform.scale.z != 1)
        {
            matrix.scale(transform.scale.x, transform.scale.y, transform.scale.z);
        }
    }

    /**
     * Build a transform matrix from a ModelPartTransform.
     */
    private Matrix4f buildTransformMatrix(ModelPartTransform transform)
    {
        tempMatrix.identity();
        applyTransform(tempMatrix, transform);
        return new Matrix4f(tempMatrix);
    }
}
