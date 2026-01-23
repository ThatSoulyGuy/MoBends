package goblinbob.mobends.standard.client.model.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import goblinbob.mobends.standard.client.model.armor.cache.CacheManager;
import goblinbob.mobends.standard.client.model.armor.tier.RenderTier;
import goblinbob.mobends.standard.client.model.armor.tier.TierClassifier;
import goblinbob.mobends.standard.client.model.armor.tier1.Tier1Renderer;
import goblinbob.mobends.standard.client.model.armor.tier2.Tier2Renderer;
import goblinbob.mobends.standard.data.BipedEntityData;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;

/**
 * Unified facade for the two-tier armor rendering system.
 * This class coordinates tier selection and delegates to the appropriate renderer.
 *
 * <p>Tier selection:</p>
 * <ul>
 *   <li>Tier 1 (Standard): Vanilla/standard HumanoidModel armor with joint slicing</li>
 *   <li>Tier 2 (Custom Model): Modded armor with custom 3D models</li>
 * </ul>
 */
@OnlyIn(Dist.CLIENT)
public class ArmorRenderingFacade
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ArmorRenderingFacade.class);

    // Tier renderers
    private final Tier1Renderer tier1Renderer;
    private final Tier2Renderer tier2Renderer;

    // Performance tracking
    private long tier1Count = 0;
    private long tier2Count = 0;

    // Debug mode
    private boolean debugMode = false;
    private RenderTier forcedTier = null;

    public ArmorRenderingFacade()
    {
        this.tier1Renderer = new Tier1Renderer();
        this.tier2Renderer = new Tier2Renderer();
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
     * Render armor with the specified tier.
     */
    @SuppressWarnings("unchecked")
    private <T extends LivingEntity> boolean renderWithTier(RenderTier tier, ArmorRenderContext<T> context, Model armorModel)
    {
        switch (tier)
        {
            case TIER_1_STANDARD:
                if (armorModel instanceof HumanoidModel<?>)
                {
                    return tier1Renderer.render(context, (HumanoidModel<?>) armorModel);
                }
                // Fall through to Tier 2 if not HumanoidModel
                LOGGER.debug("Model not HumanoidModel, falling back to Tier 2");
                // Fall through

            case TIER_2_CUSTOM_MODEL:
            default:
                return tier2Renderer.render(context, armorModel);
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
            return;
        }

        switch (tier)
        {
            case TIER_1_STANDARD:
                tier1Count++;
                break;
            case TIER_2_CUSTOM_MODEL:
                tier2Count++;
                break;
        }
    }

    /**
     * Render armor using the context built from parameters.
     * Convenience method for LayerCustomBipedArmor integration.
     * Uses default white color (no tint).
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

        // Build render context with default white color
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
                .armorColor(0xFFFFFFFF) // White, fully opaque = no tint
                .build();

        return renderWithTexture(context, armorModel, texture, armorStack.hasFoil());
    }

    /**
     * Render armor with color tint using the context built from parameters.
     * Used for dyeable armor like leather.
     *
     * @param red Red color component (0.0-1.0)
     * @param green Green color component (0.0-1.0)
     * @param blue Blue color component (0.0-1.0)
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
            ResourceLocation texture,
            float red, float green, float blue)
    {
        if (texture == null || armorModel == null || entityData == null)
        {
            return false;
        }

        // Build render context with color from RGB floats
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
                .armorColor(red, green, blue) // Set color from RGB floats
                .build();

        return renderWithTexture(context, armorModel, texture, armorStack.hasFoil());
    }

    /**
     * Render with a specific texture.
     * Color tinting is applied via context.getArmorColor().
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
            int color = context.getArmorColor();
            LOGGER.debug("Rendering armor with texture {} color 0x{} using {}",
                    texture, Integer.toHexString(color), tier);
        }

        boolean success = false;
        try
        {
            switch (tier)
            {
                case TIER_1_STANDARD:
                    if (armorModel instanceof HumanoidModel<?>)
                    {
                        success = tier1Renderer.renderWithTexture(context, (HumanoidModel<?>) armorModel,
                                texture, hasFoil);
                        if (success)
                        {
                            break;
                        }
                    }
                    // Fall through to Tier 2

                case TIER_2_CUSTOM_MODEL:
                default:
                    success = tier2Renderer.renderWithTexture(context, armorModel, texture, hasFoil);
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

    /**
     * Render armor with a custom VertexConsumer (for trims, glint overlays, etc.).
     * Uses the same per-bone transform approach as regular armor rendering.
     * This allows trims to follow Mo'Bends animations.
     *
     * @param context The armor render context
     * @param armorModel The armor model to render
     * @param vertexConsumer The vertex consumer to render to
     * @return true if rendering was handled, false to fall back to vanilla
     */
    public <T extends LivingEntity> boolean renderWithVertexConsumer(
            ArmorRenderContext<T> context,
            Model armorModel,
            VertexConsumer vertexConsumer)
    {
        if (context == null || armorModel == null || vertexConsumer == null)
        {
            return false;
        }

        // Only Tier 1 supports custom vertex consumer rendering currently
        if (armorModel instanceof HumanoidModel<?>)
        {
            return tier1Renderer.renderWithVertexConsumer(context, (HumanoidModel<?>) armorModel, vertexConsumer);
        }

        // Tier 2 doesn't support this yet - return false
        return false;
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
        return new RenderingStats(tier1Count, tier2Count);
    }

    /**
     * Reset rendering statistics.
     */
    public void resetStats()
    {
        tier1Count = 0;
        tier2Count = 0;
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
     * Statistics about armor rendering.
     */
    public static class RenderingStats
    {
        public final long tier1Count;
        public final long tier2Count;

        public RenderingStats(long tier1, long tier2)
        {
            this.tier1Count = tier1;
            this.tier2Count = tier2;
        }

        public long totalCount()
        {
            return tier1Count + tier2Count;
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

        @Override
        public String toString()
        {
            return String.format("ArmorRendering[Tier1 (Standard): %d (%.1f%%), Tier2 (Custom): %d (%.1f%%)]",
                    tier1Count, tier1Percentage(),
                    tier2Count, tier2Percentage());
        }
    }
}
