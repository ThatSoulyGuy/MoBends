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
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;

public class ArmorRenderingFacade
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ArmorRenderingFacade.class);

    private final Tier1Renderer tier1Renderer;
    private final Tier2Renderer tier2Renderer;

    private long tier1Count = 0;
    private long tier2Count = 0;
    private long fallbackCount = 0;

    private boolean debugMode = false;
    private RenderTier forcedTier = null;

    public ArmorRenderingFacade()
    {
        this.tier1Renderer = new Tier1Renderer();
        this.tier2Renderer = new Tier2Renderer();
    }

    public <T extends LivingEntity> boolean render(ArmorRenderContext<T> context, Model armorModel)
    {
        if (context == null || armorModel == null)
        {
            return false;
        }

        RenderTier tier = determineTier(armorModel);

        if (debugMode)
        {
            LOGGER.debug("Rendering armor for {} slot {} using {}",
                    context.getEntity().getClass().getSimpleName(),
                    context.getSlot(),
                    tier);
        }

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

        updateStats(tier, success);

        return success;
    }

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
                    LOGGER.debug("Tier 1 failed, falling back to Tier 2");
                }

            case TIER_2_MODEL_INTERCEPTION:
            default:
                return tier2Renderer.render(context, armorModel);
        }
    }

    private RenderTier determineTier(Model armorModel)
    {
        if (forcedTier != null)
        {
            return forcedTier;
        }

        return TierClassifier.getInstance().classify(armorModel);
    }

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
        }
    }

    public <T extends LivingEntity> boolean renderArmor(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            T entity,
            EquipmentSlot slot,
            ItemStack armorStack,
            ArmorItem armorItem,
            Model armorModel,
            BipedEntityData<?> entityData,
            ResourceLocation texture)
    {
        if (texture == null || armorModel == null || entityData == null)
        {
            return false;
        }

        ArmorRenderContext<T> context = ArmorRenderContext.<T>builder()
                .entity(entity)
                .entityData(entityData)
                .slot(slot)
                .armorStack(armorStack)
                .poseStack(poseStack)
                .bufferSource(bufferSource)
                .packedLight(packedLight)
                .packedOverlay(OverlayTexture.NO_OVERLAY)
                .partialTicks(0)
                .armorModel(armorModel)
                .build();

        return renderWithTexture(context, armorModel, texture, armorStack.hasFoil());
    }

    private <T extends LivingEntity> boolean renderWithTexture(
            ArmorRenderContext<T> context,
            Model armorModel,
            ResourceLocation texture,
            boolean hasFoil)
    {
        RenderTier tier = determineTier(armorModel);

        if (debugMode)
        {
            LOGGER.debug("Rendering armor with texture {} using {}", texture, tier);
        }

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

                case TIER_2_MODEL_INTERCEPTION:
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

    public void setDebugMode(boolean enabled)
    {
        this.debugMode = enabled;
    }

    public void forceTier(@Nullable RenderTier tier)
    {
        this.forcedTier = tier;
    }

    public RenderingStats getStats()
    {
        return new RenderingStats(tier1Count, tier2Count, fallbackCount);
    }

    public void resetStats()
    {
        tier1Count = 0;
        tier2Count = 0;
        fallbackCount = 0;
    }

    public void clearCaches()
    {
        CacheManager.getInstance().clearAll();
    }

    public Tier1Renderer getTier1Renderer()
    {
        return tier1Renderer;
    }

    public Tier2Renderer getTier2Renderer()
    {
        return tier2Renderer;
    }

    public static class RenderingStats
    {
        public final long tier1Count;
        public final long tier2Count;
        public final long fallbackCount;

        public RenderingStats(long tier1, long tier2, long fallback)
        {
            this.tier1Count = tier1;
            this.tier2Count = tier2;
            this.fallbackCount = fallback;
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
            return String.format("ArmorRendering[T1: %d (%.1f%%), T2: %d (%.1f%%), Fallbacks: %d]",
                    tier1Count, tier1Percentage(),
                    tier2Count, tier2Percentage(),
                    fallbackCount);
        }
    }
}
