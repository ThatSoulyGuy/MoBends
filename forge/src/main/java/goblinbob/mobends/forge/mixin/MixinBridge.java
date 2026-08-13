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
        }
    }

    public static boolean hasAnimationData(LivingEntity entity) {
        Object data = EntityDatabase.instance.get(entity);
        return data instanceof BipedEntityData<?>;
    }

    public static BipedEntityData<?> getEntityData(LivingEntity entity) {
        Object data = EntityDatabase.instance.get(entity);
        if (data instanceof BipedEntityData<?>) {
            return (BipedEntityData<?>) data;
        }
        return null;
    }
}
