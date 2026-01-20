package goblinbob.mobends.standard.main;

import com.mojang.logging.LogUtils;
import goblinbob.mobends.core.Core;
import goblinbob.mobends.core.addon.AddonHelper;
import goblinbob.mobends.core.addon.Addons;
import goblinbob.mobends.core.animation.keyframe.AnimationLoader;
import goblinbob.mobends.core.bender.EntityBenderRegistry;
import goblinbob.mobends.core.client.event.KeyboardHandler;
import goblinbob.mobends.core.configuration.CoreClientConfig;
import goblinbob.mobends.core.configuration.CoreServerConfig;
import goblinbob.mobends.core.data.EntityDatabase;
import goblinbob.mobends.core.network.NetworkHandler;
import goblinbob.mobends.core.pack.PackDataProvider;
import goblinbob.mobends.core.util.GsonResources;
import goblinbob.mobends.standard.DefaultAddon;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
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

    public MoBends()
    {
        instance = this;

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register configurations
        CoreServerConfig.register();
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> CoreClientConfig::register);

        // Register lifecycle event listeners
        modEventBus.addListener(this::commonSetup);

        // Client-specific setup
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            modEventBus.addListener(this::clientSetup);
            modEventBus.addListener(KeyboardHandler::registerKeyMappings);
        });

        // Register ourselves for server and other game events
        MinecraftForge.EVENT_BUS.register(this);

        LOGGER.info("Mo' Bends {} initializing...", ModStatics.VERSION);
    }

    /**
     * Common setup - runs on both client and server.
     */
    private void commonSetup(final FMLCommonSetupEvent event)
    {
        event.enqueueWork(() -> {
            // Register network messages
            NetworkHandler.register();
            LOGGER.info("Mo' Bends network handler registered");
        });
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
