package goblinbob.mobends.standard.client.model.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import goblinbob.mobends.core.client.model.IModelPart;
import goblinbob.mobends.core.math.Quaternion;
import goblinbob.mobends.core.math.SmoothOrientation;
import goblinbob.mobends.core.math.vector.IVec3f;
import goblinbob.mobends.standard.data.BipedEntityData;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Renders armor using the rigid body approach with per-vertex bone assignment:
 * 1. Capture vertices from vanilla armor rendering (in rest pose)
 * 2. Assign EACH VERTEX to a bone based on its position
 * 3. Transform each vertex by its assigned bone's transform
 * 4. Output vertices in original order (preserving quad structure)
 *
 * This approach works with ANY armor - vanilla, modded, custom OBJ, etc.
 */
@OnlyIn(Dist.CLIENT)
public class RigidArmorRenderer
{
    private static final float SCALE = 1.0f / 16.0f;

    private final ArmorBoneAssignment boneAssignment;
    private final CapturingVertexConsumer captureConsumer;

    // Pre-computed transforms for each bone (updated per-frame)
    private final Map<BoneRegion, BoneTransform> boneTransforms = new EnumMap<>(BoneRegion.class);

    // Rest pose positions computed dynamically from entity data
    private final Map<BoneRegion, RestPosePosition> restPosePositions = new EnumMap<>(BoneRegion.class);

    public RigidArmorRenderer()
    {
        this.boneAssignment = new ArmorBoneAssignment();
        this.captureConsumer = new CapturingVertexConsumer();
    }

    /**
     * Get the capturing vertex consumer for intercepting armor rendering.
     */
    public CapturingVertexConsumer getCaptureConsumer()
    {
        captureConsumer.clear();
        return captureConsumer;
    }

    /**
     * Render captured vertices with Mo' Bends bone transforms.
     * Each vertex is independently assigned to a bone and transformed.
     *
     * @param poseStack The pose stack
     * @param outputConsumer The vertex consumer
     * @param packedLight Packed light value
     * @param packedOverlay Packed overlay value
     * @param data Entity animation data
     */
    public void renderCapturedVertices(
            PoseStack poseStack,
            VertexConsumer outputConsumer,
            int packedLight,
            int packedOverlay,
            BipedEntityData<?> data)
    {
        renderCapturedVertices(poseStack, outputConsumer, packedLight, packedOverlay, data, 1.0f, 0xFFFFFFFF);
    }

    /**
     * Render captured vertices with Mo' Bends bone transforms.
     * Each vertex is independently assigned to a bone and transformed.
     *
     * @param poseStack The pose stack
     * @param outputConsumer The vertex consumer
     * @param packedLight Packed light value
     * @param packedOverlay Packed overlay value
     * @param data Entity animation data
     * @param entityScale Scale factor (1.0 for adults, 0.5 for babies)
     */
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

    /**
     * Render captured vertices with Mo' Bends bone transforms.
     * Each vertex is independently assigned to a bone and transformed.
     *
     * @param poseStack The pose stack
     * @param outputConsumer The vertex consumer
     * @param packedLight Packed light value
     * @param packedOverlay Packed overlay value
     * @param data Entity animation data
     * @param entityScale Scale factor (1.0 for adults, 0.5 for babies)
     * @param armorColor ARGB color tint for leather armor (0xFFFFFFFF for no tint)
     */
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

        // Note: Global transforms (globalOffset, centerRotation, renderRotation, localOffset)
        // are already applied by MutatedRenderer.beforeRender() to the PoseStack before armor renders.
        poseStack.pushPose();

        // IMPORTANT: Compute rest positions FIRST, then transforms (which depend on rest positions)
        computeRestPosePositions(data);
        computeBoneTransforms(poseStack, data);

        // Transform and output each vertex individually
        for (CapturedVertex v : vertices)
        {
            // Assign this vertex to a bone based on its position
            BoneRegion region = boneAssignment.assignVertex(v.x, v.y, v.z);

            // Get the pre-computed transform for this bone
            BoneTransform transform = boneTransforms.get(region);
            RestPosePosition restPos = restPosePositions.get(region);
            if (transform == null || restPos == null)
            {
                // Fallback: output vertex unchanged
                // Apply armor color tint
                float tintR = ((armorColor >> 16) & 0xFF) / 255.0F;
                float tintG = ((armorColor >> 8) & 0xFF) / 255.0F;
                float tintB = (armorColor & 0xFF) / 255.0F;
                float tintA = ((armorColor >> 24) & 0xFF) / 255.0F;
                int color = ((int)(v.alpha * tintA * 255.0F) << 24) |
                            ((int)(v.red * tintR * 255.0F) << 16) |
                            ((int)(v.green * tintG * 255.0F) << 8) |
                            (int)(v.blue * tintB * 255.0F);
                outputConsumer.addVertex(v.x, v.y, v.z)
                        .setColor(color)
                        .setUv(v.u, v.v)
                        .setOverlay(packedOverlay)
                        .setLight(packedLight)
                        .setNormal(v.normalX, v.normalY, v.normalZ);
                continue;
            }

            // Convert to bone-local coordinates using dynamically computed rest position
            float localX = v.x - restPos.x;
            float localY = v.y - restPos.y;
            float localZ = v.z - restPos.z;

            // Transform position by bone matrix
            float tx = transform.m00 * localX + transform.m10 * localY + transform.m20 * localZ + transform.m30;
            float ty = transform.m01 * localX + transform.m11 * localY + transform.m21 * localZ + transform.m31;
            float tz = transform.m02 * localX + transform.m12 * localY + transform.m22 * localZ + transform.m32;

            // Transform normal by bone normal matrix
            float nx = transform.n00 * v.normalX + transform.n10 * v.normalY + transform.n20 * v.normalZ;
            float ny = transform.n01 * v.normalX + transform.n11 * v.normalY + transform.n21 * v.normalZ;
            float nz = transform.n02 * v.normalX + transform.n12 * v.normalY + transform.n22 * v.normalZ;

            // Output transformed vertex
            // Apply armor color tint
            float tintR = ((armorColor >> 16) & 0xFF) / 255.0F;
            float tintG = ((armorColor >> 8) & 0xFF) / 255.0F;
            float tintB = (armorColor & 0xFF) / 255.0F;
            float tintA = ((armorColor >> 24) & 0xFF) / 255.0F;
            int color = ((int)(v.alpha * tintA * 255.0F) << 24) |
                        ((int)(v.red * tintR * 255.0F) << 16) |
                        ((int)(v.green * tintG * 255.0F) << 8) |
                        (int)(v.blue * tintB * 255.0F);
            outputConsumer.addVertex(tx, ty, tz)
                    .setColor(color)
                    .setUv(v.u, v.v)
                    .setOverlay(packedOverlay)
                    .setLight(packedLight)
                    .setNormal(nx, ny, nz);
        }

        poseStack.popPose();
    }

    /**
     * Pre-compute transforms for all bones.
     *
     * CRITICAL INSIGHT: MoBends bone pivot positions don't always match vanilla ModelPart pivots!
     * For example, MoBends leftLeg pivot is at (0,12,0) but vanilla is at (1.9,12,0).
     *
     * Since captured vertices come from vanilla armor (using vanilla pivots), we MUST rotate
     * around vanilla pivot positions. Therefore, we cannot use MoBends' applyCharacterTransform
     * directly, as it would rotate around wrong pivots.
     *
     * Instead, we:
     * 1. Translate to vanilla rest position (the correct pivot for captured vertices)
     * 2. Apply ONLY the rotation from MoBends (accumulated through hierarchy)
     * 3. Also apply the animation offset from MoBends
     *
     * The transform is: translate(vanillaRestPos + animOffset) * rotation
     */
    private void computeBoneTransforms(PoseStack poseStack, BipedEntityData<?> data)
    {
        boneTransforms.clear();

        for (BoneRegion region : BoneRegion.values())
        {
            IModelPart part = getBoneModelPart(region, data);

            // Get vanilla rest position for this bone
            RestPosePosition restPos = restPosePositions.get(region);
            if (restPos == null)
            {
                continue;
            }

            poseStack.pushPose();

            // Translate to the vanilla rest position
            poseStack.translate(restPos.x, restPos.y, restPos.z);

            if (part != null)
            {
                // Apply animation offset from this bone
                IVec3f offset = part.getOffset();
                if (offset.getX() != 0 || offset.getY() != 0 || offset.getZ() != 0)
                {
                    poseStack.translate(offset.getX() * SCALE, offset.getY() * SCALE, offset.getZ() * SCALE);
                }

                // Apply the accumulated rotation through the bone hierarchy
                Quaternionf accumulatedRotation = getAccumulatedRotation(part);
                poseStack.mulPose(accumulatedRotation);
            }

            PoseStack.Pose pose = poseStack.last();
            Matrix4f matrix = pose.pose();
            Matrix3f normal = pose.normal();

            BoneTransform transform = new BoneTransform();
            // Copy position matrix
            transform.m00 = matrix.m00(); transform.m01 = matrix.m01(); transform.m02 = matrix.m02();
            transform.m10 = matrix.m10(); transform.m11 = matrix.m11(); transform.m12 = matrix.m12();
            transform.m20 = matrix.m20(); transform.m21 = matrix.m21(); transform.m22 = matrix.m22();
            transform.m30 = matrix.m30(); transform.m31 = matrix.m31(); transform.m32 = matrix.m32();
            // Copy normal matrix
            transform.n00 = normal.m00(); transform.n01 = normal.m01(); transform.n02 = normal.m02();
            transform.n10 = normal.m10(); transform.n11 = normal.m11(); transform.n12 = normal.m12();
            transform.n20 = normal.m20(); transform.n21 = normal.m21(); transform.n22 = normal.m22();

            boneTransforms.put(region, transform);

            poseStack.popPose();
        }
    }

    /**
     * Get the accumulated rotation through the bone hierarchy (parent * child order).
     * This extracts ONLY rotation, ignoring bone positions, because:
     * - MoBends bone positions differ from vanilla ModelPart pivots (especially legs)
     * - We need to rotate captured vanilla vertices around vanilla pivot positions
     */
    private Quaternionf getAccumulatedRotation(IModelPart part)
    {
        Quaternionf result = new Quaternionf(); // Identity

        // Build list from root to this part
        java.util.ArrayList<IModelPart> hierarchy = new java.util.ArrayList<>();
        IModelPart current = part;
        while (current != null)
        {
            hierarchy.add(0, current); // Add at beginning to get root-first order
            current = current.getParent();
        }

        // Multiply rotations in order (parent first)
        for (IModelPart p : hierarchy)
        {
            SmoothOrientation orientation = p.getRotation();
            if (orientation != null)
            {
                Quaternion q = orientation.getSmooth();
                if (q != null && !q.isIdentity())
                {
                    // Convert to JOML quaternion and multiply
                    Quaternionf jomlQ = new Quaternionf(q.x, q.y, q.z, q.w);
                    result.mul(jomlQ);
                }
            }
        }

        return result;
    }

    /**
     * Get the Mo' Bends model part for a bone region.
     */
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

    /**
     * Compute rest pose positions for all bones using VANILLA HumanoidModel positions.
     * This is critical: captured vertices come from vanilla armor models, so rest positions
     * must match where vanilla ModelParts place their geometry.
     *
     * Vanilla HumanoidModel part positions (in model units):
     * - head: (0, 0, 0)
     * - body: (0, 0, 0)
     * - leftArm: (5, 2, 0)  - pivot point
     * - rightArm: (-5, 2, 0) - pivot point
     * - leftLeg: (1.9, 12, 0) - pivot point
     * - rightLeg: (-1.9, 12, 0) - pivot point
     *
     * For upper/lower limb segments, we estimate midpoints.
     */
    private void computeRestPosePositions(BipedEntityData<?> data)
    {
        restPosePositions.clear();

        // Use vanilla HumanoidModel positions (in render units = model units * SCALE)

        // Head: pivot at (0, 0, 0)
        setRestPosition(BoneRegion.HEAD, 0, 0, 0);

        // Body: pivot at (0, 0, 0)
        setRestPosition(BoneRegion.BODY, 0, 0, 0);

        // Left arm: pivot at (5, 2, 0) in model units
        // Upper arm covers Y=0 to Y=6, center at Y=3
        setRestPosition(BoneRegion.LEFT_ARM_UPPER, 5, 2, 0);
        // Lower arm covers Y=6 to Y=12, center at Y=9
        setRestPosition(BoneRegion.LEFT_ARM_LOWER, 5, 8, 0);

        // Right arm: pivot at (-5, 2, 0) in model units
        setRestPosition(BoneRegion.RIGHT_ARM_UPPER, -5, 2, 0);
        setRestPosition(BoneRegion.RIGHT_ARM_LOWER, -5, 8, 0);

        // Left leg: pivot at (1.9, 12, 0) in model units
        // Upper leg covers Y=12 to Y=18
        setRestPosition(BoneRegion.LEFT_LEG_UPPER, 1.9f, 12, 0);
        // Lower leg covers Y=18 to Y=24
        setRestPosition(BoneRegion.LEFT_LEG_LOWER, 1.9f, 18, 0);

        // Right leg: pivot at (-1.9, 12, 0) in model units
        setRestPosition(BoneRegion.RIGHT_LEG_UPPER, -1.9f, 12, 0);
        setRestPosition(BoneRegion.RIGHT_LEG_LOWER, -1.9f, 18, 0);

        // ROOT at origin
        setRestPosition(BoneRegion.ROOT, 0, 0, 0);
    }

    /**
     * Helper to set rest position in model units (will be converted to render units).
     */
    private void setRestPosition(BoneRegion region, float x, float y, float z)
    {
        RestPosePosition pos = new RestPosePosition();
        pos.x = x * SCALE;
        pos.y = y * SCALE;
        pos.z = z * SCALE;
        restPosePositions.put(region, pos);
    }

    /**
     * Cached bone transform matrices to avoid recomputation per vertex.
     */
    private static class BoneTransform
    {
        // Position matrix (4x3, last row is always 0,0,0,1)
        float m00, m01, m02;
        float m10, m11, m12;
        float m20, m21, m22;
        float m30, m31, m32;

        // Normal matrix (3x3)
        float n00, n01, n02;
        float n10, n11, n12;
        float n20, n21, n22;
    }

    /**
     * Rest pose position for a bone (in render units, already scaled).
     */
    private static class RestPosePosition
    {
        float x, y, z;
    }
}
