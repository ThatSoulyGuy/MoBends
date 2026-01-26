package goblinbob.mobends.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import goblinbob.mobends.neoforge.mixin.MixinBridge;
import net.minecraft.client.model.AgeableListModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.LivingEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to intercept AgeableListModel rendering and redirect to MoBends custom rendering
 * when a mutation is active for HumanoidModel instances.
 */
@Mixin(AgeableListModel.class)
public abstract class AgeableListModelMixin<T extends LivingEntity> {

    @Unique
    private static final Logger mobends$LOGGER = LoggerFactory.getLogger("MoBends-Mixin");
    @Unique
    private static int mobends$debugCounter = 0;

    @Inject(method = "renderToBuffer", at = @At("HEAD"), cancellable = true)
    private void mobends$interceptRender(PoseStack poseStack, VertexConsumer vertexConsumer,
                                         int packedLight, int packedOverlay, int color,
                                         CallbackInfo ci) {
        // Check for HumanoidModel (biped entities)
        if ((Object) this instanceof HumanoidModel) {
            boolean shouldRender = MixinBridge.shouldRenderBipedCustom();
            // Debug logging (every 100 calls to avoid spam)
            if (++mobends$debugCounter % 100 == 0) {
                mobends$LOGGER.info("[MoBends DEBUG] Mixin interceptRender: isHumanoidModel=true, shouldRenderCustom={}", shouldRender);
            }
            if (shouldRender) {
                MixinBridge.renderBipedMutated(poseStack, vertexConsumer, packedLight, packedOverlay, color);
                ci.cancel();
            }
        }
    }
}
