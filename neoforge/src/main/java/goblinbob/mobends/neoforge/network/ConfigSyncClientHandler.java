package goblinbob.mobends.neoforge.network;

import com.mojang.logging.LogUtils;
import goblinbob.mobends.core.network.SharedNetworkConfiguration;
import goblinbob.mobends.neoforge.network.msg.ConfigRequestPayload;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.slf4j.Logger;

public class ConfigSyncClientHandler
{
    private static final Logger LOGGER = LogUtils.getLogger();

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

        // Entity types and models can differ on the next world, so the derived-bender scan
        // has to be allowed to run again.
        goblinbob.mobends.core.bender.BenderDiscovery.reset();
    }
}
