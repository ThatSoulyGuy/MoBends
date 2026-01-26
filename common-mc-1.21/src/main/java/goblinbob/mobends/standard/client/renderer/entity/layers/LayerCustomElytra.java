package goblinbob.mobends.standard.client.renderer.entity.layers;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import goblinbob.mobends.api.player.IPlayerSkinProvider;
import goblinbob.mobends.api.rendering.IModelRenderHelper;
import goblinbob.mobends.core.data.EntityData;
import goblinbob.mobends.core.data.EntityDatabase;
import goblinbob.mobends.standard.data.PlayerData;
import net.minecraft.client.model.ElytraModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Custom elytra layer for Mo' Bends animated elytra rendering.
 * Updated for 1.20.1 to use PoseStack and RenderSystem instead of GlStateManager.
 */
public class LayerCustomElytra extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>>
{
    /** The basic Elytra texture. */
    private static final ResourceLocation TEXTURE_ELYTRA = goblinbob.mobends.core.util.ResourceLocationFactory.parse("textures/entity/elytra.png");
    /** The model used by the Elytra. */
    private final ElytraModel<AbstractClientPlayer> elytraModel;

    public LayerCustomElytra(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderer, EntityModelSet modelSet)
    {
        super(renderer);
        this.elytraModel = new ElytraModel<>(modelSet.bakeLayer(ModelLayers.ELYTRA));
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
                       AbstractClientPlayer player, float limbSwing, float limbSwingAmount,
                       float partialTicks, float ageInTicks, float netHeadYaw, float headPitch)
    {
        final EntityData<?> entityData = EntityDatabase.instance.get(player);
        if (!(entityData instanceof PlayerData))
            return;

        final PlayerData data = (PlayerData) entityData;
        final float scale = 0.0625F;

        ItemStack itemstack = player.getItemBySlot(EquipmentSlot.CHEST);

        if (itemstack.is(Items.ELYTRA))
        {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();

            IPlayerSkinProvider skinProvider = IPlayerSkinProvider.Holder.getProvider();
            ResourceLocation texture;
            ResourceLocation elytraTexture = skinProvider != null ? (ResourceLocation) skinProvider.getElytraTexture(player) : null;
            ResourceLocation capeTexture = skinProvider != null ? (ResourceLocation) skinProvider.getCapeTexture(player) : null;
            if (elytraTexture != null)
            {
                texture = elytraTexture;
            }
            else if (capeTexture != null && player.isModelPartShown(PlayerModelPart.CAPE))
            {
                texture = capeTexture;
            }
            else
            {
                texture = TEXTURE_ELYTRA;
            }

            poseStack.pushPose();
            data.body.applyCharacterTransform(poseStack, 0.0625F);
            poseStack.translate(0.0F, -12.0F * scale, 0.0F);

            this.elytraModel.setupAnim(player, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

            IModelRenderHelper renderHelper = IModelRenderHelper.Holder.getHelper();
            VertexConsumer vertexConsumer = (VertexConsumer) renderHelper.getArmorFoilBuffer(
                    bufferSource, RenderType.armorCutoutNoCull(texture), itemstack.hasFoil());
            renderHelper.renderModelToBuffer(this.elytraModel, poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY,
                    0xFFFFFFFF);

            RenderSystem.disableBlend();
            poseStack.popPose();
        }
    }
}
