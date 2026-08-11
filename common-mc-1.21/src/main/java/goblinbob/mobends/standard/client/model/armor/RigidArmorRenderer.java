package goblinbob.mobends.standard.client.model.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import goblinbob.mobends.api.rendering.IEntityVertexHelper;
import goblinbob.mobends.core.client.model.IModelPart;
import goblinbob.mobends.lib.math.Quaternion;
import goblinbob.mobends.lib.math.SmoothOrientation;
import goblinbob.mobends.lib.math.vector.IVec3f;
import goblinbob.mobends.standard.data.BipedEntityData;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class RigidArmorRenderer
{
    private static final float SCALE = 1.0f / 16.0f;

    private final ArmorBoneAssignment boneAssignment;
    private final CapturingVertexConsumer captureConsumer;

    private final Map<BoneRegion, BoneTransform> boneTransforms = new EnumMap<>(BoneRegion.class);

    private final Map<BoneRegion, RestPosePosition> restPosePositions = new EnumMap<>(BoneRegion.class);

    public RigidArmorRenderer()
    {
        this.boneAssignment = new ArmorBoneAssignment();
        this.captureConsumer = new CapturingVertexConsumer();
    }

    public CapturingVertexConsumer getCaptureConsumer()
    {
        captureConsumer.clear();
        return captureConsumer;
    }

    public void renderCapturedVertices(
            PoseStack poseStack,
            VertexConsumer outputConsumer,
            int packedLight,
            int packedOverlay,
            BipedEntityData<?> data)
    {
        renderCapturedVertices(poseStack, outputConsumer, packedLight, packedOverlay, data, 1.0f, 0xFFFFFFFF);
    }

    public void renderCapturedVertices(
            PoseStack poseStack,
            VertexConsumer outputConsumer,
            int packedLight,
            int packedOverlay,
            BipedEntityData<?> data,
            float entityScale)
    {
        renderCapturedVertices(poseStack, outputConsumer, packedLight, packedOverlay, data, entityScale, 0xFFFFFFFF);
    }

    public void renderCapturedVertices(
            PoseStack poseStack,
            VertexConsumer outputConsumer,
            int packedLight,
            int packedOverlay,
            BipedEntityData<?> data,
            float entityScale,
            int armorColor)
    {
        List<CapturedVertex> vertices = captureConsumer.getVertices();
        if (vertices.isEmpty())
        {
            return;
        }

        poseStack.pushPose();

        computeRestPosePositions(data);
        computeBoneTransforms(poseStack, data);

        for (CapturedVertex v : vertices)
        {
            BoneRegion region = boneAssignment.assignVertex(v.x, v.y, v.z);

            BoneTransform transform = boneTransforms.get(region);
            RestPosePosition restPos = restPosePositions.get(region);
            if (transform == null || restPos == null)
            {
                float tintR = ((armorColor >> 16) & 0xFF) / 255.0F;
                float tintG = ((armorColor >> 8) & 0xFF) / 255.0F;
                float tintB = (armorColor & 0xFF) / 255.0F;
                float tintA = ((armorColor >> 24) & 0xFF) / 255.0F;
                int color = ((int)(v.alpha * tintA * 255.0F) << 24) |
                            ((int)(v.red * tintR * 255.0F) << 16) |
                            ((int)(v.green * tintG * 255.0F) << 8) |
                            (int)(v.blue * tintB * 255.0F);
                IEntityVertexHelper.Holder.getHelper().emitVertex(outputConsumer,
                        v.x, v.y, v.z,
                        color,
                        v.u, v.v,
                        packedOverlay, packedLight,
                        v.normalX, v.normalY, v.normalZ);
                continue;
            }

            float localX = v.x - restPos.x;
            float localY = v.y - restPos.y;
            float localZ = v.z - restPos.z;

            float tx = transform.m00 * localX + transform.m10 * localY + transform.m20 * localZ + transform.m30;
            float ty = transform.m01 * localX + transform.m11 * localY + transform.m21 * localZ + transform.m31;
            float tz = transform.m02 * localX + transform.m12 * localY + transform.m22 * localZ + transform.m32;

            float nx = transform.n00 * v.normalX + transform.n10 * v.normalY + transform.n20 * v.normalZ;
            float ny = transform.n01 * v.normalX + transform.n11 * v.normalY + transform.n21 * v.normalZ;
            float nz = transform.n02 * v.normalX + transform.n12 * v.normalY + transform.n22 * v.normalZ;

            float tintR = ((armorColor >> 16) & 0xFF) / 255.0F;
            float tintG = ((armorColor >> 8) & 0xFF) / 255.0F;
            float tintB = (armorColor & 0xFF) / 255.0F;
            float tintA = ((armorColor >> 24) & 0xFF) / 255.0F;
            int color = ((int)(v.alpha * tintA * 255.0F) << 24) |
                        ((int)(v.red * tintR * 255.0F) << 16) |
                        ((int)(v.green * tintG * 255.0F) << 8) |
                        (int)(v.blue * tintB * 255.0F);
            IEntityVertexHelper.Holder.getHelper().emitVertex(outputConsumer,
                    tx, ty, tz,
                    color,
                    v.u, v.v,
                    packedOverlay, packedLight,
                    nx, ny, nz);
        }

        poseStack.popPose();
    }

    private void computeBoneTransforms(PoseStack poseStack, BipedEntityData<?> data)
    {
        boneTransforms.clear();

        for (BoneRegion region : BoneRegion.values())
        {
            IModelPart part = getBoneModelPart(region, data);

            RestPosePosition restPos = restPosePositions.get(region);
            if (restPos == null)
            {
                continue;
            }

            poseStack.pushPose();

            poseStack.translate(restPos.x, restPos.y, restPos.z);

            if (part != null)
            {
                IVec3f offset = part.getOffset();
                if (offset.getX() != 0 || offset.getY() != 0 || offset.getZ() != 0)
                {
                    poseStack.translate(offset.getX() * SCALE, offset.getY() * SCALE, offset.getZ() * SCALE);
                }

                Quaternionf accumulatedRotation = getAccumulatedRotation(part);
                poseStack.mulPose(accumulatedRotation);
            }

            PoseStack.Pose pose = poseStack.last();
            Matrix4f matrix = pose.pose();
            Matrix3f normal = pose.normal();

            BoneTransform transform = new BoneTransform();
            transform.m00 = matrix.m00(); transform.m01 = matrix.m01(); transform.m02 = matrix.m02();
            transform.m10 = matrix.m10(); transform.m11 = matrix.m11(); transform.m12 = matrix.m12();
            transform.m20 = matrix.m20(); transform.m21 = matrix.m21(); transform.m22 = matrix.m22();
            transform.m30 = matrix.m30(); transform.m31 = matrix.m31(); transform.m32 = matrix.m32();
            transform.n00 = normal.m00(); transform.n01 = normal.m01(); transform.n02 = normal.m02();
            transform.n10 = normal.m10(); transform.n11 = normal.m11(); transform.n12 = normal.m12();
            transform.n20 = normal.m20(); transform.n21 = normal.m21(); transform.n22 = normal.m22();

            boneTransforms.put(region, transform);

            poseStack.popPose();
        }
    }

    private Quaternionf getAccumulatedRotation(IModelPart part)
    {
        Quaternionf result = new Quaternionf();

        java.util.ArrayList<IModelPart> hierarchy = new java.util.ArrayList<>();
        IModelPart current = part;
        while (current != null)
        {
            hierarchy.add(0, current);
            current = current.getParent();
        }

        for (IModelPart p : hierarchy)
        {
            SmoothOrientation orientation = p.getRotation();
            if (orientation != null)
            {
                Quaternion q = orientation.getSmooth();
                if (q != null && !q.isIdentity())
                {
                    Quaternionf jomlQ = new Quaternionf(q.x, q.y, q.z, q.w);
                    result.mul(jomlQ);
                }
            }
        }

        return result;
    }

    private IModelPart getBoneModelPart(BoneRegion region, BipedEntityData<?> data)
    {
        switch (region)
        {
            case HEAD:
                return data.head;
            case BODY:
                return data.body;
            case LEFT_ARM_UPPER:
                return data.leftArm;
            case LEFT_ARM_LOWER:
                return data.leftForeArm;
            case RIGHT_ARM_UPPER:
                return data.rightArm;
            case RIGHT_ARM_LOWER:
                return data.rightForeArm;
            case LEFT_LEG_UPPER:
                return data.leftLeg;
            case LEFT_LEG_LOWER:
                return data.leftForeLeg;
            case RIGHT_LEG_UPPER:
                return data.rightLeg;
            case RIGHT_LEG_LOWER:
                return data.rightForeLeg;
            case ROOT:
            default:
                return null;
        }
    }

    private void computeRestPosePositions(BipedEntityData<?> data)
    {
        restPosePositions.clear();

        setRestPosition(BoneRegion.HEAD, 0, 0, 0);

        setRestPosition(BoneRegion.BODY, 0, 0, 0);

        setRestPosition(BoneRegion.LEFT_ARM_UPPER, 5, 2, 0);
        setRestPosition(BoneRegion.LEFT_ARM_LOWER, 5, 8, 0);

        setRestPosition(BoneRegion.RIGHT_ARM_UPPER, -5, 2, 0);
        setRestPosition(BoneRegion.RIGHT_ARM_LOWER, -5, 8, 0);

        setRestPosition(BoneRegion.LEFT_LEG_UPPER, 1.9f, 12, 0);
        setRestPosition(BoneRegion.LEFT_LEG_LOWER, 1.9f, 18, 0);

        setRestPosition(BoneRegion.RIGHT_LEG_UPPER, -1.9f, 12, 0);
        setRestPosition(BoneRegion.RIGHT_LEG_LOWER, -1.9f, 18, 0);

        setRestPosition(BoneRegion.ROOT, 0, 0, 0);
    }

    private void setRestPosition(BoneRegion region, float x, float y, float z)
    {
        RestPosePosition pos = new RestPosePosition();
        pos.x = x * SCALE;
        pos.y = y * SCALE;
        pos.z = z * SCALE;
        restPosePositions.put(region, pos);
    }

    private static class BoneTransform
    {
        float m00, m01, m02;
        float m10, m11, m12;
        float m20, m21, m22;
        float m30, m31, m32;

        float n00, n01, n02;
        float n10, n11, n12;
        float n20, n21, n22;
    }

    private static class RestPosePosition
    {
        float x, y, z;
    }
}
