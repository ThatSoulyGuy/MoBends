package goblinbob.mobends.neoforge.network;

import goblinbob.mobends.neoforge.network.msg.ConfigRequestPayload;
import goblinbob.mobends.neoforge.network.msg.ConfigResponsePayload;
import goblinbob.mobends.standard.main.ModStatics;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * Network handler for Mo' Bends.
 * Handles packet registration for NeoForge 1.21.
 *
 * Packets are registered as optional, allowing the mod to work on vanilla servers
 * that don't have Mo' Bends installed.
 */
public class NetworkHandler {

    /**
     * Register all network packets as optional.
     * This method is called automatically when the RegisterPayloadHandlersEvent is fired.
     *
     * Using optional() means the connection will work even if the server doesn't have
     * this mod installed - packets simply won't be sent/received in that case.
     */
    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        // Use optional() to allow connections to vanilla servers
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
