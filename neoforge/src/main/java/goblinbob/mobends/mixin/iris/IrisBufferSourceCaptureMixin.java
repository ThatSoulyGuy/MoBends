package goblinbob.mobends.mixin.iris;

import com.mojang.blaze3d.vertex.VertexConsumer;
import goblinbob.mobends.standard.client.model.armor.ArmorCaptureContext;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "net.irisshaders.batchedentityrendering.impl.FullyBufferedMultiBufferSource", remap = false)
public class IrisBufferSourceCaptureMixin
{
    @Inject(method = {"getBuffer", "m_6299_"}, at = @At("HEAD"), cancellable = true, require = 0)
    private void mobends$redirectBufferToCapture(RenderType renderType, CallbackInfoReturnable<VertexConsumer> cir)
    {
        final VertexConsumer capture = ArmorCaptureContext.active();
        if (capture == null)
        {
            return;
        }

        if (ArmorCaptureContext.isEmissiveType(renderType))
        {
            ArmorCaptureContext.recordEmissive(renderType);
            cir.setReturnValue(ArmorCaptureContext.discard());
            return;
        }

        cir.setReturnValue(capture);
    }
}
