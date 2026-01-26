package goblinbob.mobends.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import goblinbob.mobends.forge.mixin.MixinBridge;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.ColorableAgeableListModel;
import net.minecraft.client.model.WolfModel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to intercept ColorableAgeableListModel rendering and redirect to MoBends custom rendering
 * when a wolf mutation is active.
 *
 * MC 1.20.1: renderToBuffer uses float RGBA instead of packed int color
 */
@Mixin(ColorableAgeableListModel.class)
public abstract class ColorableAgeableListModelMixin<T extends Entity> {

    @Inject(method = "renderToBuffer", at = @At("HEAD"), cancellable = true)
    private void mobends$interceptRender(PoseStack poseStack, VertexConsumer vertexConsumer,
                                         int packedLight, int packedOverlay,
                                         float red, float green, float blue, float alpha,
                                         CallbackInfo ci) {
        // Check if this is a WolfModel
        if ((Object) this instanceof WolfModel) {
            if (MixinBridge.shouldRenderWolfCustom()) {
                MixinBridge.renderWolfMutated(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
                ci.cancel();
            }
        }
    }
}
