package goblinbob.mobends.core.network.msg;

import goblinbob.mobends.core.network.NetworkHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * This packet is sent by the client to the server to request
 * the server specific configuration.
 */
public class ConfigRequestPacket
{
    /**
     * Constructor for creating the packet.
     */
    public ConfigRequestPacket()
    {
    }

    /**
     * Encode the packet data to the buffer.
     */
    public static void encode(ConfigRequestPacket msg, FriendlyByteBuf buf)
    {
        // No data to encode
    }

    /**
     * Decode the packet data from the buffer.
     */
    public static ConfigRequestPacket decode(FriendlyByteBuf buf)
    {
        return new ConfigRequestPacket();
    }

    /**
     * Handle the packet on the receiving side (server).
     */
    public static void handle(ConfigRequestPacket msg, Supplier<NetworkEvent.Context> ctx)
    {
        ctx.get().enqueueWork(() -> {
            ServerPlayer sender = ctx.get().getSender();
            if (sender != null)
            {
                // Send back the config response
                NetworkHandler.sendToPlayer(new ConfigResponsePacket(), sender);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
