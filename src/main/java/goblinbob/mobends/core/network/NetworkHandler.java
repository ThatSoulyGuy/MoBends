package goblinbob.mobends.core.network;

import goblinbob.mobends.core.network.msg.ConfigRequestPacket;
import goblinbob.mobends.core.network.msg.ConfigResponsePacket;
import goblinbob.mobends.standard.main.ModStatics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;

/**
 * Network handler for Mo' Bends.
 * Handles packet registration and sending for 1.20.1.
 */
public class NetworkHandler
{
    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(ModStatics.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    private static int nextId()
    {
        return packetId++;
    }

    /**
     * Register all network packets.
     * Call this during common setup.
     */
    public static void register()
    {
        INSTANCE.registerMessage(nextId(),
                ConfigRequestPacket.class,
                ConfigRequestPacket::encode,
                ConfigRequestPacket::decode,
                ConfigRequestPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));

        INSTANCE.registerMessage(nextId(),
                ConfigResponsePacket.class,
                ConfigResponsePacket::encode,
                ConfigResponsePacket::decode,
                ConfigResponsePacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }

    /**
     * Send a packet to the server.
     */
    public static <MSG> void sendToServer(MSG message)
    {
        INSTANCE.sendToServer(message);
    }

    /**
     * Send a packet to a specific player.
     */
    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player)
    {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

    /**
     * Send a packet to all players.
     */
    public static <MSG> void sendToAll(MSG message)
    {
        INSTANCE.send(PacketDistributor.ALL.noArg(), message);
    }
}
