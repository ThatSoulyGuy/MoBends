package goblinbob.mobends.forge.network.msg;

import goblinbob.mobends.forge.network.ForgeNetworkHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

public class MessageConfigRequest
{

    public MessageConfigRequest()
    {
    }

    public static void encode(MessageConfigRequest message, FriendlyByteBuf buf)
    {
    }

    public static MessageConfigRequest decode(FriendlyByteBuf buf)
    {
        return new MessageConfigRequest();
    }

    public static void handle(MessageConfigRequest message, Supplier<NetworkEvent.Context> contextSupplier)
    {
        final NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() -> {
            final ServerPlayer sender = context.getSender();
            if (sender == null)
            {
                return;
            }

            ForgeNetworkHandler.getChannel().send(
                    PacketDistributor.PLAYER.with(() -> sender),
                    new MessageConfigResponse());
        });

        context.setPacketHandled(true);
    }

}
