package goblinbob.mobends.core.client.event;

import goblinbob.mobends.core.addon.Addons;
import goblinbob.mobends.core.data.EntityDatabase;
import net.minecraft.client.Minecraft;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;

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
    public void updateAnimations(TickEvent.RenderTickEvent event)
    {
        if (event.phase == Phase.END)
            return;
        if (Minecraft.getInstance().level == null || Minecraft.getInstance().player == null)
            return;

        if (!Minecraft.getInstance().isPaused())
        {
            DataUpdateHandler.partialTicks = event.renderTickTime;
        }

        final float newTicks = Minecraft.getInstance().player.tickCount + event.renderTickTime;

        if (DataUpdateHandler.ticks > newTicks)
        {
            onTicksRestart();
        }

        if (!(Minecraft.getInstance().level.isClientSide && Minecraft.getInstance().isPaused()))
        {
            DataUpdateHandler.ticksPerFrame = Math.min(Math.max(0F, newTicks - DataUpdateHandler.ticks), 1F);
            DataUpdateHandler.ticks = newTicks;

            EntityDatabase.instance.updateRender(event.renderTickTime);
            Addons.onRenderTick(event.renderTickTime);
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
    public void onClientTick(TickEvent.ClientTickEvent event)
    {
        if (event.phase == Phase.END || Minecraft.getInstance().player == null || Minecraft.getInstance().isPaused())
            return;

        EntityDatabase.instance.updateClient();
        Addons.onClientTick();
    }

}
