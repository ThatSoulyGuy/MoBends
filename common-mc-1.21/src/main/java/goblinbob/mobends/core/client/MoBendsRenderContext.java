package goblinbob.mobends.core.client;

import goblinbob.mobends.standard.mutators.BipedMutator;
import goblinbob.mobends.standard.mutators.SpiderMutator;
import goblinbob.mobends.standard.mutators.SquidMutator;

public class MoBendsRenderContext {

    private static final ThreadLocal<BipedMutator<?, ?, ?>> currentBipedMutator = new ThreadLocal<>();
    private static final ThreadLocal<SpiderMutator> currentSpiderMutator = new ThreadLocal<>();
    private static final ThreadLocal<SquidMutator> currentSquidMutator = new ThreadLocal<>();

    private static final ThreadLocal<Boolean> inMainModelRender = ThreadLocal.withInitial(() -> false);

    private static final ThreadLocal<net.minecraft.client.model.HumanoidModel<?>> currentVanillaModel = new ThreadLocal<>();

    public static void beginMainModelRender() {
        inMainModelRender.set(true);
    }

    public static void endMainModelRender() {
        inMainModelRender.set(false);
    }

    public static boolean isInMainModelRender() {
        return inMainModelRender.get();
    }

    public static void setCurrentVanillaModel(net.minecraft.client.model.HumanoidModel<?> model) {
        currentVanillaModel.set(model);
    }

    public static net.minecraft.client.model.HumanoidModel<?> getCurrentVanillaModel() {
        return currentVanillaModel.get();
    }

    public static void setCurrentBipedMutator(BipedMutator<?, ?, ?> mutator) {
        currentBipedMutator.set(mutator);
    }

    public static BipedMutator<?, ?, ?> getCurrentBipedMutator() {
        return currentBipedMutator.get();
    }

    public static void setCurrentSpiderMutator(SpiderMutator mutator) {
        currentSpiderMutator.set(mutator);
    }

    public static SpiderMutator getCurrentSpiderMutator() {
        return currentSpiderMutator.get();
    }

    public static void setCurrentSquidMutator(SquidMutator mutator) {
        currentSquidMutator.set(mutator);
    }

    public static SquidMutator getCurrentSquidMutator() {
        return currentSquidMutator.get();
    }

    public static void clear() {
        currentBipedMutator.remove();
        currentSpiderMutator.remove();
        currentSquidMutator.remove();
        inMainModelRender.remove();
        currentVanillaModel.remove();
    }
}
