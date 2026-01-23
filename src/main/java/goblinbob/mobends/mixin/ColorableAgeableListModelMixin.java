package goblinbob.mobends.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import goblinbob.mobends.core.client.MoBendsRenderContext;
import goblinbob.mobends.standard.mutators.WolfMutator;
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
 * ColorableAgeableListModel.renderToBuffer overrides AgeableListModel.renderToBuffer to apply
 * the color multipliers, so we intercept here for WolfModel.
 */
@Mixin(ColorableAgeableListModel.class)
public abstract class ColorableAgeableListModelMixin<T extends Entity> {

    @Inject(method = "renderToBuffer", at = @At("HEAD"), cancellable = true)
    private void mobends$interceptRender(PoseStack poseStack, VertexConsumer vertexConsumer,
                                         int packedLight, int packedOverlay,
                                         float red, float green, float blue, float alpha,
                                         CallbackInfo ci) {
        // Only intercept during main model render phase
        if (!MoBendsRenderContext.isInMainModelRender()) {
            return;
        }

        // Check if this is a WolfModel
        if ((Object) this instanceof WolfModel) {
            WolfMutator mutator = MoBendsRenderContext.getCurrentWolfMutator();

            if (mutator != null && mutator.shouldRenderCustom()) {
                mutator.renderMutated(poseStack, vertexConsumer, packedLight, packedOverlay,
                                     red, green, blue, alpha);
                // End main model render phase so layers render normally
                MoBendsRenderContext.endMainModelRender();
                ci.cancel();
            }
        }
    }
}
