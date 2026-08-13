package goblinbob.mobends.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import goblinbob.mobends.forge.mixin.MixinBridge;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.SpiderModel;
import net.minecraft.client.model.SquidModel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HierarchicalModel.class)
public abstract class SpiderModelMixin<T extends Entity> {

    @Inject(method = "renderToBuffer", at = @At("HEAD"), cancellable = true)
    private void mobends$interceptRender(PoseStack poseStack, VertexConsumer vertexConsumer,
                                         int packedLight, int packedOverlay,
                                         float red, float green, float blue, float alpha,
                                         CallbackInfo ci) {
        if ((Object) this instanceof SpiderModel) {
            if (MixinBridge.shouldRenderSpiderCustom()) {
                MixinBridge.renderSpiderMutated(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
                ci.cancel();
            }
            return;
        }

        if ((Object) this instanceof SquidModel) {
            if (MixinBridge.shouldRenderSquidCustom()) {
                MixinBridge.renderSquidMutated(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
                ci.cancel();
            }
        }
    }
}
