package goblinbob.mobends.forge.platform;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import goblinbob.mobends.api.rendering.IModelRenderHelper;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;

/**
 * Forge 1.20.1 implementation of IModelRenderHelper.
 *
 * MC 1.20.1:
 * - renderToBuffer(PoseStack, VertexConsumer, int light, int overlay, float r, float g, float b, float a)
 * - ItemRenderer.getArmorFoilBuffer(MultiBufferSource, RenderType, boolean isEntity, boolean hasFoil)
 */
public class ForgeModelRenderHelper implements IModelRenderHelper
{
    @Override
    public void renderModelToBuffer(Object model, Object poseStack, Object vertexConsumer, int packedLight, int packedOverlay, int color)
    {
        Model m = (Model) model;
        PoseStack ps = (PoseStack) poseStack;
        VertexConsumer vc = (VertexConsumer) vertexConsumer;

        // Unpack ARGB color to floats
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
        // In 1.20.1, getArmorFoilBuffer takes 4 params: source, type, isEntity (true for armor), hasFoil
        return ItemRenderer.getArmorFoilBuffer(source, type, true, hasFoil);
    }
}
