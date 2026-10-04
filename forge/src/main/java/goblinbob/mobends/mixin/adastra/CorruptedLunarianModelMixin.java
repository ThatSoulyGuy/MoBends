package goblinbob.mobends.mixin.adastra;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import goblinbob.mobends.core.client.MixinBridge;
import net.minecraft.util.FastColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "earth.terrarium.adastra.client.models.entities.mobs.CorruptedLunarianModel", remap = false)
public class CorruptedLunarianModelMixin
{
    @Inject(
            method = "m_7695_(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;IIFFFF)V",
            at = @At("HEAD"),
            cancellable = true,
            require = 0)
    private void mobends$renderMutated(PoseStack poseStack, VertexConsumer vertexConsumer,
                                       int packedLight, int packedOverlay,
                                       float red, float green, float blue, float alpha,
                                       CallbackInfo ci)
    {
        final int color = FastColor.ARGB32.color((int) (alpha * 255.0F), (int) (red * 255.0F),
                (int) (green * 255.0F), (int) (blue * 255.0F));

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
