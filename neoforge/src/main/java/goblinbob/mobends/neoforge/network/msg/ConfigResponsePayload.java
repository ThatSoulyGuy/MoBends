package goblinbob.mobends.neoforge.network.msg;

import com.mojang.logging.LogUtils;
import goblinbob.mobends.core.network.SharedNetworkConfiguration;
import goblinbob.mobends.core.network.SharedProperty;
import goblinbob.mobends.standard.main.ModStatics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.slf4j.Logger;

public record ConfigResponsePayload(CompoundTag configData) implements CustomPacketPayload {

    private static final Logger LOGGER = LogUtils.getLogger();

    public static final Type<ConfigResponsePayload> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(ModStatics.MODID, "config_response"));

    public static final StreamCodec<FriendlyByteBuf, ConfigResponsePayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.COMPOUND_TAG,
        ConfigResponsePayload::configData,
        ConfigResponsePayload::new
    );

    public ConfigResponsePayload() {
        this(createConfigData());
    }

    private static CompoundTag createConfigData() {
        CompoundTag data = new CompoundTag();
        SharedNetworkConfiguration.INSTANCE.getSharedConfig().writeToNBT(data);
        return data;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ConfigResponsePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (payload.configData == null) {
                LOGGER.error("An error occurred while receiving server configuration.");
                return;
            }

            SharedNetworkConfiguration.INSTANCE.getSharedConfig().readFromNBT(payload.configData);

            final StringBuilder builder = new StringBuilder("Received Mo' Bends server configuration.\n");
            final Iterable<SharedProperty<?>> properties = SharedNetworkConfiguration.INSTANCE.getSharedConfig().getProperties();
            for (SharedProperty<?> property : properties) {
                builder.append(String.format(" - %s: %s\n", property.getKey(), property.getValue()));
            }
            LOGGER.info(builder.toString());
        });
    }
}
