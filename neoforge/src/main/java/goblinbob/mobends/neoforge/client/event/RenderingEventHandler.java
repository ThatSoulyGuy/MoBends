package goblinbob.mobends.neoforge.client.event;

import goblinbob.mobends.compat.EpicFightCompat;
import goblinbob.mobends.core.client.event.LivingRenderEvents;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.RenderLivingEvent;

public class RenderingEventHandler
{
    @SubscribeEvent
    public void onClientTick(ClientTickEvent.Post event)
    {
        LivingRenderEvents.onClientTickEnd();
    }

    @SubscribeEvent
    public void onRenderTick(RenderFrameEvent.Pre event)
    {
        LivingRenderEvents.onRenderFrameStart(event.getPartialTick().getGameTimeDeltaPartialTick(false));
    }

    @SubscribeEvent
    public void beforeHandRender(RenderHandEvent event)
    {
        LivingRenderEvents.beforeHandRender();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void yieldLayersToEpicFight(RenderLivingEvent.Pre<?, ?> event)
    {
        EpicFightCompat.suspendLayerSwap(event.getEntity(), event.getRenderer());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public void reclaimLayersFromEpicFight(RenderLivingEvent.Pre<?, ?> event)
    {
        EpicFightCompat.resumeLayerSwap();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void beforeLivingRender(RenderLivingEvent.Pre<?, ?> event)
    {
        LivingRenderEvents.beforeLivingRender(event.getEntity(), event.getRenderer(), event.getPartialTick(),
                event.getPoseStack(), event.getMultiBufferSource(), event.getPackedLight());
    }

    @SubscribeEvent
    public void afterLivingRender(RenderLivingEvent.Post<?, ?> event)
    {
        LivingRenderEvents.afterLivingRender(event.getEntity(), event.getPartialTick(), event.getPoseStack());
    }

    @SubscribeEvent
    public void onRenderLevelStage(RenderLevelStageEvent event)
    {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_SKY)
        {
            LivingRenderEvents.afterSky(event.getPoseStack(), event.getPartialTick().getGameTimeDeltaPartialTick(false));
        }
        else if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS)
        {
            LivingRenderEvents.afterTranslucentBlocks(event.getPoseStack());
        }
    }
}
