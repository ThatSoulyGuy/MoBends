package goblinbob.mobends.neoforge.network;

import goblinbob.mobends.core.network.SharedNetworkConfiguration;
import goblinbob.mobends.neoforge.network.msg.ConfigRequestPayload;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.network.PacketDistributor;

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
            PacketDistributor.sendToServer(new ConfigRequestPayload());
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
