package goblinbob.mobends.core.client.event;

import goblinbob.mobends.core.network.NetworkConfiguration;
import goblinbob.mobends.core.network.msg.ConfigRequestPayload;
import goblinbob.mobends.standard.main.MoBends;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.AbstractClientPlayer;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.bus.api.SubscribeEvent;

public class WorldJoinHandler
{

    @SubscribeEvent
    public void onPlayerJoinedServer(EntityJoinLevelEvent event)
    {
        if (event.getEntity() instanceof AbstractClientPlayer)
        {
            ClientPacketListener connection = Minecraft.getInstance().getConnection();

            // Check if the server has the Mo' Bends mod installed by checking if it accepts our packets
            boolean serverHasMod = connection != null && connection.hasChannel(ConfigRequestPayload.TYPE);

            if (serverHasMod)
            {
                // Server has Mo' Bends - set restrictive defaults, then request server config
                NetworkConfiguration.instance.onWorldJoin();

                // Request server configuration
                PacketDistributor.sendToServer(new ConfigRequestPayload());
                MoBends.LOG.info("Mo' Bends detected on server, requesting configuration...");
            }
            else
            {
                // Vanilla server or server without Mo' Bends - use permissive defaults (same as singleplayer)
                NetworkConfiguration.instance.onVanillaServerJoin();
                MoBends.LOG.info("Connected to vanilla server - using permissive defaults");
            }
        }
    }

}
