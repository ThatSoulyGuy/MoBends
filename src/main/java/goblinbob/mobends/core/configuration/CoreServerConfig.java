package goblinbob.mobends.core.configuration;

import com.mojang.logging.LogUtils;
import goblinbob.mobends.core.network.NetworkConfiguration;
import goblinbob.mobends.core.network.SharedProperty;
import goblinbob.mobends.standard.main.ModStatics;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import org.slf4j.Logger;

/**
 * Server-side configuration for Mo' Bends 1.20.1.
 */
public class CoreServerConfig extends CoreConfig
{
    private static final Logger LOGGER = LogUtils.getLogger();

    private static ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static ForgeConfigSpec SPEC;

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
     * Register this config with Forge.
     * Call during mod construction.
     */
    public static void register()
    {
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, SPEC, ModStatics.MODID + "-server.toml");
    }
}
