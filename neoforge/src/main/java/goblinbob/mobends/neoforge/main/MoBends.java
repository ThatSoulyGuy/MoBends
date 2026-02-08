package goblinbob.mobends.neoforge.main;

import com.mojang.logging.LogUtils;
import goblinbob.mobends.api.platform.PlatformServices;
import goblinbob.mobends.core.Core;
import goblinbob.mobends.neoforge.CoreClient;
import goblinbob.mobends.core.addon.AddonHelper;
import goblinbob.mobends.core.addon.Addons;
import goblinbob.mobends.core.animation.keyframe.AnimationLoader;
import goblinbob.mobends.core.bender.EntityBenderRegistry;
import goblinbob.mobends.neoforge.compat.ModCompatManager;
import goblinbob.mobends.core.data.EntityDatabase;
import goblinbob.mobends.neoforge.client.event.KeyboardEventHandler;
import goblinbob.mobends.neoforge.client.event.RenderingEventHandler;
import goblinbob.mobends.neoforge.network.NetworkHandler;
import goblinbob.mobends.neoforge.platform.NeoForgePlatformServices;
import goblinbob.mobends.core.pack.PackDataProvider;
import goblinbob.mobends.core.util.GsonResources;
import goblinbob.mobends.standard.DefaultAddon;
import goblinbob.mobends.standard.main.ModStatics;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

/**
 * Main entry point for the Mo' Bends mod.
 * Ported to Minecraft 1.20.1 with Forge.
 */
@Mod(MoBends.MODID)
public class MoBends
{
    public static final String MODID = "mobends";

    private static final Logger LOGGER = LogUtils.getLogger();
    public static final Logger LOG = LOGGER;
    public static MoBends instance;

    public MoBends(IEventBus modEventBus, ModContainer container)
    {
        instance = this;

        // Client-side setup
        if (FMLEnvironment.dist == Dist.CLIENT) {
            modEventBus.addListener(this::clientSetup);
            // Register key mappings on mod event bus
            modEventBus.addListener(KeyboardEventHandler::registerKeyMappings);
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
        // Initialize platform services FIRST (before Core)
        PlatformServices.set(new NeoForgePlatformServices());
        LOGGER.info("Mo' Bends platform services initialized: {}", PlatformServices.get().getPlatformName());

        // Initialize the Core for client
        CoreClient.createAsClient();

        // Perform client setup on the Core (registers event handlers)
        Core.getInstance().onClientSetup();

        // Register the default addon (standard animations) - this registers entity benders
        AddonHelper.registerAddon(ModStatics.MODID, new DefaultAddon());

        // Apply configuration AFTER entity benders are registered
        Core.getInstance().applyConfigurationToEntityBenders();

        // Register NeoForge event handlers
        NeoForge.EVENT_BUS.register(new RenderingEventHandler());
        NeoForge.EVENT_BUS.register(new KeyboardEventHandler());
        LOGGER.info("Mo' Bends event handlers registered");

        // Initialize mod compatibility layers (Curios, Better Blood Overlay, PlayerAnimationLib, etc.)
        ModCompatManager.init();

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
