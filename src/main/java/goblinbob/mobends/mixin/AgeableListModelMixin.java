package goblinbob.mobends.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import goblinbob.mobends.core.client.MoBendsRenderContext;
import goblinbob.mobends.standard.mutators.BipedMutator;
import goblinbob.mobends.standard.mutators.WolfMutator;
import net.minecraft.client.model.AgeableListModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.WolfModel;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to intercept AgeableListModel rendering and redirect to MoBends custom rendering
 * when a mutation is active for HumanoidModel or WolfModel instances.
 */
@Mixin(AgeableListModel.class)
public abstract class AgeableListModelMixin<T extends LivingEntity> {

    @Inject(method = "renderToBuffer", at = @At("HEAD"), cancellable = true)
    private void mobends$interceptRender(PoseStack poseStack, VertexConsumer vertexConsumer,
                                         int packedLight, int packedOverlay,
                                         float red, float green, float blue, float alpha,
                                         CallbackInfo ci) {
        // CRITICAL: Only intercept if we're in the main entity model render phase.
        // During layer rendering (armor, elytra, etc.), we must NOT intercept,
        // otherwise we'd render player geometry instead of armor textures.
        if (!MoBendsRenderContext.isInMainModelRender()) {
            return;
        }

        // Check for HumanoidModel (biped entities)
        if ((Object) this instanceof HumanoidModel) {
            BipedMutator<?, ?, ?> mutator = MoBendsRenderContext.getCurrentBipedMutator();

            if (mutator != null && mutator.shouldRenderCustom()) {
                mutator.renderMutated(poseStack, vertexConsumer, packedLight, packedOverlay,
                                     red, green, blue, alpha);
                // End main model render phase so layers (armor, elytra) render normally
                MoBendsRenderContext.endMainModelRender();
                ci.cancel();
                return;
            }
        }

        // Check for WolfModel
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
