package goblinbob.mobends.neoforge.client.renderer.entity.layers;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import goblinbob.mobends.core.asset.AssetLocation;
import goblinbob.mobends.core.asset.AssetModels;
import goblinbob.mobends.core.client.model.IModelPart;
import goblinbob.mobends.core.data.EntityData;
import goblinbob.mobends.core.data.EntityDatabase;
import goblinbob.mobends.core.supporters.*;
import goblinbob.mobends.core.util.Color;
import goblinbob.mobends.standard.data.PlayerData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.BakedModel;
import org.joml.Vector3f;

import java.util.Map;
import java.util.Set;

public class LayerPlayerAccessories extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>>
{
    private final TextureManager textureManager;

    public LayerPlayerAccessories(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderer)
    {
        super(renderer);
        this.textureManager = Minecraft.getInstance().getTextureManager();
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
                       AbstractClientPlayer player, float limbSwing, float limbSwingAmount,
                       float partialTicks, float ageInTicks, float netHeadYaw, float headPitch)
    {
        Set<Map.Entry<String, AccessoryDetails>> accessories = SupporterContent.getAccessories();

        final EntityData<?> entityData = EntityDatabase.instance.get(player);
        if (!(entityData instanceof PlayerData))
            return;

        final PlayerData data = (PlayerData) entityData;
        final float scale = 0.0625F;

        Map<String, AccessorySettings> settingsMap = SupporterContent.getAccessorySettingsMapFor(player);

        for (Map.Entry<String, AccessoryDetails> entry : accessories)
        {
            AccessorySettings accessorySettings = settingsMap.getOrDefault(entry.getKey(), AccessorySettings.DEFAULT);
            renderAccessory(poseStack, bufferSource, packedLight, player, data, entry.getValue(), accessorySettings, scale);
        }
    }

    private void renderAccessory(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
                                 AbstractClientPlayer player, PlayerData data, AccessoryDetails accessory,
                                 AccessorySettings settings, float scale)
    {
        if (!settings.isUnlocked() || settings.isHidden())
        {
            return;
        }

        for (AccessoryPart part : accessory.getParts())
        {
            renderPart(poseStack, bufferSource, packedLight, player, data, part, settings, scale);
        }
    }

    private void renderPart(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
                            AbstractClientPlayer player, PlayerData data, AccessoryPart part,
                            AccessorySettings settings, float scale)
    {
        BakedModel bakedModel = AssetModels.INSTANCE.getModel(part.getModelPath());

        if (bakedModel == null)
        {
            return;
        }

        poseStack.pushPose();

        if (player.isCrouching())
        {
            if (player.getAbilities().flying)
            {
                poseStack.translate(0F, 4F * scale, 0F);
            }
            else
            {
                poseStack.translate(0F, 3F * scale, 0F);
            }
        }

        applyBindPointTransform(poseStack, data, part.getBindPoint(), scale);

        Vector3f translation = part.getTranslation();
        Vector3f rotation = part.getRotation();
        Vector3f scaleVec = part.getScale();

        if (translation != null)
        {
            poseStack.translate(translation.x, translation.y, translation.z);
        }
        if (rotation != null)
        {
            poseStack.mulPose(Axis.XP.rotationDegrees(rotation.x));
            poseStack.mulPose(Axis.YP.rotationDegrees(rotation.y));
            poseStack.mulPose(Axis.ZP.rotationDegrees(rotation.z));
        }
        if (scaleVec != null)
        {
            poseStack.scale(scaleVec.x, scaleVec.y, scaleVec.z);
        }

        textureManager.bindForSetup(part.getDiffuseTexturePath().getResourceLocation());

        AssetLocation inkedLocation = part.getInkedTexturePath();
        if (inkedLocation != null)
        {
            textureManager.bindForSetup(inkedLocation.getResourceLocation());
            RenderSystem.setShaderColor(1, 0, 0, 1);
            int color = Color.asHex(settings.getColor());
            float r = ((color >> 16) & 0xFF) / 255.0F;
            float g = ((color >> 8) & 0xFF) / 255.0F;
            float b = (color & 0xFF) / 255.0F;
            RenderSystem.setShaderColor(r, g, b, 1.0F);
            RenderSystem.setShaderColor(1, 1, 0, 1);
        }

        poseStack.popPose();
    }

    private void applyBindPointTransform(PoseStack poseStack, PlayerData data, BindPoint bindPoint, float scale)
    {
        IModelPart modelPart = bindPoint.getPartSelector().apply(data);

        if (modelPart != null)
        {
            modelPart.applyCharacterTransform(poseStack, scale);
        }
    }
}
