package goblinbob.mobends.core.client.event;

import goblinbob.mobends.core.network.NetworkConfiguration;
import goblinbob.mobends.core.network.NetworkHandler;
import goblinbob.mobends.core.network.msg.ConfigRequestPacket;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class WorldJoinHandler
{

    @SubscribeEvent
    public void onPlayerJoinedServer(EntityJoinLevelEvent event)
    {
        if (event.getEntity() instanceof AbstractClientPlayer)
        {
            // Setting the most restrictive configuration
            NetworkConfiguration.instance.onWorldJoin();

            // Sending a request to the server for the server-specific config.
            NetworkHandler.sendToServer(new ConfigRequestPacket());
        }
    }

}
