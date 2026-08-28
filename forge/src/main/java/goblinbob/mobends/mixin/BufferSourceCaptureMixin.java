package goblinbob.mobends.mixin;

import com.mojang.blaze3d.vertex.VertexConsumer;
import goblinbob.mobends.standard.client.model.armor.ArmorCaptureContext;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiBufferSource.BufferSource.class)
public abstract class BufferSourceCaptureMixin {

    @Inject(method = "getBuffer", at = @At("HEAD"), cancellable = true)
    private void mobends$redirectBufferToCapture(RenderType renderType, CallbackInfoReturnable<VertexConsumer> cir) {
        VertexConsumer capture = ArmorCaptureContext.active();

        if (capture == null) {
            return;
        }

        if (ArmorCaptureContext.isEmissiveType(renderType)) {
            ArmorCaptureContext.recordEmissive(renderType);
            cir.setReturnValue(ArmorCaptureContext.discard());
            return;
        }

        ArmorCaptureContext.noteRenderType(renderType);
        cir.setReturnValue(capture);
    }

}
