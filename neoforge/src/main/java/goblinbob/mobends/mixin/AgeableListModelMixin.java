package goblinbob.mobends.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import goblinbob.mobends.neoforge.mixin.MixinBridge;
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
                                         int packedLight, int packedOverlay, int color,
                                         CallbackInfo ci) {
        if ((Object) this instanceof HumanoidModel) {
            if (MixinBridge.shouldRenderBipedCustom()) {
                MixinBridge.renderBipedMutated(poseStack, vertexConsumer, packedLight, packedOverlay, color);
                ci.cancel();
            }
        }
        else if ((Object) this instanceof WolfModel) {
            if (MixinBridge.shouldRenderWolfCustom()) {
                MixinBridge.renderWolfMutated(poseStack, vertexConsumer, packedLight, packedOverlay, color);
                ci.cancel();
            }
        }
    }
}
