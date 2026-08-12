package goblinbob.mobends.neoforge.network;

import goblinbob.mobends.neoforge.network.msg.ConfigRequestPayload;
import goblinbob.mobends.neoforge.network.msg.ConfigResponsePayload;
import goblinbob.mobends.standard.main.ModStatics;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class NetworkHandler {

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(ModStatics.MODID).optional();

        registrar.playToServer(
            ConfigRequestPayload.TYPE,
            ConfigRequestPayload.STREAM_CODEC,
            ConfigRequestPayload::handle
        );

        registrar.playToClient(
            ConfigResponsePayload.TYPE,
            ConfigResponsePayload.STREAM_CODEC,
            ConfigResponsePayload::handle
        );
    }
}
