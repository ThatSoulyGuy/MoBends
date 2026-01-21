package goblinbob.mobends.core.client.event;

import goblinbob.mobends.core.addon.Addons;
import goblinbob.mobends.core.data.EntityDatabase;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.bus.api.SubscribeEvent;

public class DataUpdateHandler
{

    public static float partialTicks = 0.0f;
    protected static float ticks = 0.0f;
    public static float ticksPerFrame = 0.0f;

    public static float getTicks()
    {
        return ticks;
    }

    @SubscribeEvent
    public void updateAnimations(RenderFrameEvent.Pre event)
    {
        if (Minecraft.getInstance().level == null || Minecraft.getInstance().player == null)
            return;

        float renderTickTime = event.getPartialTick().getGameTimeDeltaPartialTick(false);

        if (!Minecraft.getInstance().isPaused())
        {
            DataUpdateHandler.partialTicks = renderTickTime;
        }

        final float newTicks = Minecraft.getInstance().player.tickCount + renderTickTime;

        if (DataUpdateHandler.ticks > newTicks)
        {
            onTicksRestart();
        }

        if (!(Minecraft.getInstance().level.isClientSide && Minecraft.getInstance().isPaused()))
        {
            DataUpdateHandler.ticksPerFrame = Math.min(Math.max(0F, newTicks - DataUpdateHandler.ticks), 1F);
            DataUpdateHandler.ticks = newTicks;

            EntityDatabase.instance.updateRender(renderTickTime);
            Addons.onRenderTick(renderTickTime);
        }
        else
        {
            DataUpdateHandler.ticksPerFrame = 0F;
        }
    }

    public static void onTicksRestart()
    {
        EntityDatabase.instance.onTicksRestart();
    }

    @SubscribeEvent
    public void onClientTick(ClientTickEvent.Pre event)
    {
        if (Minecraft.getInstance().player == null || Minecraft.getInstance().isPaused())
            return;

        EntityDatabase.instance.updateClient();
        Addons.onClientTick();
    }

}
