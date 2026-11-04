package goblinbob.mobends.compat;

import com.mojang.blaze3d.vertex.PoseStack;
import goblinbob.mobends.core.client.MoBendsRenderContext;
import goblinbob.mobends.standard.mutators.BipedMutator;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;

public class ChromewareLimbLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>>
{
    private final RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> original;
    private final boolean split;

    public ChromewareLimbLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderer,
                               RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> original)
    {
        super(renderer);
        this.original = original;
        this.split = ChromewareCompat.splitsLimbs(original);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, AbstractClientPlayer entity,
                       float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks,
                       float netHeadYaw, float headPitch)
    {
        final BipedMutator<?, ?, ?> mutator = MoBendsRenderContext.getCurrentBipedMutator();
        final HumanoidModel<?> model = MoBendsRenderContext.getCurrentVanillaModel();

        if (mutator == null || model == null || !mutator.shouldRenderCustom()
                || entity != MoBendsRenderContext.getCurrentEntity())
        {
            original.render(poseStack, bufferSource, packedLight, entity, limbSwing, limbSwingAmount, partialTicks,
                    ageInTicks, netHeadYaw, headPitch);
            return;
        }

        ChromewareCompat.renderBent(mutator, model, poseStack, bufferSource, split,
                source -> original.render(poseStack, source, packedLight, entity, limbSwing, limbSwingAmount,
                        partialTicks, ageInTicks, netHeadYaw, headPitch));
    }
}
