package goblinbob.mobends.forge.network;

import com.mojang.logging.LogUtils;
import goblinbob.mobends.forge.MoBendsForge;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import org.slf4j.Logger;

public class ForgeNetworkHandler {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String PROTOCOL_VERSION = "1";

    private static SimpleChannel CHANNEL;

    public static void register() {
        LOGGER.info("Registering Mo' Bends network channel");

        CHANNEL = NetworkRegistry.newSimpleChannel(
                new ResourceLocation(MoBendsForge.MOD_ID, "main"),
                () -> PROTOCOL_VERSION,
                s -> true,
                s -> true
        );

        LOGGER.info("Mo' Bends network channel registered");
    }

    public static SimpleChannel getChannel() {
        return CHANNEL;
    }
}
