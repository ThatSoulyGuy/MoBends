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
        configuration.initialize();

        initModules();

        PackManager.INSTANCE.initialize(configuration);

    }

    @Override
    public void applyConfigurationToEntityBenders()
    {
        EntityBenderRegistry.instance.applyConfiguration(configuration);
    }

    @Nullable
    public static ForgeCore getInstance()
    {
        return INSTANCE;
    }

    public static void createAsClient()
    {
        new ForgeCore();
    }
}
