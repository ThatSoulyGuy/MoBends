package goblinbob.mobends.mixin.skinweathering;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import goblinbob.mobends.core.client.MixinBridge;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "com.example.weathering.client.WeatheringFeatureRenderer", remap = false)
public class WeatheringFeatureRendererMixin
{
    @Inject(
            method = "renderModelWithVariance(Lnet/minecraft/client/model/HumanoidModel;Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;IFFFFI)V",
            at = @At("HEAD"),
            cancellable = true,
            require = 0)
    private void mobends$renderOnBentModel(HumanoidModel<?> model, PoseStack poseStack, VertexConsumer buffer,
                                           int light, float r, float g, float b, float alpha, int seed,
                                           CallbackInfo ci)
    {
        if (!MixinBridge.shouldRenderBipedOverlay(model, null))
        {
            return;
        }

        final int color = (int) (alpha * 255.0F) << 24
                | (int) (Mth.clamp(r, 0.0F, 1.0F) * 255.0F) << 16
                | (int) (Mth.clamp(g, 0.0F, 1.0F) * 255.0F) << 8
                | (int) (Mth.clamp(b, 0.0F, 1.0F) * 255.0F);

        model.renderToBuffer(poseStack, buffer, light, OverlayTexture.NO_OVERLAY, color);
        ci.cancel();
    }
}
