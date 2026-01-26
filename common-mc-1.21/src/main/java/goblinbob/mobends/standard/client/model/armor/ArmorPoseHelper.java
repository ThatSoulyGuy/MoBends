package goblinbob.mobends.standard.client.model.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import goblinbob.mobends.api.rendering.IEntityVertexHelper;
import goblinbob.mobends.core.client.model.ModelPartTransform;
import goblinbob.mobends.core.util.GlHelper;
import goblinbob.mobends.standard.data.BipedEntityData;
import net.minecraft.client.model.geom.ModelPart;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

/**
 * Shared utility class for armor pose stack transformations.
 * Used by both Tier 1 and Tier 2 armor renderers to ensure consistent
 * transform application matching vanilla armor behavior.
 */
public final class ArmorPoseHelper
{
    /** Scale factor: 1 model unit = 1/16 render units */
    public static final float SCALE = 1.0f / 16.0f;

    private ArmorPoseHelper()
    {
        // Utility class - no instantiation
    }

    /**
     * Apply a Mo'Bends part transform to the PoseStack.
     * This syncs both positions and rotations to match the player model exactly.
     *
     * Transform order (matching CoordinateSpaceManager and player model):
     * 1. Per-part globalOffset (pre-parent offset, e.g., for special positioning)
     * 2. Position (pivot point, scaled by offsetScale)
     * 3. Offset (animation offset, scaled by offsetScale)
     * 4. Rotation
     *
     * @param poseStack The pose stack to modify
     * @param transform The Mo'Bends transform
     * @param isChildPart If true, applies position (for parts relative to parent). If false, skips position.
     */
    public static void applyPartTransform(PoseStack poseStack, ModelPartTransform transform, boolean isChildPart)
    {
        if (transform == null)
        {
            return;
        }

        float offsetScale = transform.offsetScale;

        // 1. Apply per-part globalOffset (pre-parent transform offset)
        if (transform.globalOffset.x != 0 || transform.globalOffset.y != 0 || transform.globalOffset.z != 0)
        {
            poseStack.translate(
                transform.globalOffset.x * SCALE,
                transform.globalOffset.y * SCALE,
                transform.globalOffset.z * SCALE
            );
        }

        // 2. Apply position (pivot point) - only for child parts relative to parent
        // Root parts (body, legs) use vanilla armor model positions
        if (isChildPart && (transform.position.x != 0 || transform.position.y != 0 || transform.position.z != 0))
        {
            poseStack.translate(
                transform.position.x * SCALE * offsetScale,
                transform.position.y * SCALE * offsetScale,
                transform.position.z * SCALE * offsetScale
            );
        }

        // 3. Apply animation offset (animated position change)
        if (transform.offset.x != 0 || transform.offset.y != 0 || transform.offset.z != 0)
        {
            poseStack.translate(
                transform.offset.x * SCALE * offsetScale,
                transform.offset.y * SCALE * offsetScale,
                transform.offset.z * SCALE * offsetScale
            );
        }

        // 4. Apply rotation
        GlHelper.rotate(poseStack, transform.rotation.getSmooth());
    }

    /**
     * Apply leg transform using the vanilla armor model's X position.
     * Legs are root parts that should use vanilla armor model positioning for X,
     * but Mo'Bends transforms for Y, Z, rotation, and animation offset.
     *
     * This ensures leg armor renders at the correct horizontal position for all
     * biped entities, regardless of what Mo'Bends entityData has configured.
     *
     * @param poseStack The pose stack to modify
     * @param transform The Mo'Bends leg transform
     * @param vanillaLegX The vanilla armor model's leg X position (typically ±1.9F)
     */
    public static void applyLegTransform(PoseStack poseStack, ModelPartTransform transform, float vanillaLegX)
    {
        if (transform == null)
        {
            return;
        }

        float offsetScale = transform.offsetScale;

        // 1. Apply per-part globalOffset (pre-parent transform offset)
        if (transform.globalOffset.x != 0 || transform.globalOffset.y != 0 || transform.globalOffset.z != 0)
        {
            poseStack.translate(
                transform.globalOffset.x * SCALE,
                transform.globalOffset.y * SCALE,
                transform.globalOffset.z * SCALE
            );
        }

        // 2. Apply position - use vanilla X for horizontal placement, Mo'Bends Y/Z for pivot
        // This ensures legs are horizontally positioned correctly using the vanilla armor model's
        // position, while still allowing Mo'Bends to control the vertical pivot point.
        poseStack.translate(
            vanillaLegX * SCALE * offsetScale,
            transform.position.y * SCALE * offsetScale,
            transform.position.z * SCALE * offsetScale
        );

        // 3. Apply animation offset (animated position change)
        if (transform.offset.x != 0 || transform.offset.y != 0 || transform.offset.z != 0)
        {
            poseStack.translate(
                transform.offset.x * SCALE * offsetScale,
                transform.offset.y * SCALE * offsetScale,
                transform.offset.z * SCALE * offsetScale
            );
        }

        // 4. Apply rotation
        GlHelper.rotate(poseStack, transform.rotation.getSmooth());
    }

    /**
     * Apply body transform with correct pivot point for rotation.
     * Mo'Bends rotates the body around body.position, so we need to:
     * 1. Translate to pivot
     * 2. Apply offset and rotation
     * 3. Translate back from pivot
     *
     * @param poseStack The pose stack to modify
     * @param entityData The entity's animation data
     */
    public static void applyBodyTransformWithPivot(PoseStack poseStack, BipedEntityData<?> entityData)
    {
        ModelPartTransform body = entityData.body;
        if (body == null)
        {
            return;
        }

        // Apply per-part globalOffset (before other transforms, without offsetScale)
        if (body.globalOffset.x != 0 || body.globalOffset.y != 0 || body.globalOffset.z != 0)
        {
            poseStack.translate(
                body.globalOffset.x * SCALE,
                body.globalOffset.y * SCALE,
                body.globalOffset.z * SCALE
            );
        }

        float offsetScale = body.offsetScale;

        // Translate to body pivot point (use offsetScale to match player transform exactly)
        poseStack.translate(
            body.position.x * SCALE * offsetScale,
            body.position.y * SCALE * offsetScale,
            body.position.z * SCALE * offsetScale
        );

        // Apply animation offset with offsetScale
        if (body.offset.x != 0 || body.offset.y != 0 || body.offset.z != 0)
        {
            poseStack.translate(
                body.offset.x * SCALE * offsetScale,
                body.offset.y * SCALE * offsetScale,
                body.offset.z * SCALE * offsetScale
            );
        }

        // Apply rotation (now around the correct pivot)
        GlHelper.rotate(poseStack, body.rotation.getSmooth());

        // Translate back from pivot so rendering happens at vanilla position (use offsetScale to match)
        poseStack.translate(
            -body.position.x * SCALE * offsetScale,
            -body.position.y * SCALE * offsetScale,
            -body.position.z * SCALE * offsetScale
        );
    }

    /**
     * Group captured vertices into quads (4 vertices each).
     * Minecraft renders quads, so every 4 consecutive vertices form a quad.
     *
     * @param vertices List of captured vertices
     * @return List of vertex arrays, each containing 4 vertices (one quad)
     */
    public static List<CapturedVertex[]> groupIntoQuads(List<CapturedVertex> vertices)
    {
        List<CapturedVertex[]> quads = new ArrayList<>();
        for (int i = 0; i + 3 < vertices.size(); i += 4)
        {
            quads.add(new CapturedVertex[] {
                vertices.get(i),
                vertices.get(i + 1),
                vertices.get(i + 2),
                vertices.get(i + 3)
            });
        }
        return quads;
    }

    /**
     * Render sliced geometry (upper or lower portion of quads).
     * Transforms vertices using current PoseStack and outputs to consumer.
     *
     * IMPORTANT: The VertexConsumer expects QUADS (4 vertices each), not triangles.
     * We must output vertices in groups of 4.
     *
     * @param poseStack The pose stack with current bone transform
     * @param consumer The vertex consumer to output to
     * @param sliceResults Results from QuadSlicer
     * @param renderUpper true to render upper portion, false for lower
     * @param offsetX X offset to apply to vertices before transform (for local space conversion)
     * @param offsetY Y offset to apply to vertices before transform (for local space conversion)
     * @param offsetZ Z offset to apply to vertices before transform (for local space conversion)
     * @param packedLight Light value
     * @param packedOverlay Overlay value
     * @param armorColor ARGB color for armor tinting (leather armor dyeing)
     */
    public static void renderSlicedVertices(
            PoseStack poseStack,
            VertexConsumer consumer,
            List<SliceResult> sliceResults,
            boolean renderUpper,
            float offsetX,
            float offsetY,
            float offsetZ,
            int packedLight,
            int packedOverlay,
            int armorColor)
    {
        Matrix4f matrix = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();

        for (SliceResult result : sliceResults)
        {
            List<SliceResult.SlicedVertex> vertices = renderUpper
                    ? result.getUpperVertices()
                    : result.getLowerVertices();

            if (vertices.isEmpty())
            {
                continue;
            }

            // Handle different vertex counts - RenderType expects QUADS (4 vertices each)
            int vertexCount = vertices.size();

            if (vertexCount == 4)
            {
                // Standard quad - output 4 vertices directly
                outputVertex(matrix, normal, consumer, vertices.get(0), offsetX, offsetY, offsetZ, packedLight, packedOverlay, armorColor);
                outputVertex(matrix, normal, consumer, vertices.get(1), offsetX, offsetY, offsetZ, packedLight, packedOverlay, armorColor);
                outputVertex(matrix, normal, consumer, vertices.get(2), offsetX, offsetY, offsetZ, packedLight, packedOverlay, armorColor);
                outputVertex(matrix, normal, consumer, vertices.get(3), offsetX, offsetY, offsetZ, packedLight, packedOverlay, armorColor);
            }
            else if (vertexCount == 3)
            {
                // Triangle - create degenerate quad by duplicating last vertex
                outputVertex(matrix, normal, consumer, vertices.get(0), offsetX, offsetY, offsetZ, packedLight, packedOverlay, armorColor);
                outputVertex(matrix, normal, consumer, vertices.get(1), offsetX, offsetY, offsetZ, packedLight, packedOverlay, armorColor);
                outputVertex(matrix, normal, consumer, vertices.get(2), offsetX, offsetY, offsetZ, packedLight, packedOverlay, armorColor);
                outputVertex(matrix, normal, consumer, vertices.get(2), offsetX, offsetY, offsetZ, packedLight, packedOverlay, armorColor); // Duplicate
            }
            else if (vertexCount == 5)
            {
                // Pentagon - split into 2 quads: (0,1,2,3) and (0,3,4,4)
                outputVertex(matrix, normal, consumer, vertices.get(0), offsetX, offsetY, offsetZ, packedLight, packedOverlay, armorColor);
                outputVertex(matrix, normal, consumer, vertices.get(1), offsetX, offsetY, offsetZ, packedLight, packedOverlay, armorColor);
                outputVertex(matrix, normal, consumer, vertices.get(2), offsetX, offsetY, offsetZ, packedLight, packedOverlay, armorColor);
                outputVertex(matrix, normal, consumer, vertices.get(3), offsetX, offsetY, offsetZ, packedLight, packedOverlay, armorColor);

                outputVertex(matrix, normal, consumer, vertices.get(0), offsetX, offsetY, offsetZ, packedLight, packedOverlay, armorColor);
                outputVertex(matrix, normal, consumer, vertices.get(3), offsetX, offsetY, offsetZ, packedLight, packedOverlay, armorColor);
                outputVertex(matrix, normal, consumer, vertices.get(4), offsetX, offsetY, offsetZ, packedLight, packedOverlay, armorColor);
                outputVertex(matrix, normal, consumer, vertices.get(4), offsetX, offsetY, offsetZ, packedLight, packedOverlay, armorColor); // Duplicate
            }
            else if (vertexCount >= 6)
            {
                // 6+ vertices - fan triangulation converted to quads
                // Each triangle (0, i, i+1) becomes a degenerate quad
                for (int i = 1; i < vertexCount - 1; i++)
                {
                    outputVertex(matrix, normal, consumer, vertices.get(0), offsetX, offsetY, offsetZ, packedLight, packedOverlay, armorColor);
                    outputVertex(matrix, normal, consumer, vertices.get(i), offsetX, offsetY, offsetZ, packedLight, packedOverlay, armorColor);
                    outputVertex(matrix, normal, consumer, vertices.get(i + 1), offsetX, offsetY, offsetZ, packedLight, packedOverlay, armorColor);
                    outputVertex(matrix, normal, consumer, vertices.get(i + 1), offsetX, offsetY, offsetZ, packedLight, packedOverlay, armorColor); // Duplicate
                }
            }
            // vertexCount < 3 is degenerate, skip
        }
    }

    /**
     * Output a single vertex, transformed by the given matrices.
     * Applies an offset to the vertex position before transformation (for local space conversion).
     *
     * @param matrix The position transformation matrix
     * @param normal The normal transformation matrix
     * @param consumer The vertex consumer to output to
     * @param v The sliced vertex data
     * @param offsetX X offset to apply before transform
     * @param offsetY Y offset to apply before transform
     * @param offsetZ Z offset to apply before transform
     * @param packedLight Light value
     * @param packedOverlay Overlay value
     * @param armorColor ARGB color for armor tinting
     */
    public static void outputVertex(
            Matrix4f matrix,
            Matrix3f normal,
            VertexConsumer consumer,
            SliceResult.SlicedVertex v,
            float offsetX,
            float offsetY,
            float offsetZ,
            int packedLight,
            int packedOverlay,
            int armorColor)
    {
        // Apply offset to vertex position (converts to local space for child bones)
        float vx = v.x + offsetX;
        float vy = v.y + offsetY;
        float vz = v.z + offsetZ;

        // Transform position by matrix
        float tx = matrix.m00() * vx + matrix.m10() * vy + matrix.m20() * vz + matrix.m30();
        float ty = matrix.m01() * vx + matrix.m11() * vy + matrix.m21() * vz + matrix.m31();
        float tz = matrix.m02() * vx + matrix.m12() * vy + matrix.m22() * vz + matrix.m32();

        // Transform normal by normal matrix
        float nx = normal.m00() * v.normalX + normal.m10() * v.normalY + normal.m20() * v.normalZ;
        float ny = normal.m01() * v.normalX + normal.m11() * v.normalY + normal.m21() * v.normalZ;
        float nz = normal.m02() * v.normalX + normal.m12() * v.normalY + normal.m22() * v.normalZ;

        // Apply armor color tint (for leather armor dyeing)
        // Multiply captured vertex color with armor color
        float tintR = ((armorColor >> 16) & 0xFF) / 255.0F;
        float tintG = ((armorColor >> 8) & 0xFF) / 255.0F;
        float tintB = (armorColor & 0xFF) / 255.0F;
        float tintA = ((armorColor >> 24) & 0xFF) / 255.0F;

        // Pack tinted RGBA into single int
        int color = ((int)(v.alpha * tintA * 255.0F) << 24) |
                    ((int)(v.red * tintR * 255.0F) << 16) |
                    ((int)(v.green * tintG * 255.0F) << 8) |
                    (int)(v.blue * tintB * 255.0F);
        IEntityVertexHelper.Holder.getHelper().emitVertex(consumer,
                tx, ty, tz, color, v.u, v.v,
                packedOverlay, packedLight, nx, ny, nz);
    }

    /**
     * Reset a ModelPart to origin for geometry capture.
     * Stores original values in the provided storage for later restoration.
     *
     * @param part The model part to reset
     * @param storage Array of size 6 to store [x, y, z, xRot, yRot, zRot]
     */
    public static void resetPartToOrigin(ModelPart part, float[] storage)
    {
        storage[0] = part.x;
        storage[1] = part.y;
        storage[2] = part.z;
        storage[3] = part.xRot;
        storage[4] = part.yRot;
        storage[5] = part.zRot;

        part.x = 0;
        part.y = 0;
        part.z = 0;
        part.xRot = 0;
        part.yRot = 0;
        part.zRot = 0;
    }

    /**
     * Restore a ModelPart from stored values after capture.
     *
     * @param part The model part to restore
     * @param storage Array containing [x, y, z, xRot, yRot, zRot]
     */
    public static void restorePartFromStorage(ModelPart part, float[] storage)
    {
        part.x = storage[0];
        part.y = storage[1];
        part.z = storage[2];
        part.xRot = storage[3];
        part.yRot = storage[4];
        part.zRot = storage[5];
    }

    /**
     * Render a ModelPart at the origin (without its own position/rotation).
     * Full Mo'Bends transform is already applied to PoseStack.
     * Used for arms, legs, head which use full Mo'Bends positioning.
     *
     * @param part The model part to render
     * @param poseStack The pose stack with transforms applied
     * @param vertexConsumer The vertex consumer
     * @param packedLight Light value
     * @param packedOverlay Overlay value
     */
    public static void renderPartAtOrigin(
            ModelPart part,
            PoseStack poseStack,
            VertexConsumer vertexConsumer,
            int packedLight,
            int packedOverlay)
    {
        if (part == null || !part.visible)
        {
            return;
        }

        // Store original values
        float origX = part.x, origY = part.y, origZ = part.z;
        float origXRot = part.xRot, origYRot = part.yRot, origZRot = part.zRot;

        // Reset to origin - full transform already applied via PoseStack
        part.x = 0;
        part.y = 0;
        part.z = 0;
        part.xRot = 0;
        part.yRot = 0;
        part.zRot = 0;

        // Render
        part.render(poseStack, vertexConsumer, packedLight, packedOverlay);

        // Restore original values
        part.x = origX;
        part.y = origY;
        part.z = origZ;
        part.xRot = origXRot;
        part.yRot = origYRot;
        part.zRot = origZRot;
    }

    /**
     * Render a ModelPart keeping its vanilla position but without rotation.
     * Mo'Bends rotation is already applied to PoseStack.
     * Used for body/waist which should stay at vanilla position.
     *
     * @param part The model part to render
     * @param poseStack The pose stack with rotation applied
     * @param vertexConsumer The vertex consumer
     * @param packedLight Light value
     * @param packedOverlay Overlay value
     */
    public static void renderPartWithVanillaPosition(
            ModelPart part,
            PoseStack poseStack,
            VertexConsumer vertexConsumer,
            int packedLight,
            int packedOverlay)
    {
        if (part == null || !part.visible)
        {
            return;
        }

        // Store original rotation
        float origXRot = part.xRot, origYRot = part.yRot, origZRot = part.zRot;

        // Reset rotation to identity - Mo'Bends rotation is on PoseStack
        // Keep vanilla position (x, y, z) so armor renders at correct location
        part.xRot = 0;
        part.yRot = 0;
        part.zRot = 0;

        // Render
        part.render(poseStack, vertexConsumer, packedLight, packedOverlay);

        // Restore original rotation
        part.xRot = origXRot;
        part.yRot = origYRot;
        part.zRot = origZRot;
    }
}
