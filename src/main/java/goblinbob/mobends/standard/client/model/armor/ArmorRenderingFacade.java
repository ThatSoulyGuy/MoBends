package goblinbob.mobends.standard.client.model.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import goblinbob.mobends.standard.client.model.armor.cache.CacheManager;
import goblinbob.mobends.standard.client.model.armor.tier.RenderTier;
import goblinbob.mobends.standard.client.model.armor.tier.TierClassifier;
import goblinbob.mobends.standard.client.model.armor.tier1.Tier1Renderer;
import goblinbob.mobends.standard.client.model.armor.tier2.Tier2Renderer;
import goblinbob.mobends.standard.client.model.armor.tier3.Tier3Renderer;
import goblinbob.mobends.standard.data.BipedEntityData;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;

/**
 * Unified facade for the three-tier armor rendering system.
 * This class coordinates tier selection and delegates to the appropriate renderer.
 *
 * <p>Tier selection priority:</p>
 * <ul>
 *   <li>Tier 1 (Transform Injection): ~85% of armor - vanilla/standard HumanoidModel</li>
 *   <li>Tier 2 (Model Interception): ~10% of armor - modded using ModelPart</li>
 *   <li>Tier 3 (Vertex Capture): ~5% of armor - exotic renderers</li>
 * </ul>
 */
@OnlyIn(Dist.CLIENT)
public class ArmorRenderingFacade
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ArmorRenderingFacade.class);

    // Tier renderers
    private final Tier1Renderer tier1Renderer;
    private final Tier2Renderer tier2Renderer;
    private final Tier3Renderer tier3Renderer;

    // Performance tracking
    private long tier1Count = 0;
    private long tier2Count = 0;
    private long tier3Count = 0;
    private long fallbackCount = 0;

    // Debug mode
    private boolean debugMode = false;
    private RenderTier forcedTier = null;

    public ArmorRenderingFacade()
    {
        this.tier1Renderer = new Tier1Renderer();
        this.tier2Renderer = new Tier2Renderer();
        this.tier3Renderer = new Tier3Renderer();
    }

    /**
     * Render armor using the appropriate tier for the given context.
     *
     * @param context The armor render context
     * @param armorModel The armor model to render
     * @return true if rendering was handled, false to fall back to vanilla
     */
    public <T extends LivingEntity> boolean render(ArmorRenderContext<T> context, Model armorModel)
    {
        if (context == null || armorModel == null)
        {
            return false;
        }

        // Determine which tier to use
        RenderTier tier = determineTier(armorModel);

        if (debugMode)
        {
            LOGGER.debug("Rendering armor for {} slot {} using {}",
                    context.getEntity().getClass().getSimpleName(),
                    context.getSlot(),
                    tier);
        }

        // Delegate to appropriate tier renderer
        boolean success = false;
        try
        {
            success = renderWithTier(tier, context, armorModel);
        }
        catch (Exception e)
        {
            LOGGER.error("Error rendering armor with {}: {}", tier, e.getMessage());
            if (debugMode)
            {
                e.printStackTrace();
            }
        }

        // Update statistics
        updateStats(tier, success);

        return success;
    }

    /**
     * Render armor with the specified tier, with automatic fallback.
     */
    @SuppressWarnings("unchecked")
    private <T extends LivingEntity> boolean renderWithTier(RenderTier tier, ArmorRenderContext<T> context, Model armorModel)
    {
        switch (tier)
        {
            case TIER_1_TRANSFORM_INJECTION:
                if (armorModel instanceof HumanoidModel<?>)
                {
                    boolean result = tier1Renderer.render(context, (HumanoidModel<?>) armorModel);
                    if (result)
                    {
                        return true;
                    }
                    // Fall through to Tier 2
                    LOGGER.debug("Tier 1 failed, falling back to Tier 2");
                }
                // Fall through

            case TIER_2_MODEL_INTERCEPTION:
                boolean result = tier2Renderer.render(context, armorModel);
                if (result)
                {
                    return true;
                }
                // Fall through to Tier 3
                if (tier == RenderTier.TIER_2_MODEL_INTERCEPTION)
                {
                    LOGGER.debug("Tier 2 failed, falling back to Tier 3");
                }
                // Fall through

            case TIER_3_VERTEX_CAPTURE:
            default:
                return tier3Renderer.render(context, armorModel);
        }
    }

    /**
     * Determine the appropriate tier for the given armor model.
     */
    private RenderTier determineTier(Model armorModel)
    {
        // Check for forced tier (debug mode)
        if (forcedTier != null)
        {
            return forcedTier;
        }

        return TierClassifier.getInstance().classify(armorModel);
    }

    /**
     * Update rendering statistics.
     */
    private void updateStats(RenderTier tier, boolean success)
    {
        if (!success)
        {
            fallbackCount++;
            return;
        }

        switch (tier)
        {
            case TIER_1_TRANSFORM_INJECTION:
                tier1Count++;
                break;
            case TIER_2_MODEL_INTERCEPTION:
                tier2Count++;
                break;
            case TIER_3_VERTEX_CAPTURE:
                tier3Count++;
                break;
        }
    }

    /**
     * Render armor using the context built from parameters.
     * Convenience method for LayerCustomBipedArmor integration.
     */
    @SuppressWarnings("unchecked")
    public <T extends LivingEntity> boolean renderArmor(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            T entity,
            EquipmentSlot slot,
            ItemStack armorStack,
            ArmorItem armorItem,
            HumanoidModel<?> armorModel,
            BipedEntityData<?> entityData,
            ResourceLocation texture)
    {
        if (texture == null || armorModel == null || entityData == null)
        {
            return false;
        }

        // Build render context
        ArmorRenderContext<T> context = ArmorRenderContext.<T>builder()
                .entity(entity)
                .entityData(entityData)
                .slot(slot)
                .armorStack(armorStack)
                .poseStack(poseStack)
                .bufferSource(bufferSource)
                .packedLight(packedLight)
                .packedOverlay(OverlayTexture.NO_OVERLAY)
                .partialTicks(0) // Will be set by caller if needed
                .armorModel((HumanoidModel<T>) armorModel)
                .build();

        // Set texture in context or handle separately
        return renderWithTexture(context, armorModel, texture, armorStack.hasFoil());
    }

    /**
     * Render with a specific texture.
     */
    private <T extends LivingEntity> boolean renderWithTexture(
            ArmorRenderContext<T> context,
            Model armorModel,
            ResourceLocation texture,
            boolean hasFoil)
    {
        // Determine tier and render
        RenderTier tier = determineTier(armorModel);

        if (debugMode)
        {
            LOGGER.debug("Rendering armor with texture {} using {}", texture, tier);
        }

        // For Tier 1 and 2, we can render directly
        // For Tier 3, we need to capture and re-render with proper texture
        boolean success = false;
        try
        {
            switch (tier)
            {
                case TIER_1_TRANSFORM_INJECTION:
                    if (armorModel instanceof HumanoidModel<?>)
                    {
                        success = tier1Renderer.renderWithTexture(context, (HumanoidModel<?>) armorModel, texture, hasFoil);
                        if (success)
                        {
                            break;
                        }
                    }
                    // Fall through to Tier 2

                case TIER_2_MODEL_INTERCEPTION:
                    success = tier2Renderer.renderWithTexture(context, armorModel, texture, hasFoil);
                    if (success)
                    {
                        break;
                    }
                    // Fall through to Tier 3

                case TIER_3_VERTEX_CAPTURE:
                default:
                    success = tier3Renderer.renderWithTexture(context, armorModel, texture, hasFoil);
                    break;
            }
        }
        catch (Exception e)
        {
            LOGGER.error("Error rendering armor with texture: {}", e.getMessage());
            if (debugMode)
            {
                e.printStackTrace();
            }
        }

        updateStats(tier, success);
        return success;
    }

    // === Debug and Statistics ===

    /**
     * Enable or disable debug mode.
     */
    public void setDebugMode(boolean enabled)
    {
        this.debugMode = enabled;
    }

    /**
     * Force a specific tier for all rendering (debug/testing).
     */
    public void forceTier(@Nullable RenderTier tier)
    {
        this.forcedTier = tier;
    }

    /**
     * Get rendering statistics.
     */
    public RenderingStats getStats()
    {
        return new RenderingStats(tier1Count, tier2Count, tier3Count, fallbackCount);
    }

    /**
     * Reset rendering statistics.
     */
    public void resetStats()
    {
        tier1Count = 0;
        tier2Count = 0;
        tier3Count = 0;
        fallbackCount = 0;
    }

    /**
     * Clear all caches.
     */
    public void clearCaches()
    {
        CacheManager.getInstance().clearAll();
    }

    /**
     * Get the Tier 1 renderer for advanced configuration.
     */
    public Tier1Renderer getTier1Renderer()
    {
        return tier1Renderer;
    }

    /**
     * Get the Tier 2 renderer for advanced configuration.
     */
    public Tier2Renderer getTier2Renderer()
    {
        return tier2Renderer;
    }

    /**
     * Get the Tier 3 renderer for advanced configuration.
     */
    public Tier3Renderer getTier3Renderer()
    {
        return tier3Renderer;
    }

    /**
     * Statistics about armor rendering.
     */
    public static class RenderingStats
    {
        public final long tier1Count;
        public final long tier2Count;
        public final long tier3Count;
        public final long fallbackCount;

        public RenderingStats(long tier1, long tier2, long tier3, long fallback)
        {
            this.tier1Count = tier1;
            this.tier2Count = tier2;
            this.tier3Count = tier3;
            this.fallbackCount = fallback;
        }

        public long totalCount()
        {
            return tier1Count + tier2Count + tier3Count;
        }

        public float tier1Percentage()
        {
            long total = totalCount();
            return total > 0 ? (float) tier1Count / total * 100 : 0;
        }

        public float tier2Percentage()
        {
            long total = totalCount();
            return total > 0 ? (float) tier2Count / total * 100 : 0;
        }

        public float tier3Percentage()
        {
            long total = totalCount();
            return total > 0 ? (float) tier3Count / total * 100 : 0;
        }

        @Override
        public String toString()
        {
            return String.format("ArmorRendering[T1: %d (%.1f%%), T2: %d (%.1f%%), T3: %d (%.1f%%), Fallbacks: %d]",
                    tier1Count, tier1Percentage(),
                    tier2Count, tier2Percentage(),
                    tier3Count, tier3Percentage(),
                    fallbackCount);
        }
    }
}
