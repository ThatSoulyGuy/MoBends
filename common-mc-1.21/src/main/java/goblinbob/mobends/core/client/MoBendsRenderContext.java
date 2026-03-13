package goblinbob.mobends.core.client;

import goblinbob.mobends.standard.mutators.BipedMutator;
import goblinbob.mobends.standard.mutators.SpiderMutator;
import goblinbob.mobends.standard.mutators.SquidMutator;

/**
 * Thread-local context for passing mutation state during rendering.
 * Used by mixins to determine if custom rendering should be applied.
 */
public class MoBendsRenderContext {

    private static final ThreadLocal<BipedMutator<?, ?, ?>> currentBipedMutator = new ThreadLocal<>();
    private static final ThreadLocal<SpiderMutator> currentSpiderMutator = new ThreadLocal<>();
    private static final ThreadLocal<SquidMutator> currentSquidMutator = new ThreadLocal<>();

    /**
     * Flag indicating we're rendering the main entity model (not layers like armor).
     * When false, mixins should NOT intercept HumanoidModel rendering, as it may be
     * armor, elytra, or other layer models that shouldn't use MoBends geometry.
     */
    private static final ThreadLocal<Boolean> inMainModelRender = ThreadLocal.withInitial(() -> false);

    /**
     * Reference to the current vanilla HumanoidModel, used to re-sync poses
     * after custom rendering so overlay layers (Drowned, etc.) get animated poses.
     */
    private static final ThreadLocal<net.minecraft.client.model.HumanoidModel<?>> currentVanillaModel = new ThreadLocal<>();

    /**
     * Start main model rendering phase.
     * Call this BEFORE the main entity model renders.
     */
    public static void beginMainModelRender() {
        inMainModelRender.set(true);
    }

    /**
     * End main model rendering phase.
     * Call this AFTER the main entity model renders (before layers).
     */
    public static void endMainModelRender() {
        inMainModelRender.set(false);
    }

    /**
     * Check if we're in the main model rendering phase.
     * Used by mixins to distinguish entity model from layer models (armor, etc.)
     */
    public static boolean isInMainModelRender() {
        return inMainModelRender.get();
    }

    /**
     * Store the current vanilla HumanoidModel for post-render pose sync.
     */
    public static void setCurrentVanillaModel(net.minecraft.client.model.HumanoidModel<?> model) {
        currentVanillaModel.set(model);
    }

    /**
     * Get the current vanilla HumanoidModel.
     */
    public static net.minecraft.client.model.HumanoidModel<?> getCurrentVanillaModel() {
        return currentVanillaModel.get();
    }

    /**
     * Sets the current biped mutator for the rendering context.
     * Call this before the vanilla model renders.
     */
    public static void setCurrentBipedMutator(BipedMutator<?, ?, ?> mutator) {
        currentBipedMutator.set(mutator);
    }

    /**
     * Gets the current biped mutator from the rendering context.
     * Used by mixins to check if custom rendering should be applied.
     */
    public static BipedMutator<?, ?, ?> getCurrentBipedMutator() {
        return currentBipedMutator.get();
    }

    /**
     * Sets the current spider mutator for the rendering context.
     */
    public static void setCurrentSpiderMutator(SpiderMutator mutator) {
        currentSpiderMutator.set(mutator);
    }

    /**
     * Gets the current spider mutator from the rendering context.
     */
    public static SpiderMutator getCurrentSpiderMutator() {
        return currentSpiderMutator.get();
    }

    /**
     * Sets the current squid mutator for the rendering context.
     */
    public static void setCurrentSquidMutator(SquidMutator mutator) {
        currentSquidMutator.set(mutator);
    }

    /**
     * Gets the current squid mutator from the rendering context.
     */
    public static SquidMutator getCurrentSquidMutator() {
        return currentSquidMutator.get();
    }

    /**
     * Clears the current rendering context.
     * Call this after rendering is complete.
     */
    public static void clear() {
        currentBipedMutator.remove();
        currentSpiderMutator.remove();
        currentSquidMutator.remove();
        inMainModelRender.remove();
        currentVanillaModel.remove();
    }
}
