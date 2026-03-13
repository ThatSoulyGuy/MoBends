package goblinbob.mobends.neoforge.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import goblinbob.mobends.core.client.MoBendsRenderContext;
import goblinbob.mobends.core.data.EntityDatabase;
import goblinbob.mobends.standard.data.BipedEntityData;
import goblinbob.mobends.standard.mutators.BipedMutator;
import goblinbob.mobends.standard.mutators.SpiderMutator;
import net.minecraft.world.entity.LivingEntity;

/**
 * Bridge class for mixins to access common-mc classes.
 * This allows mixins in the neoforge module to interact with the shared code
 * without causing module conflicts during dev runs.
 */
public final class MixinBridge {

    private MixinBridge() {}

    /**
     * Check if a biped mutator is active and should render custom.
     * CRITICAL: Only returns true if we're in the main entity model render phase.
     * During layer rendering (armor, elytra, etc.), returns false so vanilla renders normally.
     * @return true if custom rendering should be used
     */
    public static boolean shouldRenderBipedCustom() {
        // Only intercept if we're in the main model render phase
        if (!MoBendsRenderContext.isInMainModelRender()) {
            return false;
        }
        BipedMutator<?, ?, ?> mutator = MoBendsRenderContext.getCurrentBipedMutator();
        return mutator != null && mutator.shouldRenderCustom();
    }

    /**
     * Render the biped using the current mutator.
     * Ends the main model render phase so layers (armor, elytra) render normally.
     */
    public static void renderBipedMutated(PoseStack poseStack, VertexConsumer vertexConsumer,
                                          int packedLight, int packedOverlay, int color) {
        BipedMutator<?, ?, ?> mutator = MoBendsRenderContext.getCurrentBipedMutator();
        if (mutator != null) {
            mutator.renderMutated(poseStack, vertexConsumer, packedLight, packedOverlay, color);
            // Re-sync poses to vanilla model AFTER custom render (setupAnim overwrote the Pre-event sync).
            // This ensures overlay layers (Drowned outer layer, etc.) get animated poses when they copy
            // from the parent model.
            net.minecraft.client.model.HumanoidModel<?> vanillaModel = MoBendsRenderContext.getCurrentVanillaModel();
            if (vanillaModel != null) {
                mutator.syncPosesToVanillaModel(vanillaModel);
            }
            // End main model render phase so layers (armor, elytra) render normally
            MoBendsRenderContext.endMainModelRender();
        }
    }

    /**
     * Check if a spider mutator is active and should render custom.
     * CRITICAL: Only returns true if we're in the main entity model render phase.
     * @return true if custom rendering should be used
     */
    public static boolean shouldRenderSpiderCustom() {
        // Only intercept if we're in the main model render phase
        if (!MoBendsRenderContext.isInMainModelRender()) {
            return false;
        }
        SpiderMutator mutator = MoBendsRenderContext.getCurrentSpiderMutator();
        return mutator != null && mutator.shouldRenderCustom();
    }

    /**
     * Render the spider using the current mutator.
     * Ends the main model render phase so layers render normally.
     */
    public static void renderSpiderMutated(PoseStack poseStack, VertexConsumer vertexConsumer,
                                           int packedLight, int packedOverlay, int color) {
        SpiderMutator mutator = MoBendsRenderContext.getCurrentSpiderMutator();
        if (mutator != null) {
            mutator.renderMutated(poseStack, vertexConsumer, packedLight, packedOverlay, color);
            // End main model render phase so layers render normally
            MoBendsRenderContext.endMainModelRender();
        }
    }

    /**
     * Check if an entity has Mo'Bends animation data.
     * @param entity The entity to check
     * @return true if the entity has biped animation data
     */
    public static boolean hasAnimationData(LivingEntity entity) {
        Object data = EntityDatabase.instance.get(entity);
        return data instanceof BipedEntityData<?>;
    }

    /**
     * Get Mo'Bends entity data for an entity.
     * @param entity The entity
     * @return The biped entity data, or null if not available
     */
    public static BipedEntityData<?> getEntityData(LivingEntity entity) {
        Object data = EntityDatabase.instance.get(entity);
        if (data instanceof BipedEntityData<?>) {
            return (BipedEntityData<?>) data;
        }
        return null;
    }
}
