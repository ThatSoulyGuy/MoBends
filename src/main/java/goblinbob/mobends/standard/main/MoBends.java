package goblinbob.mobends.standard.main;

import com.mojang.logging.LogUtils;
import goblinbob.mobends.core.Core;
import goblinbob.mobends.core.addon.AddonHelper;
import goblinbob.mobends.core.addon.Addons;
import goblinbob.mobends.core.animation.keyframe.AnimationLoader;
import goblinbob.mobends.core.bender.EntityBenderRegistry;
import goblinbob.mobends.core.client.event.KeyboardHandler;
import goblinbob.mobends.core.compat.PlayerAnimationLibCompat;
import goblinbob.mobends.core.configuration.CoreClientConfig;
import goblinbob.mobends.core.configuration.CoreServerConfig;
import goblinbob.mobends.core.data.EntityDatabase;
import goblinbob.mobends.core.network.NetworkHandler;
import goblinbob.mobends.core.pack.PackDataProvider;
import goblinbob.mobends.core.util.GsonResources;
import goblinbob.mobends.standard.DefaultAddon;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import org.slf4j.Logger;

/**
 * Main entry point for the Mo' Bends mod.
 * Ported to Minecraft 1.20.1 with Forge.
 */
@Mod(ModStatics.MODID)
public class MoBends
{
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final Logger LOG = LOGGER;
    public static MoBends instance;

    public MoBends(IEventBus modEventBus, ModContainer container)
    {
        instance = this;

        // Register configurations
        CoreServerConfig.register(container);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            CoreClientConfig.register(container);
            modEventBus.addListener(this::clientSetup);
            modEventBus.addListener(KeyboardHandler::registerKeyMappings);
        }

        // Register lifecycle event listeners
        modEventBus.addListener(this::commonSetup);

        // Register network handler
        modEventBus.addListener(NetworkHandler::register);

        LOGGER.info("Mo' Bends {} initializing...", ModStatics.VERSION);
    }

    /**
     * Common setup - runs on both client and server.
     */
    private void commonSetup(final FMLCommonSetupEvent event)
    {
        LOGGER.info("Mo' Bends common setup complete");
    }

    /**
     * Client-specific setup.
     */
    private void clientSetup(final FMLClientSetupEvent event)
    {
        // Initialize the Core for client
        Core.createAsClient();

        // Perform client setup on the Core (registers event handlers)
        Core.getInstance().onClientSetup();

        // Register the default addon (standard animations) - this registers entity benders
        AddonHelper.registerAddon(ModStatics.MODID, new DefaultAddon());

        // Apply configuration AFTER entity benders are registered
        Core.getInstance().applyConfigurationToEntityBenders();

        // Initialize mod compatibility layers
        PlayerAnimationLibCompat.init();

        LOGGER.info("Mo' Bends client setup complete");
    }

    /**
     * Used to refresh all systems, clear caches. Usually performed when configuration changes.
     */
    public static void refreshSystems()
    {
        AnimationLoader.clearCache();
        GsonResources.clearCache();
        PackDataProvider.INSTANCE.clearCache();
        EntityDatabase.instance.refresh();
        EntityBenderRegistry.instance.refreshMutators();
        Addons.onRefresh();

        Core.getInstance().refreshModules();
    }
}
