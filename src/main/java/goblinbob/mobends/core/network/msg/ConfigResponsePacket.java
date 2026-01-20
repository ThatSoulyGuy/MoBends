package goblinbob.mobends.core.network.msg;

import com.mojang.logging.LogUtils;
import goblinbob.mobends.core.network.NetworkConfiguration;
import goblinbob.mobends.core.network.SharedProperty;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import org.slf4j.Logger;

import java.util.function.Supplier;

/**
 * This packet is sent by the server to a client as a response
 * to a {@link ConfigRequestPacket}.
 */
public class ConfigResponsePacket
{
    private static final Logger LOGGER = LogUtils.getLogger();

    private CompoundTag configData;

    /**
     * Constructor for creating the packet (server side).
     */
    public ConfigResponsePacket()
    {
        this.configData = new CompoundTag();
        NetworkConfiguration.instance.getSharedConfig().writeToNBT(this.configData);
    }

    /**
     * Constructor for receiving the packet (client side).
     */
    public ConfigResponsePacket(CompoundTag data)
    {
        this.configData = data;
    }

    /**
     * Encode the packet data to the buffer.
     */
    public static void encode(ConfigResponsePacket msg, FriendlyByteBuf buf)
    {
        buf.writeNbt(msg.configData);
    }

    /**
     * Decode the packet data from the buffer.
     */
    public static ConfigResponsePacket decode(FriendlyByteBuf buf)
    {
        CompoundTag tag = buf.readNbt();
        return new ConfigResponsePacket(tag != null ? tag : new CompoundTag());
    }

    /**
     * Handle the packet on the receiving side (client).
     */
    public static void handle(ConfigResponsePacket msg, Supplier<NetworkEvent.Context> ctx)
    {
        ctx.get().enqueueWork(() -> {
            if (msg.configData == null)
            {
                LOGGER.error("An error occurred while receiving server configuration.");
                return;
            }

            NetworkConfiguration.instance.getSharedConfig().readFromNBT(msg.configData);

            // Log received configuration
            final StringBuilder builder = new StringBuilder("Received Mo' Bends server configuration.\n");
            final Iterable<SharedProperty<?>> properties = NetworkConfiguration.instance.getSharedConfig().getProperties();
            for (SharedProperty<?> property : properties)
            {
                builder.append(String.format(" - %s: %s\n", property.getKey(), property.getValue()));
            }
            LOGGER.info(builder.toString());
        });
        ctx.get().setPacketHandled(true);
    }
}
