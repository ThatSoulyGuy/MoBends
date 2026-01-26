package goblinbob.mobends.core;

import goblinbob.mobends.core.module.IModule;

import java.util.ArrayList;
import java.util.List;

/**
 * Core class providing base functionality for MoBends.
 * Platform-specific implementations extend this class.
 */
public class Core
{
    protected static Core instance;
    protected final List<IModule> modules = new ArrayList<>();

    /**
     * Gets the singleton instance.
     * @return The Core instance
     */
    public static Core getInstance()
    {
        return instance;
    }

    /**
     * Creates the core as a client-side implementation.
     * Platform implementations should override this in their Core subclass.
     */
    public static void createAsClient()
    {
        // Platform implementation creates the appropriate CoreClient subclass
    }

    /**
     * Creates the core as a server-side implementation.
     * Platform implementations should override this in their Core subclass.
     */
    public static void createAsServer()
    {
        // Platform implementation creates the appropriate CoreServer subclass
    }

    /**
     * Called during client setup.
     */
    public void onClientSetup()
    {
        // Override in platform-specific implementation
    }

    /**
     * Called during server setup.
     */
    public void onServerSetup()
    {
        // Override in platform-specific implementation
    }

    /**
     * Applies configuration to entity benders.
     */
    public void applyConfigurationToEntityBenders()
    {
        // Override in platform-specific implementation
    }

    /**
     * Registers a module factory.
     * @param factory The module factory
     */
    public void registerModule(IModule.Factory factory)
    {
        modules.add(factory.create());
    }

    /**
     * Initializes all registered modules.
     */
    protected void initModules()
    {
        for (IModule module : modules)
        {
            module.init();
        }
    }

    /**
     * Refreshes all registered modules.
     */
    public void refreshModules()
    {
        for (IModule module : modules)
        {
            module.refresh();
        }
    }

    /**
     * Saves the mod configuration.
     * Platform implementations should override this behavior.
     */
    public static void saveConfiguration()
    {
        // No-op stub - platform implementations will override
    }
}
