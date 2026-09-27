package goblinbob.mobends.forge.client.event;

import goblinbob.mobends.compat.EpicFightCompat;
import goblinbob.mobends.core.client.event.LivingRenderEvents;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class RenderingEventHandler
{
    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event)
    {
        if (event.phase == TickEvent.Phase.END)
            LivingRenderEvents.onClientTickEnd();
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event)
    {
        if (event.phase == TickEvent.Phase.START)
            LivingRenderEvents.onRenderFrameStart(event.renderTickTime);
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
            LivingRenderEvents.afterSky(event.getPoseStack(), event.getPartialTick());
        }
        else if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS)
        {
            LivingRenderEvents.afterTranslucentBlocks(event.getPoseStack());
        }
    }
}
