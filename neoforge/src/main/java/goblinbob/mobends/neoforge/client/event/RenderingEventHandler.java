package goblinbob.mobends.neoforge.client.event;

import com.mojang.blaze3d.vertex.PoseStack;
import goblinbob.mobends.api.addon.Addons;
import goblinbob.mobends.core.bender.EntityBender;
import goblinbob.mobends.core.bender.EntityBenderRegistry;
import goblinbob.mobends.core.client.MoBendsRenderContext;
import goblinbob.mobends.core.client.AnimatedRiderAnchor;
import goblinbob.mobends.core.client.OffscreenAnimationUpdater;
import goblinbob.mobends.core.client.event.DataUpdateHandler;
import goblinbob.mobends.core.data.EntityDatabase;
import goblinbob.mobends.core.data.LivingEntityData;
import goblinbob.mobends.lib.flux.ComputedDependencyHelper;
import goblinbob.mobends.core.mutators.Mutator;
import goblinbob.mobends.core.util.BenderHelper;
import goblinbob.mobends.compat.ModCompatManager;
import goblinbob.mobends.standard.mutators.BipedMutator;
import goblinbob.mobends.standard.mutators.PlayerMutator;
import goblinbob.mobends.standard.mutators.SpiderMutator;
import goblinbob.mobends.standard.mutators.SquidMutator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.client.event.RenderLivingEvent;

import java.util.HashSet;
import java.util.Set;

public class RenderingEventHandler
{
    private static final Set<Integer> entitiesWithPushedPose = new HashSet<>();
    private static final Set<Integer> ridersWithPushedPose = new HashSet<>();

    @SubscribeEvent
    public void onClientTick(ClientTickEvent.Post event)
    {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.isPaused())
            return;


        goblinbob.mobends.core.bender.BenderDiscovery.scanForDerivedBenders();


        EntityDatabase.instance.updateClient();
        Addons.onClientTick();
    }

    @SubscribeEvent
    public void onRenderTick(RenderFrameEvent.Pre event)
    {
        advanceAnimations(event.getPartialTick().getGameTimeDeltaPartialTick(false));
        renderTickDrivenThisFrame = true;
    }

    private static boolean renderTickDrivenThisFrame = false;

    private static void advanceAnimations(float renderTickTime)
    {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null)
            return;

        ComputedDependencyHelper.reevaluateDirty();

        if (!mc.isPaused())
        {
            DataUpdateHandler.partialTicks = renderTickTime;
        }

        final float newTicks = mc.player.tickCount + renderTickTime;

        if (DataUpdateHandler.checkTicksRestart(newTicks))
        {
            EntityDatabase.instance.onTicksRestart();
        }

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

    @SubscribeEvent
    public void beforeHandRender(RenderHandEvent event)
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


    @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.LOWEST)
    public void beforeLivingRender(RenderLivingEvent.Pre<?, ?> event)
    {
        LivingEntity entity = event.getEntity();
        LivingEntityRenderer renderer = event.getRenderer();
        float partialTicks = event.getPartialTick();
        PoseStack poseStack = event.getPoseStack();

        if (entity == Minecraft.getInstance().player)
        {
            OffscreenAnimationUpdater.markPlayerRendered();
        }

        net.minecraft.world.phys.Vec3 riderOffset = AnimatedRiderAnchor.getRenderOffset(entity, partialTicks);
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

                MoBendsRenderContext.setCurrentEntity(entity);

                if (rawMutator instanceof BipedMutator<?, ?, ?> bipedMutator)
                {
                    MoBendsRenderContext.setCurrentBipedMutator(bipedMutator);
                    MoBendsRenderContext.beginMainModelRender();

                    EntityModel<?> model = renderer.getModel();
                    HumanoidModel<?> humanoidModel = bipedMutator.humanoidViewOf(model);
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
                else if (rawMutator instanceof goblinbob.mobends.standard.mutators.WolfMutator wolfMutator)
                {
                    MoBendsRenderContext.setCurrentWolfMutator(wolfMutator);
                    MoBendsRenderContext.beginMainModelRender();
                }
                MoBendsRenderContext.setCurrentRenderBuffers(event.getMultiBufferSource(), event.getPackedLight());
                bender.beforeRender(data, entity, partialTicks, poseStack);
            }
        }
        else
        {
            bender.deapplyMutation(renderer, entity);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @SubscribeEvent
    public void afterLivingRender(RenderLivingEvent.Post<?, ?> event)
    {
        goblinbob.mobends.compat.CarryOnCompat.captureAnchor(
                event.getEntity(), MoBendsRenderContext.getCurrentBipedMutator());

        MoBendsRenderContext.clear();

        LivingEntity entity = event.getEntity();
        EntityBender bender = EntityBenderRegistry.instance.getForEntity(entity);

        if (bender != null && entitiesWithPushedPose.remove(entity.getId()))
        {
            bender.afterRender(entity, event.getPartialTick(), event.getPoseStack());
            event.getPoseStack().popPose();
        }

        if (ridersWithPushedPose.remove(entity.getId()))
        {
            event.getPoseStack().popPose();
        }
    }

    @SubscribeEvent
    public void onRenderLevelStage(net.neoforged.neoforge.client.event.RenderLevelStageEvent event)
    {
        if (event.getStage() == net.neoforged.neoforge.client.event.RenderLevelStageEvent.Stage.AFTER_SKY)
        {
            if (!renderTickDrivenThisFrame)
            {
                advanceAnimations(event.getPartialTick().getGameTimeDeltaPartialTick(false));
            }
            renderTickDrivenThisFrame = false;


            entitiesWithPushedPose.clear();

            ridersWithPushedPose.clear();


            goblinbob.mobends.core.client.TrailRenderQueue.clear();
        }
        else if (event.getStage() == net.neoforged.neoforge.client.event.RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS)
        {
            goblinbob.mobends.standard.client.renderer.entity.ArrowTrailManager
                    .renderExternalTrails(event.getPoseStack());

            goblinbob.mobends.core.client.TrailRenderQueue.flush();
        }
    }

}
