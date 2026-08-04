package goblinbob.mobends.standard.client.renderer.entity.layers;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import goblinbob.mobends.api.player.IPlayerSkinProvider;
import goblinbob.mobends.core.data.EntityData;
import goblinbob.mobends.core.data.EntityDatabase;
import goblinbob.mobends.standard.client.renderer.entity.BendsCapeRenderer;
import goblinbob.mobends.standard.data.PlayerData;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Custom cape layer for Mo' Bends animated cape rendering.
 * Updated for 1.20.1 to use PoseStack and RenderSystem instead of GlStateManager.
 */
public class LayerCustomCape extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>>
{

    private final BendsCapeRenderer capeRenderer;

    public LayerCustomCape(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderer)
    {
        super(renderer);
        this.capeRenderer = new BendsCapeRenderer();
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

        IPlayerSkinProvider skinProvider = IPlayerSkinProvider.Holder.getProvider();
        ResourceLocation capeTexture = skinProvider != null ? (ResourceLocation) skinProvider.getCapeTexture(player) : null;
        if (capeTexture != null && !player.isInvisible() && player.isModelPartShown(PlayerModelPart.CAPE))
        {
            final ItemStack itemstack = player.getItemBySlot(EquipmentSlot.CHEST);

            if (!itemstack.is(Items.ELYTRA))
            {
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                poseStack.pushPose();

                data.body.applyLocalTransform(poseStack, 0.0625F);
                poseStack.translate(0.0F, -12.0F * scale, 2.2F * scale);
                data.cape.applyLocalTransform(poseStack, 0.0625F);
                poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));

                capeRenderer.applyAnimation(data);
                capeRenderer.render(poseStack, bufferSource, packedLight, player, 0.0625F);

                poseStack.popPose();
            }
        }
    }
}
