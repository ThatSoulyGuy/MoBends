package goblinbob.mobends.core.client;

import goblinbob.mobends.standard.mutators.BipedMutator;
import goblinbob.mobends.standard.mutators.SpiderMutator;
import goblinbob.mobends.standard.mutators.SquidMutator;
import goblinbob.mobends.standard.mutators.WolfMutator;

public class MoBendsRenderContext {

    private static BipedMutator<?, ?, ?> currentBipedMutator;
    private static SpiderMutator currentSpiderMutator;
    private static SquidMutator currentSquidMutator;
    private static WolfMutator currentWolfMutator;

    private static boolean inMainModelRender = false;

    private static net.minecraft.client.model.HumanoidModel<?> currentVanillaModel;

    private static net.minecraft.world.entity.LivingEntity currentEntity;

    private static net.minecraft.client.renderer.MultiBufferSource currentBufferSource;


    public static void beginMainModelRender() {
        inMainModelRender = true;
    }

    public static void endMainModelRender() {
        inMainModelRender = false;
    }

    public static boolean isInMainModelRender() {
        return inMainModelRender;
    }

    private static int guiEntityRenderDepth = 0;

    public static void beginGuiEntityRender() {
        guiEntityRenderDepth++;
    }

    public static void endGuiEntityRender() {
        guiEntityRenderDepth = Math.max(0, guiEntityRenderDepth - 1);
    }

    public static boolean isInGuiEntityRender() {
        return guiEntityRenderDepth > 0;
    }

    public static void setCurrentVanillaModel(net.minecraft.client.model.HumanoidModel<?> model) {
        currentVanillaModel = model;
    }

    public static net.minecraft.client.model.HumanoidModel<?> getCurrentVanillaModel() {
        return currentVanillaModel;
    }

    public static void setCurrentEntity(net.minecraft.world.entity.LivingEntity entity) {
        currentEntity = entity;
    }

    public static net.minecraft.world.entity.LivingEntity getCurrentEntity() {
        return currentEntity;
    }

    public static void setCurrentBipedMutator(BipedMutator<?, ?, ?> mutator) {
        currentBipedMutator = mutator;
    }

    public static BipedMutator<?, ?, ?> getCurrentBipedMutator() {
        return currentBipedMutator;
    }

    public static void setCurrentSpiderMutator(SpiderMutator mutator) {
        currentSpiderMutator = mutator;
    }

    public static SpiderMutator getCurrentSpiderMutator() {
        return currentSpiderMutator;
    }

    public static void setCurrentSquidMutator(SquidMutator mutator) {
        currentSquidMutator = mutator;
    }

    public static SquidMutator getCurrentSquidMutator() {
        return currentSquidMutator;
    }

    public static void setCurrentWolfMutator(WolfMutator mutator) {
        currentWolfMutator = mutator;
    }

    public static WolfMutator getCurrentWolfMutator() {
        return currentWolfMutator;
    }

    public static void setCurrentRenderBuffers(net.minecraft.client.renderer.MultiBufferSource bufferSource) {
        currentBufferSource = bufferSource;
    }

    public static net.minecraft.client.renderer.MultiBufferSource getCurrentBufferSource() {
        return currentBufferSource;
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
        currentBipedMutator = null;
        currentSpiderMutator = null;
        currentSquidMutator = null;
        currentWolfMutator = null;
        inMainModelRender = false;
        currentVanillaModel = null;
        currentEntity = null;
        currentBufferSource = null;
    }
}
