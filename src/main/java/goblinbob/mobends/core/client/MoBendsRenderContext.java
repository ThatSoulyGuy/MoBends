package goblinbob.mobends.core.client;

import goblinbob.mobends.standard.mutators.BipedMutator;
import goblinbob.mobends.standard.mutators.SpiderMutator;
import goblinbob.mobends.standard.mutators.WolfMutator;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Thread-local context for passing mutation state during rendering.
 * Used by mixins to determine if custom rendering should be applied.
 */
@OnlyIn(Dist.CLIENT)
public class MoBendsRenderContext {

    private static final ThreadLocal<BipedMutator<?, ?, ?>> currentBipedMutator = new ThreadLocal<>();
    private static final ThreadLocal<SpiderMutator> currentSpiderMutator = new ThreadLocal<>();
    private static final ThreadLocal<WolfMutator> currentWolfMutator = new ThreadLocal<>();

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
     * Sets the current wolf mutator for the rendering context.
     */
    public static void setCurrentWolfMutator(WolfMutator mutator) {
        currentWolfMutator.set(mutator);
    }

    /**
     * Gets the current wolf mutator from the rendering context.
     */
    public static WolfMutator getCurrentWolfMutator() {
        return currentWolfMutator.get();
    }

    /**
     * Clears the current rendering context.
     * Call this after rendering is complete.
     */
    public static void clear() {
        currentBipedMutator.remove();
        currentSpiderMutator.remove();
        currentWolfMutator.remove();
    }
}
