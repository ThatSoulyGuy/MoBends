package goblinbob.mobends.core.client.event;

import com.mojang.blaze3d.vertex.PoseStack;
import goblinbob.mobends.api.addon.Addons;
import goblinbob.mobends.compat.CarryOnCompat;
import goblinbob.mobends.compat.CustomNpcsCompat;
import goblinbob.mobends.compat.ModCompatManager;
import goblinbob.mobends.core.bender.BenderDiscovery;
import goblinbob.mobends.core.bender.EntityBender;
import goblinbob.mobends.core.bender.EntityBenderRegistry;
import goblinbob.mobends.core.client.AnimatedRiderAnchor;
import goblinbob.mobends.core.client.MoBendsRenderContext;
import goblinbob.mobends.core.client.OffscreenAnimationUpdater;
import goblinbob.mobends.core.client.TrailRenderQueue;
import goblinbob.mobends.core.data.EntityDatabase;
import goblinbob.mobends.core.data.LivingEntityData;
import goblinbob.mobends.core.mutators.Mutator;
import goblinbob.mobends.core.util.BenderHelper;
import goblinbob.mobends.standard.client.renderer.entity.ArrowTrailManager;
import goblinbob.mobends.standard.mutators.BipedMutator;
import goblinbob.mobends.standard.mutators.PlayerMutator;
import goblinbob.mobends.standard.mutators.SpiderMutator;
import goblinbob.mobends.standard.mutators.SquidMutator;
import goblinbob.mobends.standard.mutators.WolfMutator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.Set;

public final class LivingRenderEvents
{
    private static final Set<Integer> entitiesWithPushedPose = new HashSet<>();
    private static final Set<Integer> ridersWithPushedPose = new HashSet<>();

    private static boolean renderTickDrivenThisFrame = false;

    private LivingRenderEvents()
    {
    }

    public static void onClientTickEnd()
    {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.isPaused())
            return;

        BenderDiscovery.scanForDerivedBenders();

        EntityDatabase.instance.updateClient();
        Addons.onClientTick();
    }

    public static void onRenderFrameStart(float renderTickTime)
    {
        advanceAnimations(renderTickTime);
        renderTickDrivenThisFrame = true;
    }

    private static void advanceAnimations(float renderTickTime)
    {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null)
            return;

        if (!mc.isPaused())
        {
            DataUpdateHandler.partialTicks = renderTickTime;
        }

        final float newTicks = mc.player.tickCount + renderTickTime;

        if (!(mc.level.isClientSide && mc.isPaused()))
        {
            DataUpdateHandler.update(renderTickTime, newTicks);
            EntityDatabase.instance.updateRender(renderTickTime);
            Addons.onRenderTick(renderTickTime);
            OffscreenAnimationUpdater.updateIfNotRendered(renderTickTime);
        }
        else
        {
            DataUpdateHandler.onPaused();
        }
    }

    public static void beforeHandRender()
    {
        Minecraft mc = Minecraft.getInstance();
        Entity viewEntity = mc.getCameraEntity();

        if (!(viewEntity instanceof AbstractClientPlayer))
            return;

        AbstractClientPlayer player = (AbstractClientPlayer) viewEntity;

        if (!BenderHelper.isEntityAnimated(player))
            return;

        PlayerRenderer renderPlayer = (PlayerRenderer) mc.getEntityRenderDispatcher().getRenderer(player);
        PlayerMutator mutator = (PlayerMutator) BenderHelper.getMutatorForRenderer(AbstractClientPlayer.class, renderPlayer);
        if (mutator != null)
        {
            mutator.poseForFirstPersonView();
            mutator.restoreVanillaPivots(renderPlayer.getModel());
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void beforeLivingRender(LivingEntity entity, LivingEntityRenderer renderer, float partialTicks,
                                          PoseStack poseStack, MultiBufferSource bufferSource, int packedLight)
    {
        if (entity == Minecraft.getInstance().player)
        {
            OffscreenAnimationUpdater.markPlayerRendered();
        }

        Vec3 riderOffset = AnimatedRiderAnchor.getRenderOffset(entity, partialTicks);
        if (riderOffset != null)
        {
            poseStack.pushPose();
            poseStack.translate(riderOffset.x, riderOffset.y, riderOffset.z);
            ridersWithPushedPose.add(entity.getId());
        }

        EntityBender bender = EntityBenderRegistry.instance.getForEntity(entity);
        if (bender == null)
            return;

        if (ModCompatManager.shouldDeferAnimation(entity))
        {
            bender.deapplyMutation(renderer, entity);
            return;
        }

        if (entity.isSpectator())
        {
            bender.deapplyMutation(renderer, entity);
            return;
        }

        poseStack.pushPose();
        entitiesWithPushedPose.add(entity.getId());

        if (bender.isAnimated())
        {
            if (CustomNpcsCompat.routeRender(entity, renderer, partialTicks, poseStack, bufferSource, packedLight)
                    != CustomNpcsCompat.RenderRoute.OWN_MODEL)
            {
                return;
            }

            boolean mutationApplied = bender.applyMutation(renderer, entity, partialTicks);

            if (mutationApplied)
            {
                final Object rawMutator = bender.getMutator(renderer);
                if (rawMutator == null)
                {
                    return;
                }

                final Mutator<?, LivingEntity, ?> mutator = (Mutator<?, LivingEntity, ?>) rawMutator;
                final LivingEntityData<LivingEntity> data = (LivingEntityData<LivingEntity>) mutator.getData(entity);

                beginMutatedRender(entity, rawMutator, renderer, bufferSource);
                bender.beforeRender(data, entity, partialTicks, poseStack);
            }
        }
        else
        {
            bender.deapplyMutation(renderer, entity);
        }
    }

    public static void beginMutatedRender(LivingEntity entity, Object rawMutator, LivingEntityRenderer<?, ?> renderer,
                                          MultiBufferSource bufferSource)
    {
        MoBendsRenderContext.setCurrentEntity(entity);

        if (rawMutator instanceof BipedMutator<?, ?, ?> bipedMutator)
        {
            MoBendsRenderContext.setCurrentBipedMutator(bipedMutator);
            MoBendsRenderContext.beginMainModelRender();

            HumanoidModel<?> humanoidModel = bipedMutator.humanoidViewOf(renderer.getModel());
            if (humanoidModel != null)
            {
                MoBendsRenderContext.setCurrentVanillaModel(humanoidModel);
                bipedMutator.syncPosesToVanillaModel(humanoidModel);
            }
        }
        else if (rawMutator instanceof SpiderMutator spiderMutator)
        {
            MoBendsRenderContext.setCurrentSpiderMutator(spiderMutator);
            MoBendsRenderContext.beginMainModelRender();
        }
        else if (rawMutator instanceof SquidMutator squidMutator)
        {
            MoBendsRenderContext.setCurrentSquidMutator(squidMutator);
            MoBendsRenderContext.beginMainModelRender();
        }
        else if (rawMutator instanceof WolfMutator wolfMutator)
        {
            MoBendsRenderContext.setCurrentWolfMutator(wolfMutator);
            MoBendsRenderContext.beginMainModelRender();
        }

        MoBendsRenderContext.setCurrentRenderBuffers(bufferSource);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void afterLivingRender(LivingEntity entity, float partialTicks, PoseStack poseStack)
    {
        CarryOnCompat.captureAnchor(entity, MoBendsRenderContext.getCurrentBipedMutator());

        MoBendsRenderContext.clear();

        EntityBender bender = EntityBenderRegistry.instance.getForEntity(entity);

        if (bender != null && entitiesWithPushedPose.remove(entity.getId()))
        {
            bender.afterRender(entity, partialTicks, poseStack);
            poseStack.popPose();
        }

        if (ridersWithPushedPose.remove(entity.getId()))
        {
            poseStack.popPose();
        }
    }

    public static void afterSky(PoseStack poseStack, float partialTicks)
    {
        if (!renderTickDrivenThisFrame)
        {
            advanceAnimations(partialTicks);
        }
        renderTickDrivenThisFrame = false;

        entitiesWithPushedPose.clear();
        ridersWithPushedPose.clear();

        TrailRenderQueue.clear();

        final Vec3 cameraPosition = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        TrailRenderQueue.beginFrame(poseStack.last().pose(), cameraPosition.x, cameraPosition.y, cameraPosition.z);
    }

    public static void afterTranslucentBlocks(PoseStack poseStack)
    {
        ArrowTrailManager.renderExternalTrails(poseStack);

        TrailRenderQueue.flush();
    }
}
