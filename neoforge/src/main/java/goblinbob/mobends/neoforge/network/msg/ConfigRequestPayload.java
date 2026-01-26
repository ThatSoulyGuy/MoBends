package goblinbob.mobends.neoforge.network.msg;

import goblinbob.mobends.standard.main.ModStatics;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * This packet is sent by the client to the server to request
 * the server specific configuration.
 */
public record ConfigRequestPayload() implements CustomPacketPayload {

    public static final Type<ConfigRequestPayload> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(ModStatics.MODID, "config_request"));

    public static final StreamCodec<FriendlyByteBuf, ConfigRequestPayload> STREAM_CODEC =
        StreamCodec.unit(new ConfigRequestPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * Handle the packet on the receiving side (server).
     */
    public static void handle(ConfigRequestPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer sender) {
                // Send back the config response
                PacketDistributor.sendToPlayer(sender, new ConfigResponsePayload());
            }
        });
    }
}
