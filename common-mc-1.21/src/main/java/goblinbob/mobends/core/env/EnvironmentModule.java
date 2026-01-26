package goblinbob.mobends.core.env;

import goblinbob.mobends.core.module.IModule;

/**
 * Environment configuration module.
 * Handles environment-specific settings and detection.
 */
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
        // No-op
    }

    /**
     * Factory for creating EnvironmentModule instances.
     */
    public static class Factory implements IModule.Factory
    {
        @Override
        public IModule create()
        {
            return new EnvironmentModule();
        }
    }
}
