package goblinbob.mobends.core.env;

import goblinbob.mobends.core.module.IModule;

public class EnvironmentModule implements IModule
{
    public static EnvironmentModule INSTANCE;

    @Override
    public void init()
    {
        INSTANCE = this;
    }

    @Override
    public void onRefresh()
    {
    }

    public static class Factory implements IModule.Factory
    {
        @Override
        public IModule create()
        {
            return new EnvironmentModule();
        }
    }
}
