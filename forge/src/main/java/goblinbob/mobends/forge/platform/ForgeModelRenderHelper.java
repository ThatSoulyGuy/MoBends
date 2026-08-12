package goblinbob.mobends.forge.platform;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import goblinbob.mobends.api.rendering.IModelRenderHelper;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;

public class ForgeModelRenderHelper implements IModelRenderHelper
{
    @Override
    public void renderModelToBuffer(Object model, Object poseStack, Object vertexConsumer, int packedLight, int packedOverlay, int color)
    {
        Model m = (Model) model;
        PoseStack ps = (PoseStack) poseStack;
        VertexConsumer vc = (VertexConsumer) vertexConsumer;

        float alpha = ((color >> 24) & 0xFF) / 255.0f;
        float red = ((color >> 16) & 0xFF) / 255.0f;
        float green = ((color >> 8) & 0xFF) / 255.0f;
        float blue = (color & 0xFF) / 255.0f;

        m.renderToBuffer(ps, vc, packedLight, packedOverlay, red, green, blue, alpha);
    }

    @Override
    public Object getArmorFoilBuffer(Object bufferSource, Object renderType, boolean hasFoil)
    {
        MultiBufferSource source = (MultiBufferSource) bufferSource;
        RenderType type = (RenderType) renderType;
        return ItemRenderer.getArmorFoilBuffer(source, type, true, hasFoil);
    }
}
