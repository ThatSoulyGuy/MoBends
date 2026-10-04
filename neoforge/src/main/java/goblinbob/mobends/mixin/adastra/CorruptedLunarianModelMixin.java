package goblinbob.mobends.mixin.adastra;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import goblinbob.mobends.core.client.MixinBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "earth.terrarium.adastra.client.models.entities.mobs.CorruptedLunarianModel", remap = false)
public class CorruptedLunarianModelMixin
{
    @Inject(method = "renderToBuffer", at = @At("HEAD"), cancellable = true, require = 0)
    private void mobends$renderMutated(PoseStack poseStack, VertexConsumer vertexConsumer,
                                       int packedLight, int packedOverlay, int color,
                                       CallbackInfo ci)
    {
        if (MixinBridge.shouldRenderBipedCustom())
        {
            MixinBridge.renderBipedMutated(poseStack, vertexConsumer, packedLight, packedOverlay, color);
            ci.cancel();
        }
        else if (MixinBridge.shouldMirrorBipedRender(this))
        {
            MixinBridge.renderBipedMirror(poseStack, vertexConsumer, packedLight, packedOverlay, color);
            ci.cancel();
        }
    }
}
