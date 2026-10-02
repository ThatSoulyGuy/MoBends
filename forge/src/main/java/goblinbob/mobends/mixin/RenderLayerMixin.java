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
            float netHeadYaw, float headPitch, float partialTick,
            float red, float green, float blue,
            CallbackInfo ci) {
        if (entity.isInvisible()) {
            return;
        }
        BipedMutator<?, ?, ?> mutator = MoBendsRenderContext.getCurrentBipedMutator();
        if (mutator == null || !mutator.shouldRenderCustom()
                || (!(copyModel instanceof HumanoidModel<?>) && mutator.humanoidViewOf(copyModel) == null)
                || !mutator.hasOuterParts()) {
            return;
        }
        int color = ((int) (255.0F) << 24)
                | ((int) (red * 255.0F) << 16)
                | ((int) (green * 255.0F) << 8)
                | (int) (blue * 255.0F);
        int packedOverlay = LivingEntityRenderer.getOverlayCoords(entity, 0.0F);
        VertexConsumer vc = bufferSource.getBuffer(RenderType.entityCutoutNoCull(textureLocation));
        mutator.renderOuter(poseStack, vc, packedLight, packedOverlay, color);
        ci.cancel();
    }

    @org.spongepowered.asm.mixin.Unique
    private static BipedMutator<?, ?, ?> mobends$outerMutator;

    @org.spongepowered.asm.mixin.Unique
    private static ResourceLocation mobends$outerTexture;

    @Inject(method = "renderColoredCutoutModel", at = @At("HEAD"))
    private static <T extends LivingEntity> void mobends$prepareColoredModel(
            EntityModel<T> model, ResourceLocation textureLocation,
            PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
            T entity, float red, float green, float blue,
            CallbackInfo ci) {
        mobends$outerMutator = mobends$outerMutatorFor(model, entity);
        mobends$outerTexture = textureLocation;
    }

    @org.spongepowered.asm.mixin.injection.Redirect(method = "renderColoredCutoutModel", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/model/EntityModel;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;IIFFFF)V"))
    private static void mobends$renderColoredModel(EntityModel<?> model, PoseStack poseStack,
                                                   VertexConsumer vertexConsumer, int packedLight, int packedOverlay,
                                                   float red, float green, float blue, float alpha) {
        final BipedMutator<?, ?, ?> mutator = mobends$outerMutator;
        mobends$outerMutator = null;
        if (mutator == null) {
            model.renderToBuffer(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
            return;
        }
        int color = 0xFF000000
                | ((int) (red * 255.0F) << 16)
                | ((int) (green * 255.0F) << 8)
                | (int) (blue * 255.0F);
        mobends$renderOuter(mutator, poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }

    @org.spongepowered.asm.mixin.Unique
    private static <T extends LivingEntity> BipedMutator<?, ?, ?> mobends$outerMutatorFor(EntityModel<T> model, T entity) {
        if (entity.isInvisible()) {
            return null;
        }
        BipedMutator<?, ?, ?> mutator = MoBendsRenderContext.getCurrentBipedMutator();
        if (mutator == null || !mutator.shouldRenderCustom() || mutator.shouldModelBeSkipped(model)
                || !mutator.hasOuterParts()) {
            return null;
        }
        return mutator;
    }

    @org.spongepowered.asm.mixin.Unique
    private static void mobends$renderOuter(BipedMutator<?, ?, ?> mutator, PoseStack poseStack,
                                            VertexConsumer vertexConsumer, int packedLight, int packedOverlay,
                                            int color) {
        goblinbob.mobends.standard.client.VillagerOverlayContext.set(mobends$outerTexture);
        try {
            mutator.renderOuter(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        } finally {
            goblinbob.mobends.standard.client.VillagerOverlayContext.clear();
        }
    }

}
