package goblinbob.mobends.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import goblinbob.mobends.forge.mixin.MixinBridge;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.AgeableListModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.WolfModel;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AgeableListModel.class)
public abstract class AgeableListModelMixin<T extends LivingEntity> {

    @Inject(method = "renderToBuffer", at = @At("HEAD"), cancellable = true)
    private void mobends$interceptRender(PoseStack poseStack, VertexConsumer vertexConsumer,
                                         int packedLight, int packedOverlay,
                                         float red, float green, float blue, float alpha,
                                         CallbackInfo ci) {
        if ((Object) this instanceof HumanoidModel) {
            if (MixinBridge.shouldRenderBipedCustom()) {
                MixinBridge.renderBipedMutated(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
                ci.cancel();
            }
        }
        else if ((Object) this instanceof WolfModel) {
            if (MixinBridge.shouldRenderWolfCustom()) {
                MixinBridge.renderWolfMutated(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
                ci.cancel();
            }
        }
    }
}
