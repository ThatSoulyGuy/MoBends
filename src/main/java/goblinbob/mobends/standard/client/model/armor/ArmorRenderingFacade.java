package goblinbob.mobends.standard.client.model.armor;

import com.mojang.blaze3d.vertex.PoseStack;
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
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;

public class ArmorRenderingFacade
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ArmorRenderingFacade.class);

    private final Tier1Renderer tier1Renderer;
    private final Tier2Renderer tier2Renderer;

    public ArmorRenderingFacade()
    {
        this.tier1Renderer = new Tier1Renderer();
        this.tier2Renderer = new Tier2Renderer();
    }

    public <T extends LivingEntity> boolean renderArmor(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            T entity,
            EquipmentSlot slot,
            ItemStack armorStack,
            Model armorModel,
            BipedEntityData<?> entityData,
            ResourceLocation texture)
    {
        return renderArmor(poseStack, bufferSource, packedLight, entity, slot, armorStack,
                armorModel, entityData, texture, null);
    }

    public <T extends LivingEntity> boolean renderArmor(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            T entity,
            EquipmentSlot slot,
            ItemStack armorStack,
            Model armorModel,
            BipedEntityData<?> entityData,
            ResourceLocation texture,
            @Nullable Integer colorOverride)
    {
        if (texture == null || armorModel == null || entityData == null)
        {
            return false;
        }

        ArmorRenderContext<T> context = new ArmorRenderContext<>(entity, entityData, slot, armorStack,
                poseStack, bufferSource, packedLight, OverlayTexture.NO_OVERLAY, colorOverride);

        return renderWithTexture(context, armorModel, texture, armorStack.hasFoil());
    }

    public <T extends LivingEntity> boolean renderArmorLayer(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            T entity,
            EquipmentSlot slot,
            ItemStack armorStack,
            Model layerModel,
            BipedEntityData<?> entityData,
            ResourceLocation texture,
            @Nullable Integer colorOverride,
            java.util.function.Function<ResourceLocation, RenderType> renderTypeProvider)
    {
        if (texture == null || layerModel == null || entityData == null || renderTypeProvider == null)
        {
            return false;
        }

        ArmorRenderContext<T> context = new ArmorRenderContext<>(entity, entityData, slot, armorStack,
                poseStack, bufferSource, packedLight, OverlayTexture.NO_OVERLAY, colorOverride);

        try
        {
            tier1Renderer.render(context, layerModel, texture, renderTypeProvider);
            return true;
        }
        catch (Exception e)
        {
            LOGGER.error("Error rendering armor layer: {}", e.getMessage());
            return false;
        }
    }

    public <T extends LivingEntity> boolean renderArmorIntoConsumer(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            com.mojang.blaze3d.vertex.VertexConsumer vertexConsumer,
            int packedLight,
            int packedOverlay,
            T entity,
            EquipmentSlot slot,
            ItemStack armorStack,
            HumanoidModel<?> armorModel,
            BipedEntityData<?> entityData,
            @Nullable Integer colorOverride)
    {
        if (armorModel == null || entityData == null || vertexConsumer == null)
        {
            return false;
        }

        ArmorRenderContext<T> context = new ArmorRenderContext<>(entity, entityData, slot, armorStack,
                poseStack, bufferSource, packedLight, packedOverlay, colorOverride);

        return tier1Renderer.renderWithConsumer(context, armorModel, vertexConsumer);
    }

    private <T extends LivingEntity> boolean renderWithTexture(
            ArmorRenderContext<T> context,
            Model armorModel,
            ResourceLocation texture,
            boolean hasFoil)
    {
        boolean success = false;
        try
        {
            if (armorModel instanceof HumanoidModel<?> humanoidModel)
            {
                success = tier1Renderer.renderWithTexture(context, humanoidModel, texture, hasFoil);
            }

            if (!success)
            {
                success = tier2Renderer.renderWithTexture(context, armorModel, texture, hasFoil);
            }
        }
        catch (Exception e)
        {
            LOGGER.error("Error rendering armor with texture: {}", e.getMessage());
        }

        return success;
    }
}
