package goblinbob.mobends.mixin.mca;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import goblinbob.mobends.compat.McaCompat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "forge.net.mca.client.model.PlayerEntityExtendedModel", remap = false)
public class PlayerModelMcaRelocatedMixin
{
    @Inject(method = {"renderToBuffer", "m_7695_"}, at = @At("HEAD"), cancellable = true, require = 0)
    private void mobends$interceptRender(PoseStack poseStack, VertexConsumer vertexConsumer,
                                         int packedLight, int packedOverlay,
                                         float red, float green, float blue, float alpha,
                                         CallbackInfo ci)
    {
        int color = ((int) (alpha * 255.0F) << 24)
                | ((int) (red * 255.0F) << 16)
                | ((int) (green * 255.0F) << 8)
                | (int) (blue * 255.0F);

        if (McaCompat.renderMutatedInstead(this, poseStack, vertexConsumer, packedLight, packedOverlay, color))
        {
            ci.cancel();
        }
    }
}
