package goblinbob.mobends.core.network.msg;

import com.mojang.logging.LogUtils;
import goblinbob.mobends.core.network.NetworkConfiguration;
import org.slf4j.Logger;
import goblinbob.mobends.core.network.SharedProperty;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * This message is sent by the server to a client as a response
 * to a {@link MessageConfigRequest} message.
 */
public class MessageConfigResponse
{
    private static final Logger LOGGER = LogUtils.getLogger();

    private CompoundTag configData;

    /**
     * Necessary empty constructor.
     */
    public MessageConfigResponse()
    {
        this.configData = new CompoundTag();
        NetworkConfiguration.instance.getSharedConfig().writeToNBT(this.configData);
    }

    private MessageConfigResponse(CompoundTag configData)
    {
        this.configData = configData;
    }

    public static void encode(MessageConfigResponse msg, FriendlyByteBuf buf)
    {
        buf.writeNbt(msg.configData);
    }

    public static MessageConfigResponse decode(FriendlyByteBuf buf)
    {
        CompoundTag tag = buf.readNbt();
        if (tag == null)
        {
            LOGGER.error("An error occurred while receiving server configuration.");
            return new MessageConfigResponse(new CompoundTag());
        }
        return new MessageConfigResponse(tag);
    }

    public static void handle(MessageConfigResponse msg, Supplier<NetworkEvent.Context> ctx)
    {
        ctx.get().enqueueWork(() -> {
            NetworkConfiguration.instance.getSharedConfig().readFromNBT(msg.configData);

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
