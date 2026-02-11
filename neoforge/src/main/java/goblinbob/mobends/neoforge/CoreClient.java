package goblinbob.mobends.neoforge;

import com.mojang.logging.LogUtils;
import goblinbob.mobends.core.Core;
import goblinbob.mobends.core.asset.AssetsModule;
import goblinbob.mobends.core.bender.EntityBenderRegistry;
import goblinbob.mobends.core.client.event.*;
import goblinbob.mobends.core.configuration.CoreClientConfig;
import goblinbob.mobends.core.env.EnvironmentModule;
import goblinbob.mobends.core.pack.PackManager;
import goblinbob.mobends.neoforge.configuration.NeoForgeClientConfig;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

import javax.annotation.Nullable;

/**
 * Client-side Core implementation for Mo' Bends NeoForge.
 */
@OnlyIn(Dist.CLIENT)
public class CoreClient extends Core
{
    private static final Logger LOGGER = LogUtils.getLogger();
    private static CoreClient INSTANCE;

    private CoreClientConfig configuration;

    CoreClient()
    {
        INSTANCE = this;
        Core.instance = this;
        this.configuration = CoreClientConfig.getInstance();

        // Initialize modules directly
        modules.add(new EnvironmentModule());
        modules.add(new AssetsModule());
    }

    public CoreClientConfig getConfiguration()
    {
        return configuration;
    }

    @Override
    public void onClientSetup()
    {
        // Initialize all registered modules
        initModules();

        // Initialize configuration (requires Minecraft to be initialized)
        configuration.initialize();

        // Initialize pack manager
        PackManager.INSTANCE.initialize(configuration);

        // Note: Event handlers are stubs in common-mc and don't have @SubscribeEvent methods
        // Platform-specific event handling would need to be implemented in neoforge module
        // with proper NeoForge annotations

        LOGGER.info("Mo' Bends client core initialized");
    }

    @Override
    public void applyConfigurationToEntityBenders()
    {
        LOGGER.info("Applying configuration to entity benders");
        EntityBenderRegistry.instance.applyConfiguration(configuration);
    }

    @Nullable
    public static CoreClient getInstance()
    {
        return INSTANCE;
    }

    /**
     * Creates the client core instance.
     */
    public static void createAsClient()
    {
        new CoreClient();
    }
}
