package goblinbob.mobends.core.network.msg;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * This message is sent by the client to the server to request
 * the server specific configuration.
 */
public class MessageConfigRequest
{
    /**
     * Necessary empty constructor.
     */
    public MessageConfigRequest() {}

    public static void encode(MessageConfigRequest msg, FriendlyByteBuf buf)
    {
        // No data transferred.
    }

    public static MessageConfigRequest decode(FriendlyByteBuf buf)
    {
        // No data transferred.
        return new MessageConfigRequest();
    }

    public static void handle(MessageConfigRequest msg, Supplier<NetworkEvent.Context> ctx)
    {
        ctx.get().enqueueWork(() -> {
            // Send back the config response
            // NetworkHandler.sendToPlayer(ctx.get().getSender(), new MessageConfigResponse());
        });
        ctx.get().setPacketHandled(true);
    }
}
