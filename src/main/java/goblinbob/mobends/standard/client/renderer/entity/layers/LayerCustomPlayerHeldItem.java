package goblinbob.mobends.standard.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import goblinbob.mobends.standard.client.renderer.entity.WeaponTrailCapture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.PlayerItemInHandLayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class LayerCustomPlayerHeldItem<E extends Player, M extends EntityModel<E> & ArmedModel & HeadedModel>
        extends PlayerItemInHandLayer<E, M>
{
    private final LayerCustomHeldItem<E, M> delegate;

    public LayerCustomPlayerHeldItem(LivingEntityRenderer<E, M> renderer, LayerCustomHeldItem<E, M> delegate)
    {
        super(renderer, new CapturingItemInHandRenderer());
        this.delegate = delegate;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
                       E entity, float limbSwing, float limbSwingAmount,
                       float partialTicks, float ageInTicks, float netHeadYaw, float headPitch)
    {
        if (goblinbob.mobends.compat.BetterCombatCompat.shouldYieldModel(entity))
        {
            super.render(poseStack, bufferSource, packedLight, entity, limbSwing, limbSwingAmount,
                    partialTicks, ageInTicks, netHeadYaw, headPitch);
            return;
        }

        this.delegate.render(poseStack, bufferSource, packedLight, entity, limbSwing, limbSwingAmount,
                partialTicks, ageInTicks, netHeadYaw, headPitch);
    }

    private static final class CapturingItemInHandRenderer extends ItemInHandRenderer
    {
        CapturingItemInHandRenderer()
        {
            super(Minecraft.getInstance(), Minecraft.getInstance().getEntityRenderDispatcher(),
                    Minecraft.getInstance().getItemRenderer());
        }

        @Override
        public void renderItem(LivingEntity entity, ItemStack itemStack, ItemDisplayContext displayContext,
                               boolean leftHand, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight)
        {
            super.renderItem(entity, itemStack, displayContext, leftHand, poseStack, bufferSource, packedLight);

            WeaponTrailCapture.captureVanillaHand(entity, itemStack, displayContext,
                    leftHand ? HumanoidArm.LEFT : HumanoidArm.RIGHT, poseStack);
        }
    }
}
