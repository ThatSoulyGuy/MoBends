package goblinbob.mobends.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import goblinbob.mobends.core.client.MoBendsRenderContext;
import goblinbob.mobends.standard.mutators.SpiderMutator;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.SpiderModel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to intercept HierarchicalModel rendering and redirect to MoBends custom rendering
 * when a spider mutation is active. We target HierarchicalModel because that's where
 * renderToBuffer is defined for SpiderModel.
 */
@Mixin(HierarchicalModel.class)
public abstract class SpiderModelMixin<T extends Entity> {

    @Inject(method = "renderToBuffer", at = @At("HEAD"), cancellable = true)
    private void mobends$interceptRender(PoseStack poseStack, VertexConsumer vertexConsumer,
                                         int packedLight, int packedOverlay, int color,
                                         CallbackInfo ci) {
        // Only intercept if this is a SpiderModel and we have an active spider mutator
        if (!((Object) this instanceof SpiderModel)) {
            return;
        }

        SpiderMutator mutator = MoBendsRenderContext.getCurrentSpiderMutator();

        if (mutator != null && mutator.shouldRenderCustom()) {
            mutator.renderMutated(poseStack, vertexConsumer, packedLight, packedOverlay, color);
            ci.cancel();
        }
    }
}
