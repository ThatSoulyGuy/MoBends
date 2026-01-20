package goblinbob.mobends.standard.client.model.armor.tier1;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import goblinbob.mobends.core.client.model.ModelPartTransform;
import goblinbob.mobends.core.util.GlHelper;
import goblinbob.mobends.standard.client.model.armor.ArmorRenderContext;
import goblinbob.mobends.standard.client.model.armor.CapturedVertex;
import goblinbob.mobends.standard.client.model.armor.CapturingVertexConsumer;
import goblinbob.mobends.standard.client.model.armor.JointDefinitions;
import goblinbob.mobends.standard.client.model.armor.JointPlane;
import goblinbob.mobends.standard.client.model.armor.QuadSlicer;
import goblinbob.mobends.standard.client.model.armor.SliceResult;
import goblinbob.mobends.standard.client.model.armor.cache.CacheManager;
import goblinbob.mobends.standard.client.model.armor.tier.RenderTier;
import goblinbob.mobends.standard.client.model.armor.tier2.Tier2Renderer;
import goblinbob.mobends.standard.data.BipedEntityData;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Tier 1 renderer: Transform Injection.
 *
 * This is the most efficient rendering approach, used for ~85% of armor.
 * It works with vanilla and standard modded armor that uses HumanoidModel.
 *
 * Approach:
 * 1. Inject Mo'Bends transforms directly into the armor model's parts
 * 2. For limbs, use split rendering to handle elbow/knee bending
 * 3. Render using vanilla's rendering path with modified transforms
 * 4. Restore original transforms after rendering
 *
 * Falls back to Tier 2 if the model doesn't have expected structure.
 */
@OnlyIn(Dist.CLIENT)
public class Tier1Renderer
{
    private final TransformInjector transformInjector;
    private final SplitLimbRenderer splitLimbRenderer;
    private final Tier2Renderer tier2Fallback;

    // Store original part states for restoration
    private final PartStateStorage partStateStorage = new PartStateStorage();

    // Vertex capture and slicing for limb joint deformation
    private final CapturingVertexConsumer limbCapture = new CapturingVertexConsumer();
    private final QuadSlicer quadSlicer = new QuadSlicer();

    // Performance tracking
    private long renderCount = 0;
    private long fallbackCount = 0;

    public Tier1Renderer()
    {
        this.transformInjector = new TransformInjector();
        this.splitLimbRenderer = new SplitLimbRenderer();
        this.tier2Fallback = new Tier2Renderer();
    }

    public Tier1Renderer(TransformInjector transformInjector, SplitLimbRenderer splitLimbRenderer, Tier2Renderer tier2Fallback)
    {
        this.transformInjector = transformInjector;
        this.splitLimbRenderer = splitLimbRenderer;
        this.tier2Fallback = tier2Fallback;
    }

    /**
     * Get the tier this renderer handles.
     */
    public RenderTier getTier()
    {
        return RenderTier.TIER_1_TRANSFORM_INJECTION;
    }

    /**
     * Simple render method for facade integration.
     * Returns true if rendering was handled, false to indicate fallback needed.
     */
    public <E extends LivingEntity> boolean render(ArmorRenderContext<E> context, HumanoidModel<?> model)
    {
        if (context == null || model == null || context.getEntityData() == null)
        {
            return false;
        }

        // We need a texture to render - check if it can be derived from context
        // For now, return false to indicate caller should use renderWithTexture
        return false;
    }

    /**
     * Render armor with a specific texture.
     * Returns true if rendering was handled, false to indicate fallback needed.
     */
    public <E extends LivingEntity> boolean renderWithTexture(
            ArmorRenderContext<E> context,
            HumanoidModel<?> model,
            ResourceLocation texture,
            boolean hasFoil)
    {
        if (context == null || model == null || context.getEntityData() == null || texture == null)
        {
            return false;
        }

        try
        {
            render(context, model, texture, tex ->
                    hasFoil ? RenderType.armorCutoutNoCull(tex) : RenderType.armorCutoutNoCull(tex));
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    /**
     * Render armor using the transform injection approach.
     *
     * @param context The armor render context
     * @param model The armor model to render
     * @param texture The armor texture
     * @param renderTypeProvider Function to get RenderType from texture
     * @param <E> Entity type
     */
    public <E extends LivingEntity> void render(
            ArmorRenderContext<E> context,
            Model model,
            ResourceLocation texture,
            Function<ResourceLocation, RenderType> renderTypeProvider)
    {
        // Verify we have a HumanoidModel
        if (!(model instanceof HumanoidModel<?> humanoidModel))
        {
            // Fall back to Tier 2
            fallbackCount++;
            tier2Fallback.render(context, model, texture, renderTypeProvider);
            return;
        }

        if (context.getEntityData() == null)
        {
            // No animation data - render vanilla
            renderVanilla(context, humanoidModel, texture, renderTypeProvider);
            return;
        }

        renderCount++;

        BipedEntityData<?> entityData = context.getEntityData();
        PoseStack poseStack = context.getPoseStack();
        MultiBufferSource bufferSource = context.getBufferSource();
        EquipmentSlot slot = context.getSlot();

        // Configure visibility based on slot
        configureVisibility(humanoidModel, slot);

        RenderType renderType = renderTypeProvider.apply(texture);
        VertexConsumer vertexConsumer = bufferSource.getBuffer(renderType);

        // Get entity scale (1.0 for adults, 0.5 for babies)
        float entityScale = context.getEntityScale();

        // Get slim arm flag for arm position correction
        boolean isSlimArms = context.isSlimArms();

        // Render based on slot - each method applies proper hierarchical transforms
        switch (slot)
        {
            case HEAD:
                renderHead(poseStack, vertexConsumer, humanoidModel, entityData, context.getPackedLight(), context.getPackedOverlay(), entityScale);
                break;
            case CHEST:
                renderChest(poseStack, vertexConsumer, humanoidModel, entityData, context.getPackedLight(), context.getPackedOverlay(), entityScale, isSlimArms);
                break;
            case LEGS:
                renderLegs(poseStack, vertexConsumer, humanoidModel, entityData, context.getPackedLight(), context.getPackedOverlay(), entityScale);
                break;
            case FEET:
                renderFeet(poseStack, vertexConsumer, humanoidModel, entityData, context.getPackedLight(), context.getPackedOverlay(), entityScale);
                break;
        }

        // Record cache statistics
        CacheManager.getInstance().recordCacheAssistedRender();
    }

    private static final float SCALE = 1.0f / 16.0f;

    /**
     * Render head armor piece.
     * Head is parented to body, so we apply body transform first, then head transform.
     *
     * Uses vertex capture approach (like arms) for consistent transform handling.
     */
    private void renderHead(
            PoseStack poseStack,
            VertexConsumer vertexConsumer,
            HumanoidModel<?> model,
            BipedEntityData<?> entityData,
            int packedLight,
            int packedOverlay,
            float entityScale)
    {
        poseStack.pushPose();

        // Apply baby scale if needed (babies render at 0.5 scale)
        if (entityScale != 1.0f)
        {
            poseStack.scale(entityScale, entityScale, entityScale);
        }

        // Render helmet
        if (model.head != null && model.head.visible)
        {
            renderCapturedPart(poseStack, vertexConsumer, model.head, entityData, true, packedLight, packedOverlay, entityScale);
        }

        // Render hat overlay
        if (model.hat != null && model.hat.visible)
        {
            renderCapturedPart(poseStack, vertexConsumer, model.hat, entityData, true, packedLight, packedOverlay, entityScale);
        }

        poseStack.popPose();
    }

    /**
     * Render a part using vertex capture approach (consistent with arm/leg rendering).
     * This captures geometry at rest pose, then transforms vertices using Mo'Bends data.
     *
     * @param poseStack The pose stack
     * @param vertexConsumer The vertex consumer
     * @param part The model part to render
     * @param entityData The entity's animation data
     * @param isHead true for head parts (uses body + head transform), false otherwise
     * @param packedLight Light value
     * @param packedOverlay Overlay value
     * @param entityScale Scale factor (1.0 for adults, 0.5 for babies)
     */
    private void renderCapturedPart(
            PoseStack poseStack,
            VertexConsumer vertexConsumer,
            ModelPart part,
            BipedEntityData<?> entityData,
            boolean isHead,
            int packedLight,
            int packedOverlay,
            float entityScale)
    {
        if (part == null || !part.visible)
        {
            return;
        }

        // 1. Capture geometry at rest pose using IDENTITY PoseStack
        limbCapture.clear();
        PoseStack captureStack = new PoseStack();  // Identity matrix
        resetPartToOrigin(part);
        part.render(captureStack, limbCapture, packedLight, packedOverlay);
        restorePartFromCapture(part);

        List<CapturedVertex> vertices = limbCapture.getVertices();
        if (vertices.isEmpty())
        {
            return;
        }

        // 2. Apply Mo'Bends transforms to PoseStack
        poseStack.pushPose();
        applyGlobalOffset(poseStack, entityData, entityScale);
        applyPartTransform(poseStack, entityData.body, true);  // Body transform (head is parented to body)

        if (isHead)
        {
            applyPartTransform(poseStack, entityData.head, true);  // Head transform
        }

        // 3. Transform and output vertices
        Matrix4f matrix = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();

        // Output vertices directly (head has no slicing, just quads)
        for (CapturedVertex v : vertices)
        {
            // Transform position by matrix
            float tx = matrix.m00() * v.x + matrix.m10() * v.y + matrix.m20() * v.z + matrix.m30();
            float ty = matrix.m01() * v.x + matrix.m11() * v.y + matrix.m21() * v.z + matrix.m31();
            float tz = matrix.m02() * v.x + matrix.m12() * v.y + matrix.m22() * v.z + matrix.m32();

            // Transform normal by normal matrix
            float nx = normal.m00() * v.normalX + normal.m10() * v.normalY + normal.m20() * v.normalZ;
            float ny = normal.m01() * v.normalX + normal.m11() * v.normalY + normal.m21() * v.normalZ;
            float nz = normal.m02() * v.normalX + normal.m12() * v.normalY + normal.m22() * v.normalZ;

            // Output transformed vertex
            vertexConsumer.vertex(
                    tx, ty, tz,
                    v.red, v.green, v.blue, v.alpha,
                    v.u, v.v,
                    packedOverlay, packedLight,
                    nx, ny, nz
            );
        }

        poseStack.popPose();
    }

    /**
     * Render chest armor piece (body + arms).
     * Body uses vanilla position with Mo'Bends rotation.
     * Arms are split at elbow joint for proper bending.
     */
    private void renderChest(
            PoseStack poseStack,
            VertexConsumer vertexConsumer,
            HumanoidModel<?> model,
            BipedEntityData<?> entityData,
            int packedLight,
            int packedOverlay,
            float entityScale,
            boolean isSlimArms)
    {
        poseStack.pushPose();

        // Apply baby scale if needed (babies render at 0.5 scale)
        if (entityScale != 1.0f)
        {
            poseStack.scale(entityScale, entityScale, entityScale);
        }

        // Render body - apply rotation around correct pivot, keep vanilla position
        poseStack.pushPose();
        applyGlobalOffset(poseStack, entityData, entityScale);
        applyBodyTransformWithPivot(poseStack, entityData);
        renderPartWithVanillaPosition(model.body, poseStack, vertexConsumer, packedLight, packedOverlay);
        poseStack.popPose();

        // Render left arm (split at elbow joint)
        renderSplitArm(poseStack, vertexConsumer, model, entityData, true, packedLight, packedOverlay, entityScale, isSlimArms);

        // Render right arm (split at elbow joint)
        renderSplitArm(poseStack, vertexConsumer, model, entityData, false, packedLight, packedOverlay, entityScale, isSlimArms);

        poseStack.popPose();
    }

    /**
     * Render leg armor piece (body skirt + upper legs).
     * Body waist uses vanilla position with Mo'Bends rotation.
     * Legs are split at knee joint for proper bending.
     */
    private void renderLegs(
            PoseStack poseStack,
            VertexConsumer vertexConsumer,
            HumanoidModel<?> model,
            BipedEntityData<?> entityData,
            int packedLight,
            int packedOverlay,
            float entityScale)
    {
        poseStack.pushPose();

        // Apply baby scale if needed (babies render at 0.5 scale)
        if (entityScale != 1.0f)
        {
            poseStack.scale(entityScale, entityScale, entityScale);
        }

        // Render body (waist/skirt part of leggings) - rotation around correct pivot, vanilla position
        poseStack.pushPose();
        applyGlobalOffset(poseStack, entityData, entityScale);
        applyBodyTransformWithPivot(poseStack, entityData);
        renderPartWithVanillaPosition(model.body, poseStack, vertexConsumer, packedLight, packedOverlay);
        poseStack.popPose();

        // Render left leg (split at knee joint)
        renderSplitLeg(poseStack, vertexConsumer, model, entityData, true, packedLight, packedOverlay, entityScale);

        // Render right leg (split at knee joint)
        renderSplitLeg(poseStack, vertexConsumer, model, entityData, false, packedLight, packedOverlay, entityScale);

        poseStack.popPose();
    }

    /**
     * Render feet armor piece (boots/lower legs).
     * Legs are split at knee joint for proper bending.
     */
    private void renderFeet(
            PoseStack poseStack,
            VertexConsumer vertexConsumer,
            HumanoidModel<?> model,
            BipedEntityData<?> entityData,
            int packedLight,
            int packedOverlay,
            float entityScale)
    {
        poseStack.pushPose();

        // Apply baby scale if needed (babies render at 0.5 scale)
        if (entityScale != 1.0f)
        {
            poseStack.scale(entityScale, entityScale, entityScale);
        }

        // Render left leg (split at knee joint)
        renderSplitLeg(poseStack, vertexConsumer, model, entityData, true, packedLight, packedOverlay, entityScale);

        // Render right leg (split at knee joint)
        renderSplitLeg(poseStack, vertexConsumer, model, entityData, false, packedLight, packedOverlay, entityScale);

        poseStack.popPose();
    }

    /**
     * Apply the global offset from entity data.
     * This handles crouching and other whole-body movements.
     * The offset is scaled by entityScale for baby entities (matching MutatedRenderer behavior).
     */
    private void applyGlobalOffset(PoseStack poseStack, BipedEntityData<?> entityData, float entityScale)
    {
        if (entityData.globalOffset != null)
        {
            var offset = entityData.globalOffset.getSmooth();
            if (offset != null && (offset.x != 0 || offset.y != 0 || offset.z != 0))
            {
                // Match MutatedRenderer.beforeRender() behavior:
                // globalOffset is multiplied by entityScale for babies
                poseStack.translate(
                    offset.x * SCALE * entityScale,
                    offset.y * SCALE * entityScale,
                    offset.z * SCALE * entityScale
                );
            }
        }
    }

    /**
     * Apply body transform with correct pivot point for rotation.
     * Mo'Bends rotates the body around body.position, so we need to:
     * 1. Translate to pivot
     * 2. Apply offset and rotation
     * 3. Translate back from pivot
     */
    private void applyBodyTransformWithPivot(PoseStack poseStack, BipedEntityData<?> entityData)
    {
        ModelPartTransform body = entityData.body;
        if (body == null)
        {
            return;
        }

        // Translate to body pivot point
        poseStack.translate(
            body.position.x * SCALE,
            body.position.y * SCALE,
            body.position.z * SCALE
        );

        // Apply animation offset
        if (body.offset.x != 0 || body.offset.y != 0 || body.offset.z != 0)
        {
            poseStack.translate(
                body.offset.x * SCALE,
                body.offset.y * SCALE,
                body.offset.z * SCALE
            );
        }

        // Apply rotation (now around the correct pivot)
        GlHelper.rotate(poseStack, body.rotation.getSmooth());

        // Translate back from pivot so rendering happens at vanilla position
        poseStack.translate(
            -body.position.x * SCALE,
            -body.position.y * SCALE,
            -body.position.z * SCALE
        );
    }

    /**
     * Apply a Mo'Bends part transform to the PoseStack.
     * For root-level parts (body, legs), only applies offset + rotation.
     * For child parts (head, arms), applies position + offset + rotation.
     *
     * @param poseStack The pose stack to modify
     * @param transform The Mo'Bends transform
     * @param isChildPart If true, applies position (for parts relative to parent). If false, skips position.
     */
    private void applyPartTransform(PoseStack poseStack, ModelPartTransform transform, boolean isChildPart)
    {
        if (transform == null)
        {
            return;
        }

        // Apply position only for child parts (relative to parent)
        // Root parts (body, legs) use vanilla armor model positions
        if (isChildPart && (transform.position.x != 0 || transform.position.y != 0 || transform.position.z != 0))
        {
            poseStack.translate(
                transform.position.x * SCALE,
                transform.position.y * SCALE,
                transform.position.z * SCALE
            );
        }

        // Apply animation offset
        if (transform.offset.x != 0 || transform.offset.y != 0 || transform.offset.z != 0)
        {
            poseStack.translate(
                transform.offset.x * SCALE,
                transform.offset.y * SCALE,
                transform.offset.z * SCALE
            );
        }

        // Apply rotation
        GlHelper.rotate(poseStack, transform.rotation.getSmooth());
    }

    /**
     * Render a ModelPart keeping its vanilla position but without rotation.
     * Mo'Bends rotation is already applied to PoseStack.
     * Used for body/waist which should stay at vanilla position.
     */
    private void renderPartWithVanillaPosition(
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

    /**
     * Render a ModelPart at the origin (without its own position/rotation).
     * Full Mo'Bends transform is already applied to PoseStack.
     * Used for arms, legs, head which use full Mo'Bends positioning.
     */
    private void renderPartAtOrigin(
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

    // ========== SPLIT LIMB RENDERING ==========

    // Enable limb slicing for proper elbow/knee bending
    private static final boolean ENABLE_LIMB_SLICING = true;

    // Slim arm Y offset: standard arm Y is -10F, slim arm Y is -9.5F
    // The armor model always uses standard positions (-10F), but Mo'Bends animation
    // for slim players uses -9.5F. We need to offset the armor UP by 0.5 model units.
    private static final float SLIM_ARM_Y_OFFSET = 0.5f * SCALE;  // 0.5 model units in render units

    /**
     * Render an arm split at the elbow joint.
     * Upper arm geometry follows upperArm transform, forearm geometry follows foreArm transform.
     */
    private void renderSplitArm(
            PoseStack poseStack,
            VertexConsumer vertexConsumer,
            HumanoidModel<?> model,
            BipedEntityData<?> entityData,
            boolean isLeft,
            int packedLight,
            int packedOverlay,
            float entityScale,
            boolean isSlimArms)
    {
        ModelPart armPart = isLeft ? model.leftArm : model.rightArm;
        if (armPart == null || !armPart.visible)
        {
            return;
        }

        ModelPartTransform upperArm = isLeft ? entityData.leftArm : entityData.rightArm;

        if (!ENABLE_LIMB_SLICING)
        {
            // Simple mode: render whole arm with upper arm transform only
            // This means armor won't bend at elbow, but will render correctly
            poseStack.pushPose();
            applyGlobalOffset(poseStack, entityData, entityScale);
            applyPartTransform(poseStack, entityData.body, true);
            applyPartTransform(poseStack, upperArm, true);
            // Apply slim arm Y offset correction
            if (isSlimArms)
            {
                poseStack.translate(0, -SLIM_ARM_Y_OFFSET, 0);
            }
            renderPartAtOrigin(armPart, poseStack, vertexConsumer, packedLight, packedOverlay);
            poseStack.popPose();
            return;
        }

        // Advanced mode: split arm at elbow joint
        ModelPartTransform foreArm = isLeft ? entityData.leftForeArm : entityData.rightForeArm;
        JointPlane elbowPlane = JointDefinitions.getElbow(isLeft);

        // 1. Capture arm geometry at rest pose using IDENTITY PoseStack
        // Critical: We must use a fresh PoseStack so captured vertices are in model-local space
        // The incoming poseStack already has entity transforms that would corrupt the capture
        limbCapture.clear();
        PoseStack captureStack = new PoseStack();  // Identity matrix - no transforms
        resetPartToOrigin(armPart);
        armPart.render(captureStack, limbCapture, packedLight, packedOverlay);
        restorePartFromCapture(armPart);

        List<CapturedVertex> vertices = limbCapture.getVertices();
        if (vertices.isEmpty())
        {
            return;
        }

        // 2. Group vertices into quads and slice at elbow
        List<CapturedVertex[]> quads = groupIntoQuads(vertices);
        List<SliceResult> sliceResults = quadSlicer.sliceAll(quads, elbowPlane);

        // Slim arm Y offset for captured vertices (applied to output, not capture)
        float slimArmOffset = isSlimArms ? -SLIM_ARM_Y_OFFSET : 0;

        // 3. Render upper arm portion with upper arm transform
        // Upper portion vertices stay as-is (no offset needed except for slim arms)
        poseStack.pushPose();
        applyGlobalOffset(poseStack, entityData, entityScale);  // Global offset for crouching etc.
        applyPartTransform(poseStack, entityData.body, true);
        applyPartTransform(poseStack, upperArm, true);
        // Apply slim arm offset to Y
        renderSlicedVertices(poseStack, vertexConsumer, sliceResults, true, 0, slimArmOffset, 0, packedLight, packedOverlay);
        poseStack.popPose();

        // 4. Render forearm portion with forearm transform (chained to upper arm)
        // Lower portion vertices need to be offset by forearm's position to be in forearm-local-space
        // foreArm.position is (0, 4, 2) in model units, so we offset by (-0, -0.25, -0.125) in render units
        float foreArmOffsetX = -foreArm.position.x * SCALE;
        float foreArmOffsetY = -foreArm.position.y * SCALE + slimArmOffset;  // Include slim arm offset
        float foreArmOffsetZ = -foreArm.position.z * SCALE;
        poseStack.pushPose();
        applyGlobalOffset(poseStack, entityData, entityScale);  // Global offset for crouching etc.
        applyPartTransform(poseStack, entityData.body, true);
        applyPartTransform(poseStack, upperArm, true);
        applyPartTransform(poseStack, foreArm, true);
        renderSlicedVertices(poseStack, vertexConsumer, sliceResults, false, foreArmOffsetX, foreArmOffsetY, foreArmOffsetZ, packedLight, packedOverlay);
        poseStack.popPose();
    }

    /**
     * Render a leg split at the knee joint.
     * Upper leg geometry follows leg transform, lower leg geometry follows foreLeg transform.
     */
    private void renderSplitLeg(
            PoseStack poseStack,
            VertexConsumer vertexConsumer,
            HumanoidModel<?> model,
            BipedEntityData<?> entityData,
            boolean isLeft,
            int packedLight,
            int packedOverlay,
            float entityScale)
    {
        ModelPart legPart = isLeft ? model.leftLeg : model.rightLeg;
        if (legPart == null || !legPart.visible)
        {
            return;
        }

        ModelPartTransform upperLeg = isLeft ? entityData.leftLeg : entityData.rightLeg;

        if (!ENABLE_LIMB_SLICING)
        {
            // Simple mode: render whole leg with upper leg transform only
            // This means armor won't bend at knee, but will render correctly
            poseStack.pushPose();
            applyGlobalOffset(poseStack, entityData, entityScale);
            applyPartTransform(poseStack, upperLeg, true);
            renderPartAtOrigin(legPart, poseStack, vertexConsumer, packedLight, packedOverlay);
            poseStack.popPose();
            return;
        }

        // Advanced mode: split leg at knee joint
        ModelPartTransform lowerLeg = isLeft ? entityData.leftForeLeg : entityData.rightForeLeg;
        JointPlane kneePlane = JointDefinitions.getKnee(isLeft);

        // 1. Capture leg geometry at rest pose using IDENTITY PoseStack
        // Critical: We must use a fresh PoseStack so captured vertices are in model-local space
        // The incoming poseStack already has entity transforms that would corrupt the capture
        limbCapture.clear();
        PoseStack captureStack = new PoseStack();  // Identity matrix - no transforms
        resetPartToOrigin(legPart);
        legPart.render(captureStack, limbCapture, packedLight, packedOverlay);
        restorePartFromCapture(legPart);

        List<CapturedVertex> vertices = limbCapture.getVertices();
        if (vertices.isEmpty())
        {
            return;
        }

        // 2. Group vertices into quads and slice at knee
        List<CapturedVertex[]> quads = groupIntoQuads(vertices);
        List<SliceResult> sliceResults = quadSlicer.sliceAll(quads, kneePlane);

        // 3. Render upper leg portion with upper leg transform
        // Note: Legs are NOT parented to body in Mo'Bends, but need global offset
        // Upper portion vertices stay as-is (no offset needed)
        poseStack.pushPose();
        applyGlobalOffset(poseStack, entityData, entityScale);  // Global offset for crouching etc.
        applyPartTransform(poseStack, upperLeg, true);
        renderSlicedVertices(poseStack, vertexConsumer, sliceResults, true, 0, 0, 0, packedLight, packedOverlay);
        poseStack.popPose();

        // 4. Render lower leg portion with lower leg transform (chained to upper leg)
        // Lower portion vertices need to be offset by lowerLeg's position to be in lower-leg-local-space
        // lowerLeg.position is (0, 6, -2) in model units, so we offset by (-0, -0.375, 0.125) in render units
        float lowerLegOffsetX = -lowerLeg.position.x * SCALE;
        float lowerLegOffsetY = -lowerLeg.position.y * SCALE;
        float lowerLegOffsetZ = -lowerLeg.position.z * SCALE;
        poseStack.pushPose();
        applyGlobalOffset(poseStack, entityData, entityScale);  // Global offset for crouching etc.
        applyPartTransform(poseStack, upperLeg, true);
        applyPartTransform(poseStack, lowerLeg, true);
        renderSlicedVertices(poseStack, vertexConsumer, sliceResults, false, lowerLegOffsetX, lowerLegOffsetY, lowerLegOffsetZ, packedLight, packedOverlay);
        poseStack.popPose();
    }

    // ========== HELPER METHODS FOR SPLIT RENDERING ==========

    // Storage for original part values during capture
    private float capturedX, capturedY, capturedZ;
    private float capturedXRot, capturedYRot, capturedZRot;

    /**
     * Reset a ModelPart to origin for geometry capture.
     * Stores original values for later restoration.
     */
    private void resetPartToOrigin(ModelPart part)
    {
        capturedX = part.x;
        capturedY = part.y;
        capturedZ = part.z;
        capturedXRot = part.xRot;
        capturedYRot = part.yRot;
        capturedZRot = part.zRot;

        part.x = 0;
        part.y = 0;
        part.z = 0;
        part.xRot = 0;
        part.yRot = 0;
        part.zRot = 0;
    }

    /**
     * Restore a ModelPart to its original values after capture.
     */
    private void restorePartFromCapture(ModelPart part)
    {
        part.x = capturedX;
        part.y = capturedY;
        part.z = capturedZ;
        part.xRot = capturedXRot;
        part.yRot = capturedYRot;
        part.zRot = capturedZRot;
    }

    /**
     * Group captured vertices into quads (4 vertices each).
     * Minecraft renders quads, so every 4 consecutive vertices form a quad.
     */
    private List<CapturedVertex[]> groupIntoQuads(List<CapturedVertex> vertices)
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
     */
    private void renderSlicedVertices(
            PoseStack poseStack,
            VertexConsumer consumer,
            List<SliceResult> sliceResults,
            boolean renderUpper,
            float offsetX,
            float offsetY,
            float offsetZ,
            int packedLight,
            int packedOverlay)
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
                outputVertex(matrix, normal, consumer, vertices.get(0), offsetX, offsetY, offsetZ, packedLight, packedOverlay);
                outputVertex(matrix, normal, consumer, vertices.get(1), offsetX, offsetY, offsetZ, packedLight, packedOverlay);
                outputVertex(matrix, normal, consumer, vertices.get(2), offsetX, offsetY, offsetZ, packedLight, packedOverlay);
                outputVertex(matrix, normal, consumer, vertices.get(3), offsetX, offsetY, offsetZ, packedLight, packedOverlay);
            }
            else if (vertexCount == 3)
            {
                // Triangle - create degenerate quad by duplicating last vertex
                outputVertex(matrix, normal, consumer, vertices.get(0), offsetX, offsetY, offsetZ, packedLight, packedOverlay);
                outputVertex(matrix, normal, consumer, vertices.get(1), offsetX, offsetY, offsetZ, packedLight, packedOverlay);
                outputVertex(matrix, normal, consumer, vertices.get(2), offsetX, offsetY, offsetZ, packedLight, packedOverlay);
                outputVertex(matrix, normal, consumer, vertices.get(2), offsetX, offsetY, offsetZ, packedLight, packedOverlay); // Duplicate
            }
            else if (vertexCount == 5)
            {
                // Pentagon - split into 2 quads: (0,1,2,3) and (0,3,4,4)
                outputVertex(matrix, normal, consumer, vertices.get(0), offsetX, offsetY, offsetZ, packedLight, packedOverlay);
                outputVertex(matrix, normal, consumer, vertices.get(1), offsetX, offsetY, offsetZ, packedLight, packedOverlay);
                outputVertex(matrix, normal, consumer, vertices.get(2), offsetX, offsetY, offsetZ, packedLight, packedOverlay);
                outputVertex(matrix, normal, consumer, vertices.get(3), offsetX, offsetY, offsetZ, packedLight, packedOverlay);

                outputVertex(matrix, normal, consumer, vertices.get(0), offsetX, offsetY, offsetZ, packedLight, packedOverlay);
                outputVertex(matrix, normal, consumer, vertices.get(3), offsetX, offsetY, offsetZ, packedLight, packedOverlay);
                outputVertex(matrix, normal, consumer, vertices.get(4), offsetX, offsetY, offsetZ, packedLight, packedOverlay);
                outputVertex(matrix, normal, consumer, vertices.get(4), offsetX, offsetY, offsetZ, packedLight, packedOverlay); // Duplicate
            }
            else if (vertexCount >= 6)
            {
                // 6+ vertices - fan triangulation converted to quads
                // Each triangle (0, i, i+1) becomes a degenerate quad
                for (int i = 1; i < vertexCount - 1; i++)
                {
                    outputVertex(matrix, normal, consumer, vertices.get(0), offsetX, offsetY, offsetZ, packedLight, packedOverlay);
                    outputVertex(matrix, normal, consumer, vertices.get(i), offsetX, offsetY, offsetZ, packedLight, packedOverlay);
                    outputVertex(matrix, normal, consumer, vertices.get(i + 1), offsetX, offsetY, offsetZ, packedLight, packedOverlay);
                    outputVertex(matrix, normal, consumer, vertices.get(i + 1), offsetX, offsetY, offsetZ, packedLight, packedOverlay); // Duplicate
                }
            }
            // vertexCount < 3 is degenerate, skip
        }
    }

    /**
     * Output a single vertex, transformed by the given matrices.
     * Applies an offset to the vertex position before transformation (for local space conversion).
     */
    private void outputVertex(
            Matrix4f matrix,
            Matrix3f normal,
            VertexConsumer consumer,
            SliceResult.SlicedVertex v,
            float offsetX,
            float offsetY,
            float offsetZ,
            int packedLight,
            int packedOverlay)
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

        // Output transformed vertex
        consumer.vertex(
                tx, ty, tz,
                v.red, v.green, v.blue, v.alpha,
                v.u, v.v,
                packedOverlay, packedLight,
                nx, ny, nz
        );
    }

    /**
     * Configure model part visibility based on equipment slot.
     */
    private void configureVisibility(HumanoidModel<?> model, EquipmentSlot slot)
    {
        model.head.visible = false;
        model.hat.visible = false;
        model.body.visible = false;
        model.rightArm.visible = false;
        model.leftArm.visible = false;
        model.rightLeg.visible = false;
        model.leftLeg.visible = false;

        switch (slot)
        {
            case HEAD:
                model.head.visible = true;
                model.hat.visible = true;
                break;
            case CHEST:
                model.body.visible = true;
                model.rightArm.visible = true;
                model.leftArm.visible = true;
                break;
            case LEGS:
                model.body.visible = true;
                model.rightLeg.visible = true;
                model.leftLeg.visible = true;
                break;
            case FEET:
                model.rightLeg.visible = true;
                model.leftLeg.visible = true;
                break;
        }
    }

    /**
     * Render without animation (vanilla passthrough).
     */
    private <E extends LivingEntity> void renderVanilla(
            ArmorRenderContext<E> context,
            HumanoidModel<?> model,
            ResourceLocation texture,
            Function<ResourceLocation, RenderType> renderTypeProvider)
    {
        PoseStack poseStack = context.getPoseStack();
        MultiBufferSource bufferSource = context.getBufferSource();

        poseStack.pushPose();

        configureVisibility(model, context.getSlot());

        RenderType renderType = renderTypeProvider.apply(texture);
        model.renderToBuffer(
                poseStack,
                bufferSource.getBuffer(renderType),
                context.getPackedLight(),
                context.getPackedOverlay(),
                1.0f, 1.0f, 1.0f, 1.0f
        );

        poseStack.popPose();
    }

    /**
     * Get the transform injector.
     */
    public TransformInjector getTransformInjector()
    {
        return transformInjector;
    }

    /**
     * Get the split limb renderer.
     */
    public SplitLimbRenderer getSplitLimbRenderer()
    {
        return splitLimbRenderer;
    }

    /**
     * Get the Tier 2 fallback renderer.
     */
    public Tier2Renderer getTier2Fallback()
    {
        return tier2Fallback;
    }

    /**
     * Get the total number of render calls.
     */
    public long getRenderCount()
    {
        return renderCount;
    }

    /**
     * Get the number of renders that fell back to Tier 2.
     */
    public long getFallbackCount()
    {
        return fallbackCount;
    }

    /**
     * Get the fallback rate.
     */
    public float getFallbackRate()
    {
        return renderCount > 0 ? (float) fallbackCount / renderCount : 0;
    }

    /**
     * Reset statistics.
     */
    public void resetStats()
    {
        renderCount = 0;
        fallbackCount = 0;
    }

    /**
     * Get debug statistics.
     */
    public String getStats()
    {
        return String.format("Tier1Renderer: %d renders, %d fallbacks (%.1f%% fallback rate)",
                renderCount, fallbackCount, getFallbackRate() * 100);
    }

    /**
     * Storage for original part states to restore after rendering.
     */
    private static class PartStateStorage
    {
        private float headX, headY, headZ, headXRot, headYRot, headZRot;
        private float hatX, hatY, hatZ, hatXRot, hatYRot, hatZRot;
        private float bodyX, bodyY, bodyZ, bodyXRot, bodyYRot, bodyZRot;
        private float leftArmX, leftArmY, leftArmZ, leftArmXRot, leftArmYRot, leftArmZRot;
        private float rightArmX, rightArmY, rightArmZ, rightArmXRot, rightArmYRot, rightArmZRot;
        private float leftLegX, leftLegY, leftLegZ, leftLegXRot, leftLegYRot, leftLegZRot;
        private float rightLegX, rightLegY, rightLegZ, rightLegXRot, rightLegYRot, rightLegZRot;

        void store(HumanoidModel<?> model)
        {
            storePart(model.head);
            headX = model.head.x; headY = model.head.y; headZ = model.head.z;
            headXRot = model.head.xRot; headYRot = model.head.yRot; headZRot = model.head.zRot;

            hatX = model.hat.x; hatY = model.hat.y; hatZ = model.hat.z;
            hatXRot = model.hat.xRot; hatYRot = model.hat.yRot; hatZRot = model.hat.zRot;

            bodyX = model.body.x; bodyY = model.body.y; bodyZ = model.body.z;
            bodyXRot = model.body.xRot; bodyYRot = model.body.yRot; bodyZRot = model.body.zRot;

            leftArmX = model.leftArm.x; leftArmY = model.leftArm.y; leftArmZ = model.leftArm.z;
            leftArmXRot = model.leftArm.xRot; leftArmYRot = model.leftArm.yRot; leftArmZRot = model.leftArm.zRot;

            rightArmX = model.rightArm.x; rightArmY = model.rightArm.y; rightArmZ = model.rightArm.z;
            rightArmXRot = model.rightArm.xRot; rightArmYRot = model.rightArm.yRot; rightArmZRot = model.rightArm.zRot;

            leftLegX = model.leftLeg.x; leftLegY = model.leftLeg.y; leftLegZ = model.leftLeg.z;
            leftLegXRot = model.leftLeg.xRot; leftLegYRot = model.leftLeg.yRot; leftLegZRot = model.leftLeg.zRot;

            rightLegX = model.rightLeg.x; rightLegY = model.rightLeg.y; rightLegZ = model.rightLeg.z;
            rightLegXRot = model.rightLeg.xRot; rightLegYRot = model.rightLeg.yRot; rightLegZRot = model.rightLeg.zRot;
        }

        void restore(HumanoidModel<?> model)
        {
            model.head.x = headX; model.head.y = headY; model.head.z = headZ;
            model.head.xRot = headXRot; model.head.yRot = headYRot; model.head.zRot = headZRot;

            model.hat.x = hatX; model.hat.y = hatY; model.hat.z = hatZ;
            model.hat.xRot = hatXRot; model.hat.yRot = hatYRot; model.hat.zRot = hatZRot;

            model.body.x = bodyX; model.body.y = bodyY; model.body.z = bodyZ;
            model.body.xRot = bodyXRot; model.body.yRot = bodyYRot; model.body.zRot = bodyZRot;

            model.leftArm.x = leftArmX; model.leftArm.y = leftArmY; model.leftArm.z = leftArmZ;
            model.leftArm.xRot = leftArmXRot; model.leftArm.yRot = leftArmYRot; model.leftArm.zRot = leftArmZRot;

            model.rightArm.x = rightArmX; model.rightArm.y = rightArmY; model.rightArm.z = rightArmZ;
            model.rightArm.xRot = rightArmXRot; model.rightArm.yRot = rightArmYRot; model.rightArm.zRot = rightArmZRot;

            model.leftLeg.x = leftLegX; model.leftLeg.y = leftLegY; model.leftLeg.z = leftLegZ;
            model.leftLeg.xRot = leftLegXRot; model.leftLeg.yRot = leftLegYRot; model.leftLeg.zRot = leftLegZRot;

            model.rightLeg.x = rightLegX; model.rightLeg.y = rightLegY; model.rightLeg.z = rightLegZ;
            model.rightLeg.xRot = rightLegXRot; model.rightLeg.yRot = rightLegYRot; model.rightLeg.zRot = rightLegZRot;
        }

        private void storePart(ModelPart part)
        {
            // Helper method placeholder
        }
    }
}
