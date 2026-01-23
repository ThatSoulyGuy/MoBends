package goblinbob.mobends.standard.client.model.armor.tier2;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import goblinbob.mobends.standard.client.model.armor.ArmorRenderContext;
import goblinbob.mobends.standard.client.model.armor.cache.ArmorStructureCache;
import goblinbob.mobends.standard.client.model.armor.cache.CacheManager;
import goblinbob.mobends.standard.client.model.armor.tier.PartClassification;
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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Tier 2 renderer: Custom Model armor.
 *
 * This renderer works with armor models that don't extend HumanoidModel.
 * It analyzes the model structure spatially to classify parts, then applies transforms
 * by modifying ModelPart values before vanilla rendering.
 *
 * Approach:
 * 1. Analyze model structure (cached) to classify parts into bone regions
 * 2. For each classified part, apply Mo'Bends animation transform
 * 3. Render the model normally (vanilla render with modified part values)
 * 4. Restore original part values after render
 */
@OnlyIn(Dist.CLIENT)
public class Tier2Renderer
{
    private final SpatialAnalyzer spatialAnalyzer;
    private final ModelPartTransformer modelPartTransformer;

    // Performance tracking
    private long renderCount = 0;
    private long unclassifiedCount = 0;

    public Tier2Renderer()
    {
        this.spatialAnalyzer = new SpatialAnalyzer();
        this.modelPartTransformer = new ModelPartTransformer();
    }

    public Tier2Renderer(SpatialAnalyzer spatialAnalyzer, ModelPartTransformer modelPartTransformer)
    {
        this.spatialAnalyzer = spatialAnalyzer;
        this.modelPartTransformer = modelPartTransformer;
    }

    /**
     * Get the tier this renderer handles.
     */
    public RenderTier getTier()
    {
        return RenderTier.TIER_2_CUSTOM_MODEL;
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
            VertexConsumer vertexConsumer = ItemRenderer.getArmorFoilBuffer(
                    context.getBufferSource(),
                    RenderType.armorCutoutNoCull(texture),
                    false,  // No outer layer (handled separately if needed)
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
     * Render armor with a pre-configured VertexConsumer (for foil support).
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

        // Get or compute part classifications
        Map<String, PartClassification> classifications = getClassifications(model);

        // If we can't classify the model, render it without transforms
        if (!hasValidClassifications(classifications, context.getSlot()))
        {
            unclassifiedCount++;
            renderWithoutTransforms(context.getPoseStack(), context, model, vertexConsumer);
            return;
        }

        BipedEntityData<?> entityData = context.getEntityData();
        PoseStack poseStack = context.getPoseStack();

        // Store original part values
        Map<String, OriginalPartState> originalStates = storePartStates(model, classifications);

        try
        {
            // Apply transforms to all classified parts
            applyTransforms(model, classifications, entityData);

            // Render with modified transforms
            poseStack.pushPose();

            // Apply baby scale if needed (babies render at 0.5 scale)
            float entityScale = context.getEntityScale();
            if (entityScale != 1.0f)
            {
                poseStack.scale(entityScale, entityScale, entityScale);
            }

            // Note: Global transforms (globalOffset, centerRotation, renderRotation, localOffset)
            // are already applied by MutatedRenderer.beforeRender() to the PoseStack before armor renders.

            // Extract color from context (packed ARGB)
            int color = context.getArmorColor();
            float alpha = ((color >> 24) & 0xFF) / 255.0f;
            float red = ((color >> 16) & 0xFF) / 255.0f;
            float green = ((color >> 8) & 0xFF) / 255.0f;
            float blue = (color & 0xFF) / 255.0f;

            model.renderToBuffer(
                    poseStack,
                    vertexConsumer,
                    context.getPackedLight(),
                    context.getPackedOverlay(),
                    red, green, blue, alpha
            );

            poseStack.popPose();

            // Record cache statistics
            CacheManager.getInstance().recordCacheAssistedRender();
        }
        finally
        {
            // Restore original part states
            restorePartStates(model, originalStates);
        }
    }

    /**
     * Render without applying Mo'Bends transforms (for unclassified models).
     */
    private <E extends LivingEntity> void renderWithoutTransforms(
            PoseStack poseStack,
            ArmorRenderContext<E> context,
            Model model,
            VertexConsumer vertexConsumer)
    {
        poseStack.pushPose();

        // Apply baby scale if needed
        float entityScale = context.getEntityScale();
        if (entityScale != 1.0f)
        {
            poseStack.scale(entityScale, entityScale, entityScale);
        }

        // Extract color from context (packed ARGB)
        int color = context.getArmorColor();
        float alpha = ((color >> 24) & 0xFF) / 255.0f;
        float red = ((color >> 16) & 0xFF) / 255.0f;
        float green = ((color >> 8) & 0xFF) / 255.0f;
        float blue = (color & 0xFF) / 255.0f;

        model.renderToBuffer(
                poseStack,
                vertexConsumer,
                context.getPackedLight(),
                context.getPackedOverlay(),
                red, green, blue, alpha
        );

        poseStack.popPose();
    }

    /**
     * Render armor using the model interception approach.
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

        renderCount++;

        // Get or compute part classifications
        Map<String, PartClassification> classifications = getClassifications(model);

        // If we can't classify the model, render it without transforms
        if (!hasValidClassifications(classifications, context.getSlot()))
        {
            unclassifiedCount++;
            renderVanilla(context, model, texture, renderTypeProvider);
            return;
        }

        BipedEntityData<?> entityData = context.getEntityData();
        PoseStack poseStack = context.getPoseStack();
        MultiBufferSource bufferSource = context.getBufferSource();

        // Store original part values
        Map<String, OriginalPartState> originalStates = storePartStates(model, classifications);

        try
        {
            // Apply transforms to all classified parts
            applyTransforms(model, classifications, entityData);

            // Render with modified transforms
            poseStack.pushPose();

            // Apply baby scale if needed (babies render at 0.5 scale)
            float entityScale = context.getEntityScale();
            if (entityScale != 1.0f)
            {
                poseStack.scale(entityScale, entityScale, entityScale);
            }

            // Note: Global transforms (globalOffset, centerRotation, renderRotation, localOffset)
            // are already applied by MutatedRenderer.beforeRender() to the PoseStack before armor renders.

            RenderType renderType = renderTypeProvider.apply(texture);
            VertexConsumer vertexConsumer = bufferSource.getBuffer(renderType);

            // Extract color from context (packed ARGB)
            int color = context.getArmorColor();
            float alpha = ((color >> 24) & 0xFF) / 255.0f;
            float red = ((color >> 16) & 0xFF) / 255.0f;
            float green = ((color >> 8) & 0xFF) / 255.0f;
            float blue = (color & 0xFF) / 255.0f;

            model.renderToBuffer(
                    poseStack,
                    vertexConsumer,
                    context.getPackedLight(),
                    context.getPackedOverlay(),
                    red, green, blue, alpha
            );

            poseStack.popPose();

            // Record cache statistics
            CacheManager.getInstance().recordCacheAssistedRender();
        }
        finally
        {
            // Restore original part states
            restorePartStates(model, originalStates);
        }
    }

    /**
     * Get cached classifications or analyze the model.
     */
    private Map<String, PartClassification> getClassifications(Model model)
    {
        Class<?> modelClass = model.getClass();

        // Check structure cache first
        ArmorStructureCache cache = CacheManager.getInstance().getStructureCache();
        ArmorStructureCache.StructureEntry cached = cache.get(modelClass);
        if (cached != null && !cached.getPartClassifications().isEmpty())
        {
            return cached.getPartClassifications();
        }

        // Analyze and cache
        Map<String, PartClassification> classifications = spatialAnalyzer.analyzeModel(model);

        // Cache the results
        ArmorStructureCache.StructureEntry entry = new ArmorStructureCache.StructureEntry(
                modelClass,
                RenderTier.TIER_2_CUSTOM_MODEL,
                classifications,
                extractBoneMap(classifications),
                false,
                false
        );
        cache.put(modelClass, entry);

        return classifications;
    }

    /**
     * Check if classifications are valid enough for rendering.
     */
    private boolean hasValidClassifications(Map<String, PartClassification> classifications, EquipmentSlot slot)
    {
        if (classifications.isEmpty())
        {
            return false;
        }

        // Count high-confidence classifications
        int highConfidenceCount = 0;
        for (PartClassification classification : classifications.values())
        {
            if (classification.isModerateConfidence())
            {
                highConfidenceCount++;
            }
        }

        // Require at least one high-confidence classification
        return highConfidenceCount >= 1;
    }

    /**
     * Store original part states before transformation.
     */
    private Map<String, OriginalPartState> storePartStates(Model model, Map<String, PartClassification> classifications)
    {
        Map<String, OriginalPartState> states = new HashMap<>();

        for (Map.Entry<String, PartClassification> entry : classifications.entrySet())
        {
            String fieldName = entry.getKey();
            ModelPart part = entry.getValue().getModelPart();

            if (part != null)
            {
                states.put(fieldName, new OriginalPartState(part));
            }
        }

        return states;
    }

    /**
     * Restore original part states after transformation.
     */
    private void restorePartStates(Model model, Map<String, OriginalPartState> states)
    {
        for (Map.Entry<String, OriginalPartState> entry : states.entrySet())
        {
            PartClassification classification = getClassificationForField(model, entry.getKey());
            if (classification != null && classification.getModelPart() != null)
            {
                entry.getValue().restore(classification.getModelPart());
            }
        }
    }

    /**
     * Apply transforms to all classified parts.
     */
    private void applyTransforms(Model model, Map<String, PartClassification> classifications, BipedEntityData<?> entityData)
    {
        for (PartClassification classification : classifications.values())
        {
            if (classification.isModerateConfidence())
            {
                modelPartTransformer.applyTransform(classification.getModelPart(), classification, entityData);
            }
        }
    }

    /**
     * Get classification for a specific field name.
     */
    private PartClassification getClassificationForField(Model model, String fieldName)
    {
        Map<String, PartClassification> classifications = getClassifications(model);
        return classifications.get(fieldName);
    }

    /**
     * Extract bone region map from classifications.
     */
    private Map<String, goblinbob.mobends.standard.client.model.armor.BoneRegion> extractBoneMap(Map<String, PartClassification> classifications)
    {
        Map<String, goblinbob.mobends.standard.client.model.armor.BoneRegion> boneMap = new HashMap<>();
        for (Map.Entry<String, PartClassification> entry : classifications.entrySet())
        {
            boneMap.put(entry.getKey(), entry.getValue().getBoneRegion());
        }
        return boneMap;
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

        // Extract color from context (packed ARGB)
        int color = context.getArmorColor();
        float alpha = ((color >> 24) & 0xFF) / 255.0f;
        float red = ((color >> 16) & 0xFF) / 255.0f;
        float green = ((color >> 8) & 0xFF) / 255.0f;
        float blue = (color & 0xFF) / 255.0f;

        model.renderToBuffer(
                poseStack,
                bufferSource.getBuffer(renderType),
                context.getPackedLight(),
                context.getPackedOverlay(),
                red, green, blue, alpha
        );

        poseStack.popPose();
    }

    /**
     * Get the spatial analyzer.
     */
    public SpatialAnalyzer getSpatialAnalyzer()
    {
        return spatialAnalyzer;
    }

    /**
     * Get the model part transformer.
     */
    public ModelPartTransformer getModelPartTransformer()
    {
        return modelPartTransformer;
    }

    /**
     * Get the total number of render calls.
     */
    public long getRenderCount()
    {
        return renderCount;
    }

    /**
     * Get the number of renders where model couldn't be classified.
     */
    public long getUnclassifiedCount()
    {
        return unclassifiedCount;
    }

    /**
     * Get the unclassified rate.
     */
    public float getUnclassifiedRate()
    {
        return renderCount > 0 ? (float) unclassifiedCount / renderCount : 0;
    }

    /**
     * Reset statistics.
     */
    public void resetStats()
    {
        renderCount = 0;
        unclassifiedCount = 0;
    }

    /**
     * Get debug statistics.
     */
    public String getStats()
    {
        return String.format("Tier2Renderer: %d renders, %d unclassified (%.1f%% unclassified rate)",
                renderCount, unclassifiedCount, getUnclassifiedRate() * 100);
    }

    /**
     * Stores original ModelPart state for restoration.
     */
    private static class OriginalPartState
    {
        float x, y, z;
        float xRot, yRot, zRot;
        float xScale, yScale, zScale;
        boolean visible;

        OriginalPartState(ModelPart part)
        {
            this.x = part.x;
            this.y = part.y;
            this.z = part.z;
            this.xRot = part.xRot;
            this.yRot = part.yRot;
            this.zRot = part.zRot;
            this.xScale = part.xScale;
            this.yScale = part.yScale;
            this.zScale = part.zScale;
            this.visible = part.visible;
        }

        void restore(ModelPart part)
        {
            part.x = x;
            part.y = y;
            part.z = z;
            part.xRot = xRot;
            part.yRot = yRot;
            part.zRot = zRot;
            part.xScale = xScale;
            part.yScale = yScale;
            part.zScale = zScale;
            part.visible = visible;
        }
    }
}
