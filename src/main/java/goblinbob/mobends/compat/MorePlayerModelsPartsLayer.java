package goblinbob.mobends.compat;

import com.mojang.blaze3d.vertex.PoseStack;
import goblinbob.mobends.core.client.MoBendsRenderContext;
import goblinbob.mobends.standard.mutators.BipedMutator;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;

public class MorePlayerModelsPartsLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>>
{
    private final RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> original;

    public MorePlayerModelsPartsLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderer,
                                      RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> original)
    {
        super(renderer);
        this.original = original;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, AbstractClientPlayer entity,
                       float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks,
                       float netHeadYaw, float headPitch)
    {
        final BipedMutator<?, ?, ?> mutator = MoBendsRenderContext.getCurrentBipedMutator();
        final PlayerModel<AbstractClientPlayer> model = getParentModel();

        if (mutator == null || !mutator.shouldRenderCustom() || entity != MoBendsRenderContext.getCurrentEntity())
        {
            original.render(poseStack, bufferSource, packedLight, entity, limbSwing, limbSwingAmount, partialTicks,
                    ageInTicks, netHeadYaw, headPitch);
            return;
        }

        MorePlayerModelsCompat.beginPartsRender(entity, mutator, model);
        try
        {
            original.render(poseStack, bufferSource, packedLight, entity, limbSwing, limbSwingAmount, partialTicks,
                    ageInTicks, netHeadYaw, headPitch);
        }
        finally
        {
            MorePlayerModelsCompat.endPartsRender(model);
        }
    }
}
