package goblinbob.mobends.standard.client.model.armor.tier2;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import goblinbob.mobends.core.client.model.ModelPartTransform;
import goblinbob.mobends.standard.client.model.armor.*;
import goblinbob.mobends.standard.client.model.armor.cache.CacheManager;
import goblinbob.mobends.standard.client.model.armor.tier.RenderTier;
import goblinbob.mobends.standard.data.BipedEntityData;
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
import java.util.List;
import java.util.function.Function;

/**
 * Tier 2 renderer: For custom armor models that don't extend HumanoidModel.
 *
 * Uses Minecraft's API to find parts by standard limb names, then applies
 * vertex capture + joint slicing (same approach as Tier 1).
 *
 * Standard part names searched:
 * - "head", "hat"
 * - "body"
 * - "left_arm", "right_arm"
 * - "left_leg", "right_leg"
 *
 * For parts not found by name, falls back to rendering without joint slicing.
 */
@OnlyIn(Dist.CLIENT)
public class Tier2Renderer
{
    // Vertex capture and slicing for limb joint deformation (same as Tier 1)
    private final CapturingVertexConsumer limbCapture = new CapturingVertexConsumer();
    private final QuadSlicer quadSlicer = new QuadSlicer();

    // Performance tracking
    private long renderCount = 0;

    // Current armor color for rendering
    private int currentArmorColor = 0xFFFFFFFF;

    public Tier2Renderer()
    {
    }

    /**
     * Get the tier this renderer handles.
     */
    public RenderTier getTier()
    {
        return RenderTier.TIER_2_MODEL_INTERCEPTION;
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
            VertexConsumer vertexConsumer = ItemRenderer.getArmorFoilBuffer(
                    context.getBufferSource(),
                    RenderType.armorCutoutNoCull(texture),
                    hasFoil);

            renderWithConsumer(context, model, vertexConsumer);
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    /**
     * Render armor using the model interception approach.
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

        RenderType renderType = renderTypeProvider.apply(texture);
        VertexConsumer vertexConsumer = context.getBufferSource().getBuffer(renderType);
        renderWithConsumer(context, model, vertexConsumer);
    }

    /**
     * Core rendering logic using vertex capture + joint slicing.
     */
    private <E extends LivingEntity> void renderWithConsumer(
            ArmorRenderContext<E> context,
            Model model,
            VertexConsumer vertexConsumer)
    {
        if (context.getEntityData() == null)
        {
            return;
        }

        renderCount++;
        currentArmorColor = context.getArmorColor();

        BipedEntityData<?> entityData = context.getEntityData();
        PoseStack poseStack = context.getPoseStack();
        EquipmentSlot slot = context.getSlot();
        int packedLight = context.getPackedLight();
        int packedOverlay = context.getPackedOverlay();
        float entityScale = context.getEntityScale();

        // Get model root part
        ModelPart root = getModelRoot(model);
        if (root == null)
        {
            // Can't find root - render without Mo'Bends transforms
            renderVanillaFallback(context, model, vertexConsumer);
            return;
        }

        poseStack.pushPose();

        // Apply baby scale if needed
        if (entityScale != 1.0f)
        {
            poseStack.scale(entityScale, entityScale, entityScale);
        }

        // Render based on slot
        switch (slot)
        {
            case HEAD:
                renderHead(poseStack, vertexConsumer, root, entityData, packedLight, packedOverlay);
                break;
            case CHEST:
                renderChest(poseStack, vertexConsumer, root, entityData, packedLight, packedOverlay);
                break;
            case LEGS:
                renderLegs(poseStack, vertexConsumer, root, entityData, packedLight, packedOverlay);
                break;
            case FEET:
                renderFeet(poseStack, vertexConsumer, root, entityData, packedLight, packedOverlay);
                break;
        }

        poseStack.popPose();

        CacheManager.getInstance().recordCacheAssistedRender();
    }

    /**
     * Get the root ModelPart from a Model.
     * Uses reflection to find the root part field.
     */
    @Nullable
    private ModelPart getModelRoot(Model model)
    {
        try
        {
            // Try common field names for root part
            for (String fieldName : new String[]{"root", "body", "main"})
            {
                try
                {
                    java.lang.reflect.Field field = model.getClass().getDeclaredField(fieldName);
                    field.setAccessible(true);
                    Object value = field.get(model);
                    if (value instanceof ModelPart)
                    {
                        return (ModelPart) value;
                    }
                }
                catch (NoSuchFieldException ignored)
                {
                    // Try next field name
                }
            }

            // Search all fields for a ModelPart
            for (java.lang.reflect.Field field : model.getClass().getDeclaredFields())
            {
                if (ModelPart.class.isAssignableFrom(field.getType()))
                {
                    field.setAccessible(true);
                    return (ModelPart) field.get(model);
                }
            }
        }
        catch (Exception e)
        {
            // Reflection failed
        }
        return null;
    }

    /**
     * Find a part by name in the model hierarchy.
     */
    @Nullable
    private ModelPart findPartByName(ModelPart root, String... names)
    {
        for (String name : names)
        {
            // Try direct child lookup
            try
            {
                ModelPart child = root.getChild(name);
                if (child != null)
                {
                    return child;
                }
            }
            catch (Exception ignored)
            {
                // Child not found, try next name
            }
        }
        return null;
    }

    /**
     * Render head armor.
     */
    private void renderHead(
            PoseStack poseStack,
            VertexConsumer vertexConsumer,
            ModelPart root,
            BipedEntityData<?> entityData,
            int packedLight,
            int packedOverlay)
    {
        ModelPart headPart = findPartByName(root, "head", "Head");
        if (headPart == null)
        {
            // Try to render entire root with head transform
            renderPartWithTransform(poseStack, vertexConsumer, root, entityData.body, entityData.head,
                    packedLight, packedOverlay);
            return;
        }

        // Render head with body + head transform chain
        poseStack.pushPose();
        ArmorPoseHelper.applyBodyTransformWithPivot(poseStack, entityData);
        ArmorPoseHelper.applyPartTransform(poseStack, entityData.head, true);
        ArmorPoseHelper.renderPartAtOrigin(headPart, poseStack, vertexConsumer, packedLight, packedOverlay);
        poseStack.popPose();

        // Also render hat if present
        ModelPart hatPart = findPartByName(root, "hat", "Hat");
        if (hatPart != null)
        {
            poseStack.pushPose();
            ArmorPoseHelper.applyBodyTransformWithPivot(poseStack, entityData);
            ArmorPoseHelper.applyPartTransform(poseStack, entityData.head, true);
            ArmorPoseHelper.renderPartAtOrigin(hatPart, poseStack, vertexConsumer, packedLight, packedOverlay);
            poseStack.popPose();
        }
    }

    /**
     * Render chest armor (body + arms).
     */
    private void renderChest(
            PoseStack poseStack,
            VertexConsumer vertexConsumer,
            ModelPart root,
            BipedEntityData<?> entityData,
            int packedLight,
            int packedOverlay)
    {
        // Render body - use renderPartAtOrigin to avoid vanilla position interfering with pivot rotation
        ModelPart bodyPart = findPartByName(root, "body", "Body", "torso", "Torso");
        if (bodyPart != null)
        {
            poseStack.pushPose();
            ArmorPoseHelper.applyBodyTransformWithPivot(poseStack, entityData);
            ArmorPoseHelper.renderPartAtOrigin(bodyPart, poseStack, vertexConsumer, packedLight, packedOverlay);
            poseStack.popPose();
        }

        // Render left arm with joint slicing
        ModelPart leftArmPart = findPartByName(root, "left_arm", "leftArm", "LeftArm");
        if (leftArmPart != null)
        {
            renderSplitArm(poseStack, vertexConsumer, leftArmPart, entityData, true, packedLight, packedOverlay);
        }

        // Render right arm with joint slicing
        ModelPart rightArmPart = findPartByName(root, "right_arm", "rightArm", "RightArm");
        if (rightArmPart != null)
        {
            renderSplitArm(poseStack, vertexConsumer, rightArmPart, entityData, false, packedLight, packedOverlay);
        }
    }

    /**
     * Render leg armor (body waist + legs).
     */
    private void renderLegs(
            PoseStack poseStack,
            VertexConsumer vertexConsumer,
            ModelPart root,
            BipedEntityData<?> entityData,
            int packedLight,
            int packedOverlay)
    {
        // Render body waist - use renderPartAtOrigin to avoid vanilla position interfering with pivot rotation
        ModelPart bodyPart = findPartByName(root, "body", "Body", "torso", "Torso");
        if (bodyPart != null)
        {
            poseStack.pushPose();
            ArmorPoseHelper.applyBodyTransformWithPivot(poseStack, entityData);
            ArmorPoseHelper.renderPartAtOrigin(bodyPart, poseStack, vertexConsumer, packedLight, packedOverlay);
            poseStack.popPose();
        }

        // Render left leg with joint slicing
        ModelPart leftLegPart = findPartByName(root, "left_leg", "leftLeg", "LeftLeg");
        if (leftLegPart != null)
        {
            renderSplitLeg(poseStack, vertexConsumer, leftLegPart, entityData, true, packedLight, packedOverlay);
        }

        // Render right leg with joint slicing
        ModelPart rightLegPart = findPartByName(root, "right_leg", "rightLeg", "RightLeg");
        if (rightLegPart != null)
        {
            renderSplitLeg(poseStack, vertexConsumer, rightLegPart, entityData, false, packedLight, packedOverlay);
        }
    }

    /**
     * Render feet armor (lower legs only).
     */
    private void renderFeet(
            PoseStack poseStack,
            VertexConsumer vertexConsumer,
            ModelPart root,
            BipedEntityData<?> entityData,
            int packedLight,
            int packedOverlay)
    {
        // Render left leg with joint slicing
        ModelPart leftLegPart = findPartByName(root, "left_leg", "leftLeg", "LeftLeg");
        if (leftLegPart != null)
        {
            renderSplitLeg(poseStack, vertexConsumer, leftLegPart, entityData, true, packedLight, packedOverlay);
        }

        // Render right leg with joint slicing
        ModelPart rightLegPart = findPartByName(root, "right_leg", "rightLeg", "RightLeg");
        if (rightLegPart != null)
        {
            renderSplitLeg(poseStack, vertexConsumer, rightLegPart, entityData, false, packedLight, packedOverlay);
        }
    }

    /**
     * Render an arm split at the elbow joint.
     * Upper arm geometry follows upperArm transform, forearm geometry follows foreArm transform.
     */
    private void renderSplitArm(
            PoseStack poseStack,
            VertexConsumer vertexConsumer,
            ModelPart armPart,
            BipedEntityData<?> entityData,
            boolean isLeft,
            int packedLight,
            int packedOverlay)
    {
        ModelPartTransform upperArm = isLeft ? entityData.leftArm : entityData.rightArm;
        ModelPartTransform foreArm = isLeft ? entityData.leftForeArm : entityData.rightForeArm;
        JointPlane elbowPlane = JointDefinitions.getElbow(isLeft);

        // 1. Capture arm geometry at rest pose
        limbCapture.clear();
        PoseStack captureStack = new PoseStack();
        float[] storage = new float[6];
        ArmorPoseHelper.resetPartToOrigin(armPart, storage);
        armPart.render(captureStack, limbCapture, packedLight, packedOverlay);
        ArmorPoseHelper.restorePartFromStorage(armPart, storage);

        List<CapturedVertex> vertices = limbCapture.getVertices();
        if (vertices.isEmpty())
        {
            return;
        }

        // 2. Slice at elbow
        List<CapturedVertex[]> quads = ArmorPoseHelper.groupIntoQuads(vertices);
        List<SliceResult> sliceResults = quadSlicer.sliceAll(quads, elbowPlane);

        // 3. Render upper arm portion
        poseStack.pushPose();
        ArmorPoseHelper.applyPartTransform(poseStack, entityData.body, true);
        ArmorPoseHelper.applyPartTransform(poseStack, upperArm, true);
        ArmorPoseHelper.renderSlicedVertices(poseStack, vertexConsumer, sliceResults, true, 0, 0, 0, packedLight, packedOverlay, currentArmorColor);
        poseStack.popPose();

        // 4. Render forearm portion
        float foreArmOffsetX = -foreArm.position.x * ArmorPoseHelper.SCALE;
        float foreArmOffsetY = -foreArm.position.y * ArmorPoseHelper.SCALE;
        float foreArmOffsetZ = -foreArm.position.z * ArmorPoseHelper.SCALE;
        poseStack.pushPose();
        ArmorPoseHelper.applyPartTransform(poseStack, entityData.body, true);
        ArmorPoseHelper.applyPartTransform(poseStack, upperArm, true);
        ArmorPoseHelper.applyPartTransform(poseStack, foreArm, true);
        ArmorPoseHelper.renderSlicedVertices(poseStack, vertexConsumer, sliceResults, false, foreArmOffsetX, foreArmOffsetY, foreArmOffsetZ, packedLight, packedOverlay, currentArmorColor);
        poseStack.popPose();
    }

    /**
     * Render a leg split at the knee joint.
     * Upper leg geometry follows leg transform, lower leg geometry follows foreLeg transform.
     */
    private void renderSplitLeg(
            PoseStack poseStack,
            VertexConsumer vertexConsumer,
            ModelPart legPart,
            BipedEntityData<?> entityData,
            boolean isLeft,
            int packedLight,
            int packedOverlay)
    {
        ModelPartTransform upperLeg = isLeft ? entityData.leftLeg : entityData.rightLeg;
        ModelPartTransform lowerLeg = isLeft ? entityData.leftForeLeg : entityData.rightForeLeg;
        JointPlane kneePlane = JointDefinitions.getKnee(isLeft);

        // 1. Capture leg geometry at rest pose
        limbCapture.clear();
        PoseStack captureStack = new PoseStack();
        float[] storage = new float[6];
        ArmorPoseHelper.resetPartToOrigin(legPart, storage);
        legPart.render(captureStack, limbCapture, packedLight, packedOverlay);
        ArmorPoseHelper.restorePartFromStorage(legPart, storage);

        List<CapturedVertex> vertices = limbCapture.getVertices();
        if (vertices.isEmpty())
        {
            return;
        }

        // 2. Slice at knee
        List<CapturedVertex[]> quads = ArmorPoseHelper.groupIntoQuads(vertices);
        List<SliceResult> sliceResults = quadSlicer.sliceAll(quads, kneePlane);

        // Use the vanilla armor model's leg X position (stored in storage[0] from resetPartToOrigin)
        // This ensures leg armor renders at the correct horizontal position regardless of
        // what Mo'Bends entityData has. Legs are root parts and should use vanilla positioning.
        float vanillaLegX = storage[0];

        // 3. Render upper leg portion
        poseStack.pushPose();
        ArmorPoseHelper.applyLegTransform(poseStack, upperLeg, vanillaLegX);
        ArmorPoseHelper.renderSlicedVertices(poseStack, vertexConsumer, sliceResults, true, 0, 0, 0, packedLight, packedOverlay, currentArmorColor);
        poseStack.popPose();

        // 4. Render lower leg portion
        float lowerLegOffsetX = -lowerLeg.position.x * ArmorPoseHelper.SCALE;
        float lowerLegOffsetY = -lowerLeg.position.y * ArmorPoseHelper.SCALE;
        float lowerLegOffsetZ = -lowerLeg.position.z * ArmorPoseHelper.SCALE;
        poseStack.pushPose();
        ArmorPoseHelper.applyLegTransform(poseStack, upperLeg, vanillaLegX);
        ArmorPoseHelper.applyPartTransform(poseStack, lowerLeg, true);
        ArmorPoseHelper.renderSlicedVertices(poseStack, vertexConsumer, sliceResults, false, lowerLegOffsetX, lowerLegOffsetY, lowerLegOffsetZ, packedLight, packedOverlay, currentArmorColor);
        poseStack.popPose();
    }

    /**
     * Render a part with body and part transform chain.
     */
    private void renderPartWithTransform(
            PoseStack poseStack,
            VertexConsumer vertexConsumer,
            ModelPart part,
            ModelPartTransform bodyTransform,
            ModelPartTransform partTransform,
            int packedLight,
            int packedOverlay)
    {
        poseStack.pushPose();
        if (bodyTransform != null)
        {
            ArmorPoseHelper.applyPartTransform(poseStack, bodyTransform, true);
        }
        if (partTransform != null)
        {
            ArmorPoseHelper.applyPartTransform(poseStack, partTransform, true);
        }
        ArmorPoseHelper.renderPartAtOrigin(part, poseStack, vertexConsumer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    /**
     * Render without Mo'Bends transforms (vanilla fallback).
     */
    private <E extends LivingEntity> void renderVanillaFallback(
            ArmorRenderContext<E> context,
            Model model,
            VertexConsumer vertexConsumer)
    {
        PoseStack poseStack = context.getPoseStack();
        poseStack.pushPose();
        model.renderToBuffer(
                poseStack,
                vertexConsumer,
                context.getPackedLight(),
                context.getPackedOverlay(),
                context.getArmorColor()
        );
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
     * Get the total number of render calls.
     */
    public long getRenderCount()
    {
        return renderCount;
    }

    /**
     * Reset statistics.
     */
    public void resetStats()
    {
        renderCount = 0;
    }

    /**
     * Get debug statistics.
     */
    public String getStats()
    {
        return String.format("Tier2Renderer: %d renders", renderCount);
    }
}
