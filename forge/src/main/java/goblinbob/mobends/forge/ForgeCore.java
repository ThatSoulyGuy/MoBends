package goblinbob.mobends.forge;

import com.mojang.logging.LogUtils;
import goblinbob.mobends.core.Core;
import goblinbob.mobends.core.asset.AssetsModule;
import goblinbob.mobends.core.bender.EntityBenderRegistry;
import goblinbob.mobends.core.configuration.CoreClientConfig;
import goblinbob.mobends.core.env.EnvironmentModule;
import goblinbob.mobends.core.pack.PackManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

import javax.annotation.Nullable;

/**
 * Client-side Core implementation for Mo' Bends Forge.
 */
@OnlyIn(Dist.CLIENT)
public class ForgeCore extends Core
{
    private static final Logger LOGGER = LogUtils.getLogger();
    private static ForgeCore INSTANCE;

    private CoreClientConfig configuration;

    ForgeCore()
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

        // Initialize pack manager
        PackManager.INSTANCE.initialize(configuration);

        LOGGER.info("Mo' Bends Forge core initialized");
    }

    @Override
    public void applyConfigurationToEntityBenders()
    {
        LOGGER.info("Applying configuration to entity benders");
        EntityBenderRegistry.instance.applyConfiguration(configuration);
    }

    @Nullable
    public static ForgeCore getInstance()
    {
        return INSTANCE;
    }

    /**
     * Creates the client core instance.
     */
    public static void createAsClient()
    {
        new ForgeCore();
    }
}
