package goblinbob.mobends.forge.network;

import goblinbob.mobends.core.network.SharedNetworkConfiguration;
import goblinbob.mobends.forge.network.msg.MessageConfigRequest;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ConfigSyncClientHandler
{

    @SubscribeEvent
    public void onLoggingIn(ClientPlayerNetworkEvent.LoggingIn event)
    {
        SharedNetworkConfiguration.INSTANCE.resetToDefaults();

        if (Minecraft.getInstance().getConnection() == null)
        {
            return;
        }

        try
        {
            ForgeNetworkHandler.getChannel().sendToServer(new MessageConfigRequest());
        }
        catch (Exception e)
        {
        }
    }

    @SubscribeEvent
    public void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event)
    {
        SharedNetworkConfiguration.INSTANCE.resetToDefaults();

        goblinbob.mobends.core.bender.BenderDiscovery.reset();
    }
}
