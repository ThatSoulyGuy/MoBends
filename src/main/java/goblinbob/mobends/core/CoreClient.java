package goblinbob.mobends.core;

import com.mojang.logging.LogUtils;
import goblinbob.mobends.core.asset.AssetsModule;
import goblinbob.mobends.core.bender.EntityBenderRegistry;
import goblinbob.mobends.core.client.event.*;
import goblinbob.mobends.core.configuration.CoreClientConfig;
import goblinbob.mobends.core.connection.ConnectionManager;
import goblinbob.mobends.core.env.EnvironmentModule;
import goblinbob.mobends.core.pack.PackManager;
import goblinbob.mobends.core.supporters.SupporterContent;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import org.slf4j.Logger;

import javax.annotation.Nullable;

/**
 * Client-side Core implementation for Mo' Bends 1.20.1.
 */
@OnlyIn(Dist.CLIENT)
public class CoreClient extends Core<CoreClientConfig>
{
    private static final Logger LOGGER = LogUtils.getLogger();
    private static CoreClient INSTANCE;

    private CoreClientConfig configuration;

    CoreClient()
    {
        INSTANCE = this;
        this.configuration = new CoreClientConfig();

        // Register modules
        registerModule(new EnvironmentModule.Factory());
        registerModule(new ConnectionManager.Factory());
        registerModule(new AssetsModule.Factory());
        registerModule(new SupporterContent.Factory());
    }

    @Override
    public CoreClientConfig getConfiguration()
    {
        return configuration;
    }

    @Override
    public void onClientSetup()
    {
        super.onClientSetup();

        // Initialize all registered modules
        initModules();

        // Initialize pack manager
        PackManager.INSTANCE.initialize(configuration);

        // Note: Key bindings are registered via RegisterKeyMappingsEvent in MoBends

        // Register event handlers
        MinecraftForge.EVENT_BUS.register(new EntityRenderHandler());
        MinecraftForge.EVENT_BUS.register(new DataUpdateHandler());
        MinecraftForge.EVENT_BUS.register(new KeyboardHandler());
        MinecraftForge.EVENT_BUS.register(new FluxHandler());
        MinecraftForge.EVENT_BUS.register(new WorldJoinHandler());

        // Note: Entity bender configuration is applied later in MoBends.clientSetup()
        // after entity benders are registered by addons

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
}
