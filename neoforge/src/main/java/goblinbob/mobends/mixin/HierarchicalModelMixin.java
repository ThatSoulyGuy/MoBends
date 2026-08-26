package goblinbob.mobends.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import goblinbob.mobends.neoforge.mixin.MixinBridge;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.IllagerModel;
import net.minecraft.client.model.SpiderModel;
import net.minecraft.client.model.SquidModel;
import net.minecraft.client.model.VillagerModel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Draws the bent model in place of vanilla, for every entity whose model extends
 * {@link HierarchicalModel}.
 *
 * <p>This used to be two mixins — one for illagers, one for spiders and squids — both targeting
 * this class, both injecting at the HEAD of {@code renderToBuffer}, and both naming their handler
 * {@code mobends$interceptRender}. Their instanceof guards were disjoint so only one could ever
 * act, but the apply order between them was undefined and nothing enforced that disjointness.
 * One mixin, one branch chain, no ambiguity.
 */
@Mixin(HierarchicalModel.class)
public abstract class HierarchicalModelMixin<E extends Entity> {

    @Inject(method = "renderToBuffer", at = @At("HEAD"), cancellable = true)
    private void mobends$interceptRender(PoseStack poseStack, VertexConsumer vertexConsumer,
                                         int packedLight, int packedOverlay, int color,
                                         CallbackInfo ci) {
        Object model = this;

        if (model instanceof IllagerModel<?> || model instanceof VillagerModel<?>) {
            if (MixinBridge.shouldRenderBipedCustom()) {
                MixinBridge.renderBipedMutated(poseStack, vertexConsumer, packedLight, packedOverlay, color);
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
