package goblinbob.mobends.standard.client.model.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import goblinbob.mobends.core.client.model.ModelPartTransform;
import goblinbob.mobends.standard.data.BipedEntityData;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class CoordinateSpaceManager
{
    private static final float MODEL_SCALE = 1.0f / 16.0f;

    private final Matrix4f tempMatrix = new Matrix4f();
    private final Matrix4f resultMatrix = new Matrix4f();
    private final Vector3f tempVector = new Vector3f();
    private final Quaternionf tempQuat = new Quaternionf();

    public Matrix4f getBoneTransform(BoneRegion region, BipedEntityData<?> entityData)
    {
        ModelPartTransform transform = getTransformForRegion(region, entityData);
        if (transform == null)
        {
            return new Matrix4f();
        }

        return buildTransformMatrix(transform);
    }

    public Matrix4f getFullBoneTransform(BoneRegion region, BipedEntityData<?> entityData)
    {
        resultMatrix.identity();

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
                break;
        }

        return new Matrix4f(resultMatrix);
    }

    public Matrix4f getInverseRestPose(BoneRegion region, BipedEntityData<?> entityData)
    {
        Matrix4f restPose = getRestPoseMatrix(region, entityData);
        return restPose.invert(new Matrix4f());
    }

    public Matrix4f getRestPoseMatrix(BoneRegion region, BipedEntityData<?> entityData)
    {
        resultMatrix.identity();

        ModelPartTransform transform = getTransformForRegion(region, entityData);
        if (transform != null)
        {
            tempVector.set(
                transform.position.x * MODEL_SCALE,
                transform.position.y * MODEL_SCALE,
                transform.position.z * MODEL_SCALE
            );
            resultMatrix.translate(tempVector);
        }

        return new Matrix4f(resultMatrix);
    }

    public Vector3f transformVertex(Vector3f vertex, BoneRegion fromRegion, BoneRegion toRegion,
                                   BipedEntityData<?> entityData)
    {
        Matrix4f fromTransform = getFullBoneTransform(fromRegion, entityData);
        Matrix4f toTransform = getFullBoneTransform(toRegion, entityData);

        Matrix4f toInverse = toTransform.invert(new Matrix4f());
        Matrix4f combined = toInverse.mul(fromTransform, new Matrix4f());

        Vector3f result = new Vector3f(vertex);
        return combined.transformPosition(result);
    }

    public void applyToPoseStack(PoseStack poseStack, ModelPartTransform transform)
    {
        if (transform == null) return;

        if (transform.globalOffset.x != 0 || transform.globalOffset.y != 0 || transform.globalOffset.z != 0)
        {
            poseStack.translate(
                transform.globalOffset.x * MODEL_SCALE,
                transform.globalOffset.y * MODEL_SCALE,
                transform.globalOffset.z * MODEL_SCALE
            );
        }

        if (transform.position.x != 0 || transform.position.y != 0 || transform.position.z != 0)
        {
            poseStack.translate(
                transform.position.x * MODEL_SCALE * transform.offsetScale,
                transform.position.y * MODEL_SCALE * transform.offsetScale,
                transform.position.z * MODEL_SCALE * transform.offsetScale
            );
        }

        if (transform.offset.x != 0 || transform.offset.y != 0 || transform.offset.z != 0)
        {
            poseStack.translate(
                transform.offset.x * MODEL_SCALE * transform.offsetScale,
                transform.offset.y * MODEL_SCALE * transform.offsetScale,
                transform.offset.z * MODEL_SCALE * transform.offsetScale
            );
        }

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

        if (transform.scale.x != 1 || transform.scale.y != 1 || transform.scale.z != 1)
        {
            poseStack.scale(transform.scale.x, transform.scale.y, transform.scale.z);
        }
    }

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

    private void applyTransform(Matrix4f matrix, ModelPartTransform transform)
    {
        if (transform == null) return;

        if (transform.globalOffset.x != 0 || transform.globalOffset.y != 0 || transform.globalOffset.z != 0)
        {
            tempVector.set(
                transform.globalOffset.x * MODEL_SCALE,
                transform.globalOffset.y * MODEL_SCALE,
                transform.globalOffset.z * MODEL_SCALE
            );
            matrix.translate(tempVector);
        }

        if (transform.position.x != 0 || transform.position.y != 0 || transform.position.z != 0)
        {
            tempVector.set(
                transform.position.x * MODEL_SCALE * transform.offsetScale,
                transform.position.y * MODEL_SCALE * transform.offsetScale,
                transform.position.z * MODEL_SCALE * transform.offsetScale
            );
            matrix.translate(tempVector);
        }

        if (transform.offset.x != 0 || transform.offset.y != 0 || transform.offset.z != 0)
        {
            tempVector.set(
                transform.offset.x * MODEL_SCALE * transform.offsetScale,
                transform.offset.y * MODEL_SCALE * transform.offsetScale,
                transform.offset.z * MODEL_SCALE * transform.offsetScale
            );
            matrix.translate(tempVector);
        }

        var smoothRot = transform.rotation.getSmooth();
        if (smoothRot != null)
        {
            tempQuat.set((float) smoothRot.x, (float) smoothRot.y, (float) smoothRot.z, (float) smoothRot.w);
            matrix.rotate(tempQuat);
        }

        if (transform.scale.x != 1 || transform.scale.y != 1 || transform.scale.z != 1)
        {
            matrix.scale(transform.scale.x, transform.scale.y, transform.scale.z);
        }
    }

    private Matrix4f buildTransformMatrix(ModelPartTransform transform)
    {
        tempMatrix.identity();
        applyTransform(tempMatrix, transform);
        return new Matrix4f(tempMatrix);
    }
}
