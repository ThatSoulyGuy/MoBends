package goblinbob.mobends.standard.client.model.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.RenderType;

public final class TinkersArmorProxyModel extends Model
{
    public interface Draw
    {
        void draw(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color);
    }

    private Draw target;

    public TinkersArmorProxyModel()
    {
        super(RenderType::entityCutoutNoCull);
    }

    public TinkersArmorProxyModel bind(Draw target)
    {
        this.target = target;
        return this;
    }

    private void dispatch(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color)
    {
        if (target != null)
        {
            target.draw(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        }
    }

    private static int packColor(float red, float green, float blue, float alpha)
    {
        return (component(alpha) << 24) | (component(red) << 16) | (component(green) << 8) | component(blue);
    }

    private static int component(float value)
    {
        return Math.max(0, Math.min(255, (int) (value * 255.0F)));
    }

    //? if >=1.21 {
    /*@Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color)
    {
        dispatch(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
    *///?} else {
    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha)
    {
        dispatch(poseStack, vertexConsumer, packedLight, packedOverlay, packColor(red, green, blue, alpha));
    }
    //?}
}
