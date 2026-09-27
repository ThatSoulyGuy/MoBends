package goblinbob.mobends.neoforge;

import goblinbob.mobends.core.Core;
import goblinbob.mobends.core.asset.AssetsModule;
import goblinbob.mobends.core.bender.EntityBenderRegistry;
import goblinbob.mobends.core.configuration.CoreClientConfig;
import goblinbob.mobends.core.env.EnvironmentModule;
import goblinbob.mobends.core.pack.PackManager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CoreClient extends Core
{
    private CoreClientConfig configuration;

    CoreClient()
    {
        Core.instance = this;
        this.configuration = CoreClientConfig.getInstance();

        modules.add(new EnvironmentModule());
        modules.add(new AssetsModule());
    }

    @Override
    public void onClientSetup()
    {
        initModules();

        configuration.initialize();

        PackManager.INSTANCE.initialize(configuration);

    }

    @Override
    public void applyConfigurationToEntityBenders()
    {
        EntityBenderRegistry.instance.applyConfiguration(configuration);
    }

    public static void createAsClient()
    {
        new CoreClient();
    }
}
