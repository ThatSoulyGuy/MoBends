package goblinbob.mobends.forge.network;

import com.mojang.logging.LogUtils;
import goblinbob.mobends.core.network.SharedNetworkConfiguration;
import goblinbob.mobends.forge.network.msg.MessageConfigRequest;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
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
            ForgeNetworkHandler.getChannel().sendToServer(new MessageConfigRequest());
        }
        catch (Exception e)
        {
            LOGGER.debug("Mo' Bends server configuration not available: {}", e.getMessage());
        }
    }

    @SubscribeEvent
    public void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event)
    {
        SharedNetworkConfiguration.INSTANCE.resetToDefaults();
    }
}
