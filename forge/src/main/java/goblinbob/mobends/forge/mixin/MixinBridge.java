package goblinbob.mobends.forge.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import goblinbob.mobends.core.client.MoBendsRenderContext;
import goblinbob.mobends.core.data.EntityDatabase;
import goblinbob.mobends.standard.data.BipedEntityData;
import goblinbob.mobends.standard.mutators.BipedMutator;
import goblinbob.mobends.standard.mutators.SpiderMutator;
import goblinbob.mobends.standard.mutators.SquidMutator;
import goblinbob.mobends.standard.mutators.WolfMutator;
import net.minecraft.world.entity.LivingEntity;

public final class MixinBridge {

    private MixinBridge() {}

    public static boolean shouldRenderBipedCustom() {
        if (!MoBendsRenderContext.isInMainModelRender()) {
            return false;
        }
        BipedMutator<?, ?, ?> mutator = MoBendsRenderContext.getCurrentBipedMutator();
        return mutator != null && mutator.shouldRenderCustom();
    }

    public static void setBabyHeadScale(float scale) {
        BipedMutator<?, ?, ?> mutator = MoBendsRenderContext.getCurrentBipedMutator();
        if (mutator != null) {
            mutator.setBabyHeadScale(scale);
        }
    }

    public static void renderBipedMutated(PoseStack poseStack, VertexConsumer vertexConsumer,
                                          int packedLight, int packedOverlay,
                                          float red, float green, float blue, float alpha) {
        BipedMutator<?, ?, ?> mutator = MoBendsRenderContext.getCurrentBipedMutator();
        if (mutator != null) {
            int color = ((int)(alpha * 255.0F) << 24) |
                        ((int)(red * 255.0F) << 16) |
                        ((int)(green * 255.0F) << 8) |
                        (int)(blue * 255.0F);
            mutator.renderMutated(poseStack, vertexConsumer, packedLight, packedOverlay, color);
            net.minecraft.client.model.HumanoidModel<?> vanillaModel = MoBendsRenderContext.getCurrentVanillaModel();
            if (vanillaModel != null) {
                mutator.syncPosesToVanillaModel(vanillaModel);
            }
            MoBendsRenderContext.endMainModelRender();
        }
    }

    public static boolean mayRenderBipedOverlay() {
        if (MoBendsRenderContext.isInMainModelRender()) {
            return false;
        }
        BipedMutator<?, ?, ?> mutator = MoBendsRenderContext.getCurrentBipedMutator();
        return mutator != null && mutator.shouldRenderCustom();
    }

    public static boolean shouldRenderBipedOverlay(Object model, Object renderedParts) {
        if (MoBendsRenderContext.isInMainModelRender()) {
            return false;
        }
        BipedMutator<?, ?, ?> mutator = MoBendsRenderContext.getCurrentBipedMutator();
        return mutator != null && mutator.shouldRenderCustom() && mutator.isOverlayModel(model, renderedParts);
    }

    public static void renderBipedOverlay(Object model, Object renderedParts, PoseStack poseStack, VertexConsumer vertexConsumer,
                                          int packedLight, int packedOverlay,
                                          float red, float green, float blue, float alpha) {
        BipedMutator<?, ?, ?> mutator = MoBendsRenderContext.getCurrentBipedMutator();
        if (mutator != null && model instanceof net.minecraft.client.model.HumanoidModel<?> humanoidModel) {
            int color = ((int)(alpha * 255.0F) << 24) |
                        ((int)(red * 255.0F) << 16) |
                        ((int)(green * 255.0F) << 8) |
                        (int)(blue * 255.0F);
            mutator.renderOverlayModel(humanoidModel, renderedParts, poseStack, vertexConsumer, packedLight, packedOverlay, color);
        }
    }

    public static boolean shouldRenderSpiderCustom() {
        SpiderMutator mutator = MoBendsRenderContext.getCurrentSpiderMutator();
        return mutator != null && mutator.shouldRenderCustom();
    }

    public static void renderSpiderMutated(PoseStack poseStack, VertexConsumer vertexConsumer,
                                           int packedLight, int packedOverlay,
                                           float red, float green, float blue, float alpha) {
        SpiderMutator mutator = MoBendsRenderContext.getCurrentSpiderMutator();
        if (mutator != null) {
            int color = ((int)(alpha * 255.0F) << 24) |
                        ((int)(red * 255.0F) << 16) |
                        ((int)(green * 255.0F) << 8) |
                        (int)(blue * 255.0F);
            mutator.renderMutated(poseStack, vertexConsumer, packedLight, packedOverlay, color);
            MoBendsRenderContext.endMainModelRender();
        }
    }

    public static boolean shouldRenderSquidCustom() {
        SquidMutator mutator = MoBendsRenderContext.getCurrentSquidMutator();
        return mutator != null && mutator.shouldRenderCustom();
    }

    public static void renderSquidMutated(PoseStack poseStack, VertexConsumer vertexConsumer,
                                          int packedLight, int packedOverlay,
                                          float red, float green, float blue, float alpha) {
        SquidMutator mutator = MoBendsRenderContext.getCurrentSquidMutator();
        if (mutator != null) {
            int color = ((int)(alpha * 255.0F) << 24) |
                        ((int)(red * 255.0F) << 16) |
                        ((int)(green * 255.0F) << 8) |
                        (int)(blue * 255.0F);
            mutator.renderMutated(poseStack, vertexConsumer, packedLight, packedOverlay, color);
            MoBendsRenderContext.endMainModelRender();
        }
    }

    public static void setWolfBabyHeadScale(float scale) {
        WolfMutator mutator = MoBendsRenderContext.getCurrentWolfMutator();
        if (mutator != null) {
            mutator.setBabyHeadScale(scale);
        }
    }

    public static boolean shouldRenderWolfCustom() {
        WolfMutator mutator = MoBendsRenderContext.getCurrentWolfMutator();
        return mutator != null && mutator.shouldRenderCustom();
    }

    public static void renderWolfMutated(PoseStack poseStack, VertexConsumer vertexConsumer,
                                          int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        WolfMutator mutator = MoBendsRenderContext.getCurrentWolfMutator();
        if (mutator != null) {
            int color = ((int)(alpha * 255.0F) << 24) |
                        ((int)(red * 255.0F) << 16) |
                        ((int)(green * 255.0F) << 8) |
                        (int)(blue * 255.0F);
            mutator.renderMutated(poseStack, vertexConsumer, packedLight, packedOverlay, color);
            MoBendsRenderContext.endMainModelRender();
        }
    }
}
