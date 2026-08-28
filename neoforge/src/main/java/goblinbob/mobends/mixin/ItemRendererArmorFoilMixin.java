package goblinbob.mobends.mixin;

import com.mojang.blaze3d.vertex.VertexConsumer;
import goblinbob.mobends.standard.client.model.armor.ArmorCaptureContext;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererArmorFoilMixin {

    @Inject(method = "getArmorFoilBuffer", at = @At("HEAD"), cancellable = true)
    private static void mobends$redirectArmorFoilToCapture(MultiBufferSource bufferSource, RenderType renderType,
                                                           boolean hasFoil,
                                                           CallbackInfoReturnable<VertexConsumer> cir) {
        VertexConsumer capture = ArmorCaptureContext.active();
        if (capture != null) {
            ArmorCaptureContext.noteRenderType(renderType);
            cir.setReturnValue(capture);
        }
    }

}
