package goblinbob.mobends.standard.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.PlayerItemInHandLayer;
import net.minecraft.world.entity.player.Player;

public class LayerCustomPlayerHeldItem<E extends Player, M extends EntityModel<E> & ArmedModel & HeadedModel>
        extends PlayerItemInHandLayer<E, M>
{
    private final LayerCustomHeldItem<E, M> delegate;

    public LayerCustomPlayerHeldItem(LivingEntityRenderer<E, M> renderer, LayerCustomHeldItem<E, M> delegate)
    {
        super(renderer, Minecraft.getInstance().getEntityRenderDispatcher().getItemInHandRenderer());
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
}
