package goblinbob.mobends.core;

import com.mojang.logging.LogUtils;
import goblinbob.mobends.core.configuration.CoreConfig;
import goblinbob.mobends.core.module.IModule;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Core class for Mo' Bends.
 * Handles module registration and lifecycle for 1.20.1.
 */
public abstract class Core<T extends CoreConfig>
{
    private static Core<?> INSTANCE;
    private static final Logger LOG = LogUtils.getLogger();

    private final Collection<IModule> modules = new ArrayList<>();

    public abstract T getConfiguration();

    /**
     * Called during common setup.
     * Subclasses can override to perform common initialization.
     */
    public void onCommonSetup()
    {
        LOG.info("Mo' Bends Core common setup");
    }

    /**
     * Called during client setup.
     * Subclasses can override to perform client-specific initialization.
     */
    public void onClientSetup()
    {
        LOG.info("Mo' Bends Core client setup");
    }

    /**
     * Apply configuration to entity benders.
     * Called after entity benders are registered.
     */
    public void applyConfigurationToEntityBenders()
    {
        // Default implementation does nothing
        // CoreClient overrides this
    }

    /**
     * Register a module to be managed by the core.
     */
    public void registerModule(IModule module)
    {
        this.modules.add(module);
    }

    /**
     * Initialize all registered modules.
     * Should be called during setup after all modules are registered.
     */
    public void initModules()
    {
        for (IModule module : modules)
        {
            module.init();
        }
    }

    /**
     * Refresh all registered modules.
     */
    public void refreshModules()
    {
        for (IModule module : modules)
        {
            module.onRefresh();
        }
    }

    /**
     * Get the collection of registered modules.
     */
    public Collection<IModule> getModules()
    {
        return modules;
    }

    // Static methods

    public static Core<?> getInstance()
    {
        return INSTANCE;
    }

    public static void createAsClient()
    {
        if (INSTANCE == null)
        {
            INSTANCE = new CoreClient();
        }
    }

    public static void createAsServer()
    {
        if (INSTANCE == null)
        {
            INSTANCE = new CoreServer();
        }
    }

    public static void saveConfiguration()
    {
        if (INSTANCE != null)
        {
            INSTANCE.getConfiguration().save();
        }
    }

    public static Logger getLogger()
    {
        return LOG;
    }
}
