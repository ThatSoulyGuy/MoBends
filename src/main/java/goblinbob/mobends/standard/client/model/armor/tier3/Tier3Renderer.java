package goblinbob.mobends.standard.client.model.armor.tier3;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import goblinbob.mobends.standard.client.model.armor.ArmorRenderContext;
import goblinbob.mobends.standard.client.model.armor.CapturingVertexConsumer;
import goblinbob.mobends.standard.client.model.armor.RigidArmorRenderer;
import goblinbob.mobends.standard.client.model.armor.cache.CacheManager;
import goblinbob.mobends.standard.client.model.armor.tier.RenderTier;
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
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.function.Function;

/**
 * Tier 3 renderer: Vertex Capture Fallback.
 *
 * This is the most compatible but least efficient rendering approach.
 * It works by:
 * 1. Capturing all vertices output by the vanilla armor render
 * 2. Assigning each vertex to a bone based on spatial position
 * 3. Transforming each vertex by its assigned bone's animation transform
 * 4. Outputting the transformed vertices to the actual render buffer
 *
 * This renderer handles any armor model regardless of its structure.
 * It's used as a fallback when Tier 1 and Tier 2 approaches aren't applicable.
 */
@OnlyIn(Dist.CLIENT)
public class Tier3Renderer
{
    private final RigidArmorRenderer rigidArmorRenderer;

    // Performance tracking
    private long renderCount = 0;
    private long totalVerticesProcessed = 0;

    public Tier3Renderer()
    {
        this.rigidArmorRenderer = new RigidArmorRenderer();
    }

    public Tier3Renderer(RigidArmorRenderer rigidArmorRenderer)
    {
        this.rigidArmorRenderer = rigidArmorRenderer;
    }

    /**
     * Get the tier this renderer handles.
     */
    public RenderTier getTier()
    {
        return RenderTier.TIER_3_VERTEX_CAPTURE;
    }

    // =========================================================================
    // Model Pose Save/Reset/Restore - Critical for correct vertex capture
    // =========================================================================

    /**
     * Snapshot of model part poses for save/restore.
     */
    private static class ModelPoseSnapshot
    {
        private final float headXRot, headYRot, headZRot;
        private final float hatXRot, hatYRot, hatZRot;
        private final float bodyXRot, bodyYRot, bodyZRot;
        private final float leftArmXRot, leftArmYRot, leftArmZRot;
        private final float rightArmXRot, rightArmYRot, rightArmZRot;
        private final float leftLegXRot, leftLegYRot, leftLegZRot;
        private final float rightLegXRot, rightLegYRot, rightLegZRot;

        public ModelPoseSnapshot(HumanoidModel<?> model)
        {
            headXRot = model.head.xRot; headYRot = model.head.yRot; headZRot = model.head.zRot;
            hatXRot = model.hat.xRot; hatYRot = model.hat.yRot; hatZRot = model.hat.zRot;
            bodyXRot = model.body.xRot; bodyYRot = model.body.yRot; bodyZRot = model.body.zRot;
            leftArmXRot = model.leftArm.xRot; leftArmYRot = model.leftArm.yRot; leftArmZRot = model.leftArm.zRot;
            rightArmXRot = model.rightArm.xRot; rightArmYRot = model.rightArm.yRot; rightArmZRot = model.rightArm.zRot;
            leftLegXRot = model.leftLeg.xRot; leftLegYRot = model.leftLeg.yRot; leftLegZRot = model.leftLeg.zRot;
            rightLegXRot = model.rightLeg.xRot; rightLegYRot = model.rightLeg.yRot; rightLegZRot = model.rightLeg.zRot;
        }

        public void restore(HumanoidModel<?> model)
        {
            model.head.xRot = headXRot; model.head.yRot = headYRot; model.head.zRot = headZRot;
            model.hat.xRot = hatXRot; model.hat.yRot = hatYRot; model.hat.zRot = hatZRot;
            model.body.xRot = bodyXRot; model.body.yRot = bodyYRot; model.body.zRot = bodyZRot;
            model.leftArm.xRot = leftArmXRot; model.leftArm.yRot = leftArmYRot; model.leftArm.zRot = leftArmZRot;
            model.rightArm.xRot = rightArmXRot; model.rightArm.yRot = rightArmYRot; model.rightArm.zRot = rightArmZRot;
            model.leftLeg.xRot = leftLegXRot; model.leftLeg.yRot = leftLegYRot; model.leftLeg.zRot = leftLegZRot;
            model.rightLeg.xRot = rightLegXRot; model.rightLeg.yRot = rightLegYRot; model.rightLeg.zRot = rightLegZRot;
        }
    }

    /**
     * Reset the model to rest pose (zero all rotations).
     * This is CRITICAL - we want to capture vertices in their default positions,
     * then apply Mo'Bends transforms during the render phase.
     */
    private void resetToRestPose(HumanoidModel<?> model)
    {
        resetPart(model.head);
        resetPart(model.hat);
        resetPart(model.body);
        resetPart(model.leftArm);
        resetPart(model.rightArm);
        resetPart(model.leftLeg);
        resetPart(model.rightLeg);
    }

    private void resetPart(ModelPart part)
    {
        part.xRot = 0;
        part.yRot = 0;
        part.zRot = 0;
        // Keep the default positions (x, y, z) as they define the part's origin
    }

    /**
     * Simple render method for facade integration.
     * Returns true if rendering was handled, false to indicate fallback needed.
     */
    public <E extends LivingEntity> boolean render(ArmorRenderContext<E> context, Model model)
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
            Model model,
            ResourceLocation texture,
            boolean hasFoil)
    {
        if (context == null || model == null || context.getEntityData() == null || texture == null)
        {
            return false;
        }

        try
        {
            // Use ItemRenderer.getArmorFoilBuffer to properly handle enchantment glint
            renderWithFoil(context, model, texture, hasFoil);
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    /**
     * Render armor with proper foil/glint support.
     */
    private <E extends LivingEntity> void renderWithFoil(
            ArmorRenderContext<E> context,
            Model model,
            ResourceLocation texture,
            boolean hasFoil)
    {
        if (context.getEntityData() == null)
        {
            return;
        }

        PoseStack poseStack = context.getPoseStack();
        MultiBufferSource bufferSource = context.getBufferSource();
        BipedEntityData<?> entityData = context.getEntityData();

        // Get the capturing consumer
        CapturingVertexConsumer captureConsumer = rigidArmorRenderer.getCaptureConsumer();

        // Configure model visibility based on slot
        configureModelVisibility(model, context.getSlot());

        // CRITICAL: Save model poses, reset to rest pose, then restore after capture
        ModelPoseSnapshot snapshot = null;
        if (model instanceof HumanoidModel<?> humanoid)
        {
            snapshot = new ModelPoseSnapshot(humanoid);
            resetToRestPose(humanoid);
        }

        // CRITICAL: Use a FRESH identity PoseStack for capture
        PoseStack capturePoseStack = new PoseStack();

        // Render the model to capture rest-pose vertices
        model.renderToBuffer(
                capturePoseStack,
                captureConsumer,
                context.getPackedLight(),
                context.getPackedOverlay(),
                0xFFFFFFFF
        );

        // Restore original model poses
        if (snapshot != null && model instanceof HumanoidModel<?> humanoid)
        {
            snapshot.restore(humanoid);
        }

        // Track statistics
        int vertexCount = captureConsumer.getVertices().size();
        totalVerticesProcessed += vertexCount;
        renderCount++;

        // Render with proper foil support
        poseStack.pushPose();

        // Apply baby scale if needed
        float entityScale = context.getEntityScale();
        if (entityScale != 1.0f)
        {
            poseStack.scale(entityScale, entityScale, entityScale);
        }

        // Get foil-enabled vertex consumer
        VertexConsumer outputConsumer = ItemRenderer.getArmorFoilBuffer(
                bufferSource,
                RenderType.armorCutoutNoCull(texture),
                hasFoil);

        rigidArmorRenderer.renderCapturedVertices(
                poseStack,
                outputConsumer,
                context.getPackedLight(),
                context.getPackedOverlay(),
                entityData,
                entityScale,
                context.getArmorColor()
        );

        poseStack.popPose();

        // Record cache statistics
        CacheManager.getInstance().recordUncachedRender();
    }

    /**
     * Render armor using the vertex capture approach.
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
        if (context.getEntityData() == null)
        {
            // No animation data - fall back to vanilla rendering
            renderVanilla(context, model, texture, renderTypeProvider);
            return;
        }

        PoseStack poseStack = context.getPoseStack();
        MultiBufferSource bufferSource = context.getBufferSource();
        BipedEntityData<?> entityData = context.getEntityData();

        // Get the capturing consumer
        CapturingVertexConsumer captureConsumer = rigidArmorRenderer.getCaptureConsumer();

        // Configure model visibility based on slot
        configureModelVisibility(model, context.getSlot());

        // CRITICAL: Save model poses, reset to rest pose, then restore after capture
        // The model may have animation rotations applied that would distort captured vertices
        ModelPoseSnapshot snapshot = null;
        if (model instanceof HumanoidModel<?> humanoid)
        {
            snapshot = new ModelPoseSnapshot(humanoid);
            resetToRestPose(humanoid);
        }

        // CRITICAL: Use a FRESH identity PoseStack for capture!
        // We want to capture vertices in REST POSE (no transforms).
        // The context's poseStack has entity transforms already applied.
        PoseStack capturePoseStack = new PoseStack();

        // Render the model to capture rest-pose vertices
        RenderType renderType = renderTypeProvider.apply(texture);
        model.renderToBuffer(
                capturePoseStack,
                captureConsumer,
                context.getPackedLight(),
                context.getPackedOverlay(),
                0xFFFFFFFF
        );

        // Restore original model poses
        if (snapshot != null && model instanceof HumanoidModel<?> humanoid)
        {
            snapshot.restore(humanoid);
        }

        // Track statistics
        int vertexCount = captureConsumer.getVertices().size();
        totalVerticesProcessed += vertexCount;
        renderCount++;

        // Now render the captured vertices with bone transforms
        // Pass entityScale for proper baby entity handling
        poseStack.pushPose();

        // Apply baby scale if needed (babies render at 0.5 scale)
        float entityScale = context.getEntityScale();
        if (entityScale != 1.0f)
        {
            poseStack.scale(entityScale, entityScale, entityScale);
        }

        VertexConsumer outputConsumer = bufferSource.getBuffer(renderType);
        rigidArmorRenderer.renderCapturedVertices(
                poseStack,
                outputConsumer,
                context.getPackedLight(),
                context.getPackedOverlay(),
                entityData,
                entityScale,
                context.getArmorColor()
        );

        poseStack.popPose();

        // Record cache statistics
        CacheManager.getInstance().recordUncachedRender();
    }

    /**
     * Simplified render method when you have the armor model and want direct rendering.
     *
     * @param poseStack The pose stack
     * @param bufferSource Buffer source for rendering
     * @param model The armor model
     * @param texture The armor texture
     * @param slot The equipment slot
     * @param packedLight Packed light value
     * @param packedOverlay Packed overlay value
     * @param entityData Entity animation data
     * @param renderTypeProvider Function to get RenderType from texture
     */
    public void renderDirect(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            Model model,
            ResourceLocation texture,
            EquipmentSlot slot,
            int packedLight,
            int packedOverlay,
            BipedEntityData<?> entityData,
            Function<ResourceLocation, RenderType> renderTypeProvider)
    {
        renderDirect(poseStack, bufferSource, model, texture, slot, packedLight, packedOverlay, entityData, renderTypeProvider, 1.0f, 0xFFFFFFFF);
    }

    /**
     * Simplified render method when you have the armor model and want direct rendering.
     *
     * @param poseStack The pose stack
     * @param bufferSource Buffer source for rendering
     * @param model The armor model
     * @param texture The armor texture
     * @param slot The equipment slot
     * @param packedLight Packed light value
     * @param packedOverlay Packed overlay value
     * @param entityData Entity animation data
     * @param renderTypeProvider Function to get RenderType from texture
     * @param entityScale Scale factor (1.0 for adults, 0.5 for babies)
     */
    public void renderDirect(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            Model model,
            ResourceLocation texture,
            EquipmentSlot slot,
            int packedLight,
            int packedOverlay,
            BipedEntityData<?> entityData,
            Function<ResourceLocation, RenderType> renderTypeProvider,
            float entityScale)
    {
        renderDirect(poseStack, bufferSource, model, texture, slot, packedLight, packedOverlay, entityData, renderTypeProvider, entityScale, 0xFFFFFFFF);
    }

    /**
     * Simplified render method when you have the armor model and want direct rendering.
     *
     * @param poseStack The pose stack
     * @param bufferSource Buffer source for rendering
     * @param model The armor model
     * @param texture The armor texture
     * @param slot The equipment slot
     * @param packedLight Packed light value
     * @param packedOverlay Packed overlay value
     * @param entityData Entity animation data
     * @param renderTypeProvider Function to get RenderType from texture
     * @param entityScale Scale factor (1.0 for adults, 0.5 for babies)
     * @param armorColor ARGB color tint for leather armor (0xFFFFFFFF for no tint)
     */
    public void renderDirect(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            Model model,
            ResourceLocation texture,
            EquipmentSlot slot,
            int packedLight,
            int packedOverlay,
            BipedEntityData<?> entityData,
            Function<ResourceLocation, RenderType> renderTypeProvider,
            float entityScale,
            int armorColor)
    {
        if (entityData == null)
        {
            // No animation data - render normally
            RenderType renderType = renderTypeProvider.apply(texture);
            configureModelVisibility(model, slot);
            model.renderToBuffer(poseStack, bufferSource.getBuffer(renderType),
                    packedLight, packedOverlay, armorColor);
            return;
        }

        // Get the capturing consumer
        CapturingVertexConsumer captureConsumer = rigidArmorRenderer.getCaptureConsumer();

        // Configure visibility first
        configureModelVisibility(model, slot);

        // CRITICAL: Save model poses, reset to rest pose, then restore after capture
        // The model may have animation rotations applied that would distort captured vertices
        ModelPoseSnapshot snapshot = null;
        if (model instanceof HumanoidModel<?> humanoid)
        {
            snapshot = new ModelPoseSnapshot(humanoid);
            resetToRestPose(humanoid);
        }

        // CRITICAL: Use a fresh identity PoseStack for capture to get rest-pose vertices
        PoseStack capturePoseStack = new PoseStack();

        RenderType renderType = renderTypeProvider.apply(texture);
        model.renderToBuffer(capturePoseStack, captureConsumer, packedLight, packedOverlay, 0xFFFFFFFF);

        // Restore original model poses
        if (snapshot != null && model instanceof HumanoidModel<?> humanoid)
        {
            snapshot.restore(humanoid);
        }

        // Track statistics
        totalVerticesProcessed += captureConsumer.getVertices().size();
        renderCount++;

        // Transform and output phase - use the actual poseStack for rendering
        // Pass entityScale for proper baby entity handling
        poseStack.pushPose();

        // Apply baby scale if needed (babies render at 0.5 scale)
        if (entityScale != 1.0f)
        {
            poseStack.scale(entityScale, entityScale, entityScale);
        }

        VertexConsumer outputConsumer = bufferSource.getBuffer(renderType);
        rigidArmorRenderer.renderCapturedVertices(poseStack, outputConsumer,
                packedLight, packedOverlay, entityData, entityScale, armorColor);

        poseStack.popPose();
    }

    /**
     * Render without animation (vanilla passthrough).
     */
    private <E extends LivingEntity> void renderVanilla(
            ArmorRenderContext<E> context,
            Model model,
            ResourceLocation texture,
            Function<ResourceLocation, RenderType> renderTypeProvider)
    {
        PoseStack poseStack = context.getPoseStack();
        MultiBufferSource bufferSource = context.getBufferSource();

        poseStack.pushPose();

        configureModelVisibility(model, context.getSlot());

        RenderType renderType = renderTypeProvider.apply(texture);
        model.renderToBuffer(
                poseStack,
                bufferSource.getBuffer(renderType),
                context.getPackedLight(),
                context.getPackedOverlay(),
                context.getArmorColor()
        );

        poseStack.popPose();
    }

    /**
     * Configure model part visibility based on equipment slot.
     */
    private void configureModelVisibility(Model model, EquipmentSlot slot)
    {
        if (!(model instanceof HumanoidModel<?> humanoid))
        {
            return;
        }

        // Reset all visibility
        humanoid.head.visible = false;
        humanoid.hat.visible = false;
        humanoid.body.visible = false;
        humanoid.rightArm.visible = false;
        humanoid.leftArm.visible = false;
        humanoid.rightLeg.visible = false;
        humanoid.leftLeg.visible = false;

        // Set visibility based on slot
        switch (slot)
        {
            case HEAD:
                humanoid.head.visible = true;
                humanoid.hat.visible = true;
                break;
            case CHEST:
                humanoid.body.visible = true;
                humanoid.rightArm.visible = true;
                humanoid.leftArm.visible = true;
                break;
            case LEGS:
                humanoid.body.visible = true;
                humanoid.rightLeg.visible = true;
                humanoid.leftLeg.visible = true;
                break;
            case FEET:
                humanoid.rightLeg.visible = true;
                humanoid.leftLeg.visible = true;
                break;
        }
    }

    /**
     * Get the underlying rigid armor renderer.
     */
    public RigidArmorRenderer getRigidArmorRenderer()
    {
        return rigidArmorRenderer;
    }

    /**
     * Get the total number of render calls made.
     */
    public long getRenderCount()
    {
        return renderCount;
    }

    /**
     * Get the total number of vertices processed.
     */
    public long getTotalVerticesProcessed()
    {
        return totalVerticesProcessed;
    }

    /**
     * Get the average vertices per render.
     */
    public float getAverageVerticesPerRender()
    {
        return renderCount > 0 ? (float) totalVerticesProcessed / renderCount : 0;
    }

    /**
     * Reset statistics.
     */
    public void resetStats()
    {
        renderCount = 0;
        totalVerticesProcessed = 0;
    }

    /**
     * Get debug statistics.
     */
    public String getStats()
    {
        return String.format("Tier3Renderer: %d renders, %d vertices (avg %.1f/render)",
                renderCount, totalVerticesProcessed, getAverageVerticesPerRender());
    }
}
