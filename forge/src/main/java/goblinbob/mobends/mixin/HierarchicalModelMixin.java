package goblinbob.mobends.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import goblinbob.mobends.core.client.MixinBridge;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.IllagerModel;
import net.minecraft.client.model.SpiderModel;
import net.minecraft.client.model.SquidModel;
import net.minecraft.client.model.VillagerModel;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HierarchicalModel.class)
public abstract class HierarchicalModelMixin<E extends Entity> {

    @Inject(method = "renderToBuffer", at = @At("HEAD"), cancellable = true)
    private void mobends$interceptRender(PoseStack poseStack, VertexConsumer vertexConsumer,
                                         int packedLight, int packedOverlay,
                                         float red, float green, float blue, float alpha,
                                         CallbackInfo ci) {
        int color = FastColor.ARGB32.color((int)(alpha * 255.0F), (int)(red * 255.0F), (int)(green * 255.0F), (int)(blue * 255.0F));
        Object model = this;

        goblinbob.mobends.compat.SpiderOverhaulCompat.poseModel(model);

        if (model instanceof IllagerModel<?> || model instanceof VillagerModel<?>) {
            if (MixinBridge.shouldRenderBipedCustom()) {
                MixinBridge.renderBipedMutated(poseStack, vertexConsumer, packedLight, packedOverlay, color);
                ci.cancel();
            }
            else if (MixinBridge.shouldMirrorBipedRender(model)) {
                MixinBridge.renderBipedMirror(poseStack, vertexConsumer, packedLight, packedOverlay, color);
                ci.cancel();
            }
            return;
        }

        if (model instanceof SpiderModel) {
            if (MixinBridge.shouldRenderSpiderCustom()) {
                MixinBridge.renderSpiderMutated(poseStack, vertexConsumer, packedLight, packedOverlay, color);
                ci.cancel();
            }
            return;
        }

        if (model instanceof SquidModel) {
            if (MixinBridge.shouldRenderSquidCustom()) {
                MixinBridge.renderSquidMutated(poseStack, vertexConsumer, packedLight, packedOverlay, color);
                ci.cancel();
            }
        }
    }

}
