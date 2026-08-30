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

    private net.minecraft.world.entity.EquipmentSlot slotHint = null;

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
        renderCapturedVertices(poseStack, outputConsumer, packedLight, packedOverlay, data, 0xFFFFFFFF);
    }

    public void renderCapturedVertices(
            PoseStack poseStack,
            VertexConsumer outputConsumer,
            int packedLight,
            int packedOverlay,
            BipedEntityData<?> data,
            net.minecraft.world.entity.EquipmentSlot slot)
    {
        this.slotHint = slot;
        try
        {
            renderCapturedVertices(poseStack, outputConsumer, packedLight, packedOverlay, data, 0xFFFFFFFF);
        }
        finally
        {
            this.slotHint = null;
        }
    }

    public void renderCapturedVertices(
            PoseStack poseStack,
            VertexConsumer outputConsumer,
            int packedLight,
            int packedOverlay,
            BipedEntityData<?> data,
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

        final int vertexCount = vertices.size();
        final boolean quadAligned = vertexCount % 4 == 0;

        for (int index = 0; index < vertexCount; ++index)
        {
            CapturedVertex v = vertices.get(index);

            BoneRegion region = quadAligned
                    ? assignQuad(vertices, index - (index % 4))
                    : assignSingle(v);

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

    public void renderTaggedVertices(
            PoseStack poseStack,
            VertexConsumer outputConsumer,
            int packedLight,
            int packedOverlay,
            BipedEntityData<?> data,
            List<CapturedVertex> vertices,
            List<BoneRegion> regions,
            List<BoneRegion> blendRegions,
            List<Float> blendWeights)
    {
        if (vertices.isEmpty())
        {
            return;
        }

        poseStack.pushPose();

        computeRestPosePositions(data);
        computeBoneTransforms(poseStack, data);

        for (int index = 0; index < vertices.size(); ++index)
        {
            CapturedVertex v = vertices.get(index);
            BoneRegion region = regions.get(index);

            BoneTransform transform = boneTransforms.get(region);
            RestPosePosition restPos = restPosePositions.get(region);

            if (transform == null || restPos == null)
            {
                IEntityVertexHelper.Holder.getHelper().emitVertex(outputConsumer,
                        v.x, v.y, v.z, 0xFFFFFFFF, v.u, v.v,
                        packedOverlay, packedLight, v.normalX, v.normalY, v.normalZ);
                continue;
            }

            final float clearedX = BodyClearance.clearX(region, v.x);
            final float clearedZ = BodyClearance.clearZ(region, v.z);

            float localX = clearedX - restPos.x;
            float localY = v.y - restPos.y;
            float localZ = clearedZ - restPos.z;

            float tx = transform.m00 * localX + transform.m10 * localY + transform.m20 * localZ + transform.m30;
            float ty = transform.m01 * localX + transform.m11 * localY + transform.m21 * localZ + transform.m31;
            float tz = transform.m02 * localX + transform.m12 * localY + transform.m22 * localZ + transform.m32;

            float nx = transform.n00 * v.normalX + transform.n10 * v.normalY + transform.n20 * v.normalZ;
            float ny = transform.n01 * v.normalX + transform.n11 * v.normalY + transform.n21 * v.normalZ;
            float nz = transform.n02 * v.normalX + transform.n12 * v.normalY + transform.n22 * v.normalZ;

            float weight = blendWeights.get(index);
            if (weight > 0.0F)
            {
                BoneRegion other = blendRegions.get(index);
                BoneTransform t2 = boneTransforms.get(other);
                RestPosePosition rest2 = restPosePositions.get(other);

                if (t2 != null && rest2 != null)
                {
                    float lx2 = clearedX - rest2.x;
                    float ly2 = v.y - rest2.y;
                    float lz2 = clearedZ - rest2.z;

                    float bx = t2.m00 * lx2 + t2.m10 * ly2 + t2.m20 * lz2 + t2.m30;
                    float by = t2.m01 * lx2 + t2.m11 * ly2 + t2.m21 * lz2 + t2.m31;
                    float bz = t2.m02 * lx2 + t2.m12 * ly2 + t2.m22 * lz2 + t2.m32;

                    float bnx = t2.n00 * v.normalX + t2.n10 * v.normalY + t2.n20 * v.normalZ;
                    float bny = t2.n01 * v.normalX + t2.n11 * v.normalY + t2.n21 * v.normalZ;
                    float bnz = t2.n02 * v.normalX + t2.n12 * v.normalY + t2.n22 * v.normalZ;

                    tx += (bx - tx) * weight;
                    ty += (by - ty) * weight;
                    tz += (bz - tz) * weight;

                    nx += (bnx - nx) * weight;
                    ny += (bny - ny) * weight;
                    nz += (bnz - nz) * weight;
                }
            }

            int color = ((int)(v.alpha * 255.0F) << 24) |
                        ((int)(v.red * 255.0F) << 16) |
                        ((int)(v.green * 255.0F) << 8) |
                        (int)(v.blue * 255.0F);

            IEntityVertexHelper.Holder.getHelper().emitVertex(outputConsumer,
                    tx, ty, tz, color, v.u, v.v,
                    packedOverlay, packedLight, nx, ny, nz);
        }

        poseStack.popPose();
    }

    private BoneRegion assignSingle(CapturedVertex v)
    {
        return slotHint != null
                ? boneAssignment.assignVertexForSlot(v.x, v.y, v.z, slotHint)
                : boneAssignment.assignVertex(v.x, v.y, v.z);
    }

    private BoneRegion assignQuad(List<CapturedVertex> vertices, int quadStart)
    {
        float centreX = 0.0F;
        float centreY = 0.0F;
        float centreZ = 0.0F;

        for (int i = quadStart; i < quadStart + 4; ++i)
        {
            CapturedVertex v = vertices.get(i);
            centreX += v.x;
            centreY += v.y;
            centreZ += v.z;
        }

        centreX *= 0.25F;
        centreY *= 0.25F;
        centreZ *= 0.25F;

        return slotHint != null
                ? boneAssignment.assignVertexForSlot(centreX, centreY, centreZ, slotHint)
                : boneAssignment.assignVertex(centreX, centreY, centreZ);
    }

    private void computeBoneTransforms(PoseStack poseStack, BipedEntityData<?> data)
    {
        boneTransforms.clear();

        for (BoneRegion region : BoneRegion.values())
        {
            IModelPart part = getBoneModelPart(region, data);
            if (part == null)
            {
                continue;
            }

            poseStack.pushPose();

            part.applyCharacterTransform(poseStack, SCALE);

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

        for (BoneRegion region : BoneRegion.values())
        {
            IModelPart part = getBoneModelPart(region, data);
            if (part == null)
            {
                continue;
            }

            float[] accumulated = new float[3];
            accumulateRestPosition(part, accumulated);

            RestPosePosition pos = new RestPosePosition();
            pos.x = accumulated[0] * SCALE;
            pos.y = accumulated[1] * SCALE;
            pos.z = accumulated[2] * SCALE;
            restPosePositions.put(region, pos);
        }
    }

    private static void accumulateRestPosition(IModelPart part, float[] out)
    {
        if (part == null)
        {
            return;
        }

        accumulateRestPosition(part.getParent(), out);

        IVec3f position = part.getPosition();
        IVec3f offset = part.getOffset();

        out[0] += position.getX() + offset.getX();
        out[1] += position.getY() + offset.getY();
        out[2] += position.getZ() + offset.getZ();
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
