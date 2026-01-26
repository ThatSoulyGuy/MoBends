package goblinbob.mobends.forge.network;

import com.mojang.logging.LogUtils;
import goblinbob.mobends.forge.MoBendsForge;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import org.slf4j.Logger;

/**
 * Network handler for Mo' Bends Forge.
 *
 * Note: This is a minimal implementation. Full network sync requires
 * porting the common-mc network code to Forge 1.20.1 APIs.
 */
public class ForgeNetworkHandler {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String PROTOCOL_VERSION = "1";

    private static SimpleChannel CHANNEL;

    /**
     * Registers the network channel and messages.
     */
    public static void register() {
        LOGGER.info("Registering Mo' Bends network channel");

        CHANNEL = NetworkRegistry.newSimpleChannel(
                new ResourceLocation(MoBendsForge.MOD_ID, "main"),
                () -> PROTOCOL_VERSION,
                PROTOCOL_VERSION::equals,
                PROTOCOL_VERSION::equals
        );

        // Register messages here when implementing full network sync
        // Example:
        // CHANNEL.registerMessage(0,
        //     ConfigRequestMessage.class,
        //     ConfigRequestMessage::encode,
        //     ConfigRequestMessage::decode,
        //     ConfigRequestMessage::handle);

        LOGGER.info("Mo' Bends network channel registered");
    }

    /**
     * Gets the network channel.
     */
    public static SimpleChannel getChannel() {
        return CHANNEL;
    }
}
