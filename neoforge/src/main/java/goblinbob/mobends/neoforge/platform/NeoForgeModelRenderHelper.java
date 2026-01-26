package goblinbob.mobends.neoforge.platform;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import goblinbob.mobends.api.rendering.IModelRenderHelper;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;

/**
 * NeoForge 1.21.1 implementation of IModelRenderHelper.
 *
 * MC 1.21.1:
 * - renderToBuffer(PoseStack, VertexConsumer, int light, int overlay, int color)
 * - ItemRenderer.getArmorFoilBuffer(MultiBufferSource, RenderType, boolean hasFoil)
 */
public class NeoForgeModelRenderHelper implements IModelRenderHelper
{
    @Override
    public void renderModelToBuffer(Object model, Object poseStack, Object vertexConsumer, int packedLight, int packedOverlay, int color)
    {
        Model m = (Model) model;
        PoseStack ps = (PoseStack) poseStack;
        VertexConsumer vc = (VertexConsumer) vertexConsumer;
        m.renderToBuffer(ps, vc, packedLight, packedOverlay, color);
    }

    @Override
    public Object getArmorFoilBuffer(Object bufferSource, Object renderType, boolean hasFoil)
    {
        MultiBufferSource source = (MultiBufferSource) bufferSource;
        RenderType type = (RenderType) renderType;
        return ItemRenderer.getArmorFoilBuffer(source, type, hasFoil);
    }
}
