package goblinbob.mobends.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import goblinbob.mobends.core.client.MoBendsRenderContext;
import goblinbob.mobends.standard.mutators.SquidMutator;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.SquidModel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to intercept HierarchicalModel rendering and redirect to MoBends custom rendering
 * when a squid mutation is active. We target HierarchicalModel because that's where
 * renderToBuffer is defined for SquidModel.
 */
@Mixin(HierarchicalModel.class)
public abstract class SquidModelMixin<T extends Entity> {

    @Inject(method = "renderToBuffer", at = @At("HEAD"), cancellable = true)
    private void mobends$interceptSquidRender(PoseStack poseStack, VertexConsumer vertexConsumer,
                                              int packedLight, int packedOverlay,
                                              float red, float green, float blue, float alpha,
                                              CallbackInfo ci) {
        // Only intercept if this is a SquidModel and we have an active squid mutator
        if (!((Object) this instanceof SquidModel)) {
            return;
        }

        SquidMutator mutator = MoBendsRenderContext.getCurrentSquidMutator();

        if (mutator != null && mutator.shouldRenderCustom()) {
            mutator.renderMutated(poseStack, vertexConsumer, packedLight, packedOverlay,
                                 red, green, blue, alpha);
            ci.cancel();
        }
    }
}
