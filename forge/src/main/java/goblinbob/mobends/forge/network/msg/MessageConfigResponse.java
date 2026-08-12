package goblinbob.mobends.forge.network.msg;

import com.mojang.logging.LogUtils;
import goblinbob.mobends.core.network.SharedNetworkConfiguration;
import goblinbob.mobends.core.network.SharedProperty;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import org.slf4j.Logger;

import java.util.function.Supplier;

public class MessageConfigResponse
{
    private static final Logger LOGGER = LogUtils.getLogger();

    private CompoundTag configData;

    public MessageConfigResponse()
    {
        this.configData = new CompoundTag();
        SharedNetworkConfiguration.INSTANCE.getSharedConfig().writeToNBT(this.configData);
    }

    private MessageConfigResponse(CompoundTag configData)
    {
        this.configData = configData;
    }

    public static void encode(MessageConfigResponse message, FriendlyByteBuf buf)
    {
        buf.writeNbt(message.configData);
    }

    public static MessageConfigResponse decode(FriendlyByteBuf buf)
    {
        return new MessageConfigResponse(buf.readNbt());
    }

    public static void handle(MessageConfigResponse message, Supplier<NetworkEvent.Context> contextSupplier)
    {
        final NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() -> {
            if (message.configData == null)
            {
                LOGGER.error("An error occurred while receiving server configuration.");
                return;
            }

            SharedNetworkConfiguration.INSTANCE.getSharedConfig().readFromNBT(message.configData);

            final StringBuilder builder = new StringBuilder("Received Mo' Bends server configuration.\n");
            final Iterable<SharedProperty<?>> properties =
                    SharedNetworkConfiguration.INSTANCE.getSharedConfig().getProperties();
            for (SharedProperty<?> property : properties)
            {
                builder.append(String.format(" - %s: %s\n", property.getKey(), property.getValue()));
            }
            LOGGER.info(builder.toString());
        });

        context.setPacketHandled(true);
    }

}
