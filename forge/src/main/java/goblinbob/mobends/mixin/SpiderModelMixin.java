package goblinbob.mobends.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import goblinbob.mobends.forge.mixin.MixinBridge;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.SpiderModel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to intercept HierarchicalModel rendering and redirect to MoBends custom rendering
 * when a spider mutation is active.
 *
 * MC 1.20.1: renderToBuffer uses float RGBA instead of packed int color
 */
@Mixin(HierarchicalModel.class)
public abstract class SpiderModelMixin<T extends Entity> {

    @Inject(method = "renderToBuffer", at = @At("HEAD"), cancellable = true)
    private void mobends$interceptRender(PoseStack poseStack, VertexConsumer vertexConsumer,
                                         int packedLight, int packedOverlay,
                                         float red, float green, float blue, float alpha,
                                         CallbackInfo ci) {
        // Only intercept if this is a SpiderModel
        if (!((Object) this instanceof SpiderModel)) {
            return;
        }

        if (MixinBridge.shouldRenderSpiderCustom()) {
            MixinBridge.renderSpiderMutated(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
            ci.cancel();
        }
    }
}
