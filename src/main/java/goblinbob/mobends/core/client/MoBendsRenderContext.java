package goblinbob.mobends.core.client;

import goblinbob.mobends.standard.mutators.BipedMutator;
import goblinbob.mobends.standard.mutators.SpiderMutator;
import goblinbob.mobends.standard.mutators.SquidMutator;
import goblinbob.mobends.standard.mutators.WolfMutator;

public class MoBendsRenderContext {

    private static final ThreadLocal<BipedMutator<?, ?, ?>> currentBipedMutator = new ThreadLocal<>();
    private static final ThreadLocal<SpiderMutator> currentSpiderMutator = new ThreadLocal<>();
    private static final ThreadLocal<SquidMutator> currentSquidMutator = new ThreadLocal<>();
    private static final ThreadLocal<WolfMutator> currentWolfMutator = new ThreadLocal<>();

    private static final ThreadLocal<Boolean> inMainModelRender = ThreadLocal.withInitial(() -> false);

    private static final ThreadLocal<net.minecraft.client.model.HumanoidModel<?>> currentVanillaModel = new ThreadLocal<>();

    private static final ThreadLocal<net.minecraft.world.entity.LivingEntity> currentEntity = new ThreadLocal<>();

    private static final ThreadLocal<net.minecraft.client.renderer.MultiBufferSource> currentBufferSource = new ThreadLocal<>();
    private static final ThreadLocal<Integer> currentPackedLight = new ThreadLocal<>();


    public static void beginMainModelRender() {
        inMainModelRender.set(true);
    }

    public static void endMainModelRender() {
        inMainModelRender.set(false);
    }

    public static boolean isInMainModelRender() {
        return inMainModelRender.get();
    }

    private static final ThreadLocal<Integer> guiEntityRenderDepth = ThreadLocal.withInitial(() -> 0);

    public static void beginGuiEntityRender() {
        guiEntityRenderDepth.set(guiEntityRenderDepth.get() + 1);
    }

    public static void endGuiEntityRender() {
        guiEntityRenderDepth.set(Math.max(0, guiEntityRenderDepth.get() - 1));
    }

    public static boolean isInGuiEntityRender() {
        return guiEntityRenderDepth.get() > 0;
    }

    public static void setCurrentVanillaModel(net.minecraft.client.model.HumanoidModel<?> model) {
        currentVanillaModel.set(model);
    }

    public static net.minecraft.client.model.HumanoidModel<?> getCurrentVanillaModel() {
        return currentVanillaModel.get();
    }

    public static void setCurrentEntity(net.minecraft.world.entity.LivingEntity entity) {
        currentEntity.set(entity);
    }

    public static net.minecraft.world.entity.LivingEntity getCurrentEntity() {
        return currentEntity.get();
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

    public static void setCurrentWolfMutator(WolfMutator mutator) {
        currentWolfMutator.set(mutator);
    }

    public static WolfMutator getCurrentWolfMutator() {
        return currentWolfMutator.get();
    }

    public static void setCurrentRenderBuffers(net.minecraft.client.renderer.MultiBufferSource bufferSource, int packedLight) {
        currentBufferSource.set(bufferSource);
        currentPackedLight.set(packedLight);
    }

    public static net.minecraft.client.renderer.MultiBufferSource getCurrentBufferSource() {
        return currentBufferSource.get();
    }

    public static int getCurrentPackedLight() {
        Integer light = currentPackedLight.get();
        return light == null ? 15728880 : light;
    }

    private static boolean inArmorRender = false;

    public static void beginArmorRender() {
        inArmorRender = true;
    }

    public static void endArmorRender() {
        inArmorRender = false;
    }

    public static boolean isInArmorRender() {
        return inArmorRender;
    }

    public static void clear() {
        inArmorRender = false;
        currentBipedMutator.remove();
        currentSpiderMutator.remove();
        currentSquidMutator.remove();
        currentWolfMutator.remove();
        inMainModelRender.remove();
        currentVanillaModel.remove();
        currentEntity.remove();
        currentBufferSource.remove();
        currentPackedLight.remove();
    }
}
