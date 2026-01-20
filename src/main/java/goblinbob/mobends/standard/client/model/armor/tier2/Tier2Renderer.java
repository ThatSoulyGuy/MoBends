package goblinbob.mobends.standard.client.model.armor.tier2;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import goblinbob.mobends.standard.client.model.armor.ArmorRenderContext;
import goblinbob.mobends.standard.client.model.armor.cache.ArmorStructureCache;
import goblinbob.mobends.standard.client.model.armor.cache.CacheManager;
import goblinbob.mobends.standard.client.model.armor.tier.PartClassification;
import goblinbob.mobends.standard.client.model.armor.tier.RenderTier;
import goblinbob.mobends.standard.client.model.armor.tier3.Tier3Renderer;
import goblinbob.mobends.standard.data.BipedEntityData;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Tier 2 renderer: Model Interception.
 *
 * This renderer works with armor models that use ModelPart but don't extend HumanoidModel.
 * It analyzes the model structure spatially to classify parts, then applies transforms
 * by modifying ModelPart values before vanilla rendering.
 *
 * Approach:
 * 1. Analyze model structure (cached) to classify parts into bone regions
 * 2. For each classified part, apply Mo'Bends animation transform
 * 3. Render the model normally (vanilla render with modified part values)
 * 4. Restore original part values after render
 *
 * Falls back to Tier 3 if analysis fails or produces low-confidence results.
 */
@OnlyIn(Dist.CLIENT)
public class Tier2Renderer
{
    private final SpatialAnalyzer spatialAnalyzer;
    private final ModelPartTransformer modelPartTransformer;
    private final Tier3Renderer tier3Fallback;

    // Threshold for using analyzed results vs falling back to Tier 3
    private static final float MIN_CONFIDENCE = 0.5f;

    // Performance tracking
    private long renderCount = 0;
    private long fallbackCount = 0;

    public Tier2Renderer()
    {
        this.spatialAnalyzer = new SpatialAnalyzer();
        this.modelPartTransformer = new ModelPartTransformer();
        this.tier3Fallback = new Tier3Renderer();
    }

    public Tier2Renderer(SpatialAnalyzer spatialAnalyzer, ModelPartTransformer modelPartTransformer, Tier3Renderer tier3Fallback)
    {
        this.spatialAnalyzer = spatialAnalyzer;
        this.modelPartTransformer = modelPartTransformer;
        this.tier3Fallback = tier3Fallback;
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
            render(context, model, texture, tex -> RenderType.armorCutoutNoCull(tex));
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
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

        // Check if we have sufficient classifications
        if (!hasValidClassifications(classifications, context.getSlot()))
        {
            // Fall back to Tier 3
            fallbackCount++;
            tier3Fallback.render(context, model, texture, renderTypeProvider);
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

            // Apply global offset (for crouching, etc.) scaled by entityScale for babies
            // This matches MutatedRenderer.beforeRender() behavior
            applyGlobalOffset(poseStack, entityData, entityScale);

            RenderType renderType = renderTypeProvider.apply(texture);
            VertexConsumer vertexConsumer = bufferSource.getBuffer(renderType);

            model.renderToBuffer(
                    poseStack,
                    vertexConsumer,
                    context.getPackedLight(),
                    context.getPackedOverlay(),
                    1.0f, 1.0f, 1.0f, 1.0f
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
                RenderTier.TIER_2_MODEL_INTERCEPTION,
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
     * Get the Tier 3 fallback renderer.
     */
    public Tier3Renderer getTier3Fallback()
    {
        return tier3Fallback;
    }

    /**
     * Get the total number of render calls.
     */
    public long getRenderCount()
    {
        return renderCount;
    }

    /**
     * Get the number of renders that fell back to Tier 3.
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
        return String.format("Tier2Renderer: %d renders, %d fallbacks (%.1f%% fallback rate)",
                renderCount, fallbackCount, getFallbackRate() * 100);
    }

    private static final float SCALE = 1.0f / 16.0f;

    /**
     * Apply global offset from entity data, scaled by entityScale for baby entities.
     * This matches MutatedRenderer.beforeRender() behavior.
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
