package goblinbob.mobends.core;

import goblinbob.mobends.core.module.IModule;

import java.util.ArrayList;
import java.util.List;

public class Core
{
    protected static Core instance;
    protected final List<IModule> modules = new ArrayList<>();

    public static Core getInstance()
    {
        return instance;
    }

    public static void createAsClient()
    {
    }

    public static void createAsServer()
    {
    }

    public void onClientSetup()
    {
    }

    public void onServerSetup()
    {
    }

    public void applyConfigurationToEntityBenders()
    {
    }

    public void registerModule(IModule.Factory factory)
    {
        modules.add(factory.create());
    }

    protected void initModules()
    {
        for (IModule module : modules)
        {
            module.init();
        }
    }

    public void refreshModules()
    {
        for (IModule module : modules)
        {
            module.refresh();
        }
    }

    public static void saveConfiguration()
    {
    }
}
