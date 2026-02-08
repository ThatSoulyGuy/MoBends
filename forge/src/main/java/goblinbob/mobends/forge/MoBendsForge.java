package goblinbob.mobends.forge;

import com.mojang.logging.LogUtils;
import goblinbob.mobends.api.platform.PlatformServices;
import goblinbob.mobends.core.Core;
import goblinbob.mobends.core.addon.AddonHelper;
import goblinbob.mobends.core.addon.Addons;
import goblinbob.mobends.core.animation.keyframe.AnimationLoader;
import goblinbob.mobends.core.bender.EntityBenderRegistry;
import goblinbob.mobends.core.data.EntityDatabase;
import goblinbob.mobends.core.pack.PackDataProvider;
import goblinbob.mobends.core.util.GsonResources;
import goblinbob.mobends.forge.client.event.KeyboardEventHandler;
import goblinbob.mobends.forge.client.event.RenderingEventHandler;
import goblinbob.mobends.forge.compat.ModCompatManager;
import goblinbob.mobends.forge.network.ForgeNetworkHandler;
import goblinbob.mobends.forge.platform.ForgePlatformServices;
import goblinbob.mobends.standard.DefaultAddon;
import goblinbob.mobends.standard.main.ModStatics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.slf4j.Logger;

/**
 * Mo' Bends Forge entry point for Minecraft 1.20.1
 */
@Mod(MoBendsForge.MOD_ID)
public class MoBendsForge
{
    public static final String MOD_ID = "mobends";

    private static final Logger LOGGER = LogUtils.getLogger();
    public static final Logger LOG = LOGGER;
    public static MoBendsForge instance;

    public MoBendsForge()
    {
        instance = this;

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register lifecycle event listeners
        modEventBus.addListener(this::commonSetup);

        if (FMLEnvironment.dist == Dist.CLIENT)
        {
            modEventBus.addListener(this::clientSetup);
            // Register key mappings on mod event bus
            modEventBus.addListener(KeyboardEventHandler::registerKeyMappings);
        }

        // Register ourselves for server and other game events
        MinecraftForge.EVENT_BUS.register(this);

        LOGGER.info("Mo' Bends {} initializing...", ModStatics.VERSION);
    }

    /**
     * Common setup - runs on both client and server.
     */
    private void commonSetup(final FMLCommonSetupEvent event)
    {
        LOGGER.info("Mo' Bends common setup");

        // Register network handler
        ForgeNetworkHandler.register();
    }

    /**
     * Client-specific setup.
     */
    private void clientSetup(final FMLClientSetupEvent event)
    {
        // Initialize platform services FIRST (before Core)
        PlatformServices.set(new ForgePlatformServices());
        LOGGER.info("Mo' Bends platform services initialized: {}", PlatformServices.get().getPlatformName());

        // Initialize the Core for client
        ForgeCore.createAsClient();

        // Perform client setup on the Core (registers event handlers)
        Core.getInstance().onClientSetup();

        // Register the default addon (standard animations) - this registers entity benders
        AddonHelper.registerAddon(ModStatics.MODID, new DefaultAddon());

        // Apply configuration AFTER entity benders are registered
        Core.getInstance().applyConfigurationToEntityBenders();

        // Register Forge event handlers
        MinecraftForge.EVENT_BUS.register(new RenderingEventHandler());
        MinecraftForge.EVENT_BUS.register(new KeyboardEventHandler());
        LOGGER.info("Mo' Bends event handlers registered");

        // Initialize mod compatibility layers (Curios, Better Blood Overlay, PlayerAnimationLib, etc.)
        ModCompatManager.init();

        LOGGER.info("Mo' Bends client setup complete");
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
        LOGGER.info("Mo' Bends: Server starting");
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
