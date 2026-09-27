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

    public void onClientSetup()
    {
    }

    public void applyConfigurationToEntityBenders()
    {
    }

    protected void initModules()
    {
        for (IModule module : modules)
        {
            module.init();
        }
    }
}
