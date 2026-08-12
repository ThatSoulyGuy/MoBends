package goblinbob.mobends.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import goblinbob.mobends.core.client.MoBendsRenderContext;
import goblinbob.mobends.standard.mutators.BipedMutator;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderLayer.class)
public abstract class RenderLayerMixin {

    @Inject(method = "coloredCutoutModelCopyLayerRender", at = @At("HEAD"), cancellable = true)
    private static <T extends LivingEntity> void mobends$redirectOverlayToBendsParts(
            EntityModel<T> parentModel, EntityModel<T> copyModel, ResourceLocation textureLocation,
            PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
            T entity, float limbSwing, float limbSwingAmount, float ageInTicks,
            float netHeadYaw, float headPitch, float partialTick, int color,
            CallbackInfo ci) {
        if (entity.isInvisible()) {
            return;
        }
        BipedMutator<?, ?, ?> mutator = MoBendsRenderContext.getCurrentBipedMutator();
        if (mutator == null || !mutator.shouldRenderCustom()
                || !(copyModel instanceof HumanoidModel<?>)
                || !mutator.hasOuterParts()) {
            return;
        }
        int packedOverlay = LivingEntityRenderer.getOverlayCoords(entity, 0.0F);
        VertexConsumer vc = bufferSource.getBuffer(RenderType.entityCutoutNoCull(textureLocation));
        mutator.renderOuter(poseStack, vc, packedLight, packedOverlay, color);
        ci.cancel();
    }
}
