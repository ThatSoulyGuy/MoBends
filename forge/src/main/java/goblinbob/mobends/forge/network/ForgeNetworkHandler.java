package goblinbob.mobends.forge.network;

import goblinbob.mobends.forge.MoBendsForge;
import goblinbob.mobends.forge.network.msg.MessageConfigRequest;
import goblinbob.mobends.forge.network.msg.MessageConfigResponse;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;

public class ForgeNetworkHandler {
    private static final String PROTOCOL_VERSION = "1";

    private static SimpleChannel CHANNEL;

    public static void register() {

        CHANNEL = NetworkRegistry.newSimpleChannel(
                new ResourceLocation(MoBendsForge.MOD_ID, "main"),
                () -> PROTOCOL_VERSION,
                s -> true,
                s -> true
        );

        int id = 0;

        CHANNEL.registerMessage(id++,
                MessageConfigRequest.class,
                MessageConfigRequest::encode,
                MessageConfigRequest::decode,
                MessageConfigRequest::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));

        CHANNEL.registerMessage(id++,
                MessageConfigResponse.class,
                MessageConfigResponse::encode,
                MessageConfigResponse::decode,
                MessageConfigResponse::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));

    }

    public static SimpleChannel getChannel() {
        return CHANNEL;
    }
}
