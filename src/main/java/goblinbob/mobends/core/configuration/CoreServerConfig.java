package goblinbob.mobends.core.configuration;

import com.mojang.logging.LogUtils;
import goblinbob.mobends.core.network.NetworkConfiguration;
import goblinbob.mobends.core.network.SharedProperty;
import goblinbob.mobends.standard.main.ModStatics;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.slf4j.Logger;

/**
 * Server-side configuration for Mo' Bends 1.20.1.
 */
public class CoreServerConfig extends CoreConfig
{
    private static final Logger LOGGER = LogUtils.getLogger();

    private static ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static ModConfigSpec SPEC;

    static
    {
        BUILDER.comment("Mo' Bends Server Configuration").push("server");

        // Add server config options here if needed
        // The shared properties from NetworkConfiguration can be synced separately

        BUILDER.pop();
        SPEC = BUILDER.build();
    }

    public CoreServerConfig()
    {
        load();
    }

    @Override
    public void save()
    {
        LOGGER.debug("Saving Mo' Bends server configuration");
    }

    @Override
    public void load()
    {
        LOGGER.debug("Loading Mo' Bends server configuration");

        // Initialize shared properties from network configuration
        final Iterable<SharedProperty<?>> props = NetworkConfiguration.instance.getSharedConfig().getProperties();
        for (SharedProperty<?> prop : props)
        {
            // Properties will use their default values initially
            LOGGER.debug("Initialized shared property: {}", prop.getKey());
        }
    }

    /**
     * Register this config with NeoForge.
     * Call during mod construction.
     */
    public static void register(ModContainer container)
    {
        container.registerConfig(ModConfig.Type.SERVER, SPEC, ModStatics.MODID + "-server.toml");
    }
}
