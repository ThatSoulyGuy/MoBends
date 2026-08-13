package goblinbob.mobends.neoforge.main;

import com.mojang.logging.LogUtils;
import goblinbob.mobends.api.player.IPlayerSkinProvider;
import goblinbob.mobends.api.platform.PlatformServices;
import goblinbob.mobends.core.Core;
import goblinbob.mobends.neoforge.CoreClient;
import goblinbob.mobends.core.addon.AddonHelper;
import goblinbob.mobends.core.addon.Addons;
import goblinbob.mobends.core.animation.keyframe.AnimationLoader;
import goblinbob.mobends.core.bender.EntityBenderRegistry;
import goblinbob.mobends.compat.ModCompatManager;
import goblinbob.mobends.neoforge.player.NeoForgePlayerSkinProvider;
import goblinbob.mobends.core.data.EntityDatabase;
import goblinbob.mobends.neoforge.client.event.KeyboardEventHandler;
import goblinbob.mobends.neoforge.client.event.RenderingEventHandler;
import goblinbob.mobends.neoforge.network.NetworkHandler;
import goblinbob.mobends.neoforge.platform.NeoForgePlatformServices;
import goblinbob.mobends.core.pack.PackDataProvider;
import goblinbob.mobends.core.util.GsonResources;
import goblinbob.mobends.neoforge.NeoForgeAddon;
import goblinbob.mobends.standard.main.ModStatics;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

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

        if (FMLEnvironment.dist == Dist.CLIENT) {
            modEventBus.addListener(this::clientSetup);
            modEventBus.addListener(KeyboardEventHandler::registerKeyMappings);
            modEventBus.addListener(goblinbob.mobends.neoforge.client.event.EntityRendererRegistrar::registerRenderers);

            container.registerExtensionPoint(
                    net.neoforged.neoforge.client.gui.IConfigScreenFactory.class,
                    (modContainer, parent) -> goblinbob.mobends.core.client.gui.UIBridge.createConfigScreen());
        }

        modEventBus.addListener(this::commonSetup);

        modEventBus.addListener(NetworkHandler::register);

        container.registerConfig(net.neoforged.fml.config.ModConfig.Type.CLIENT, NeoForgeConfig.SPEC);
        modEventBus.addListener((ModConfigEvent.Loading event) -> onModConfigEvent(event));
        modEventBus.addListener((ModConfigEvent.Reloading event) -> onModConfigEvent(event));

        LOGGER.info("Mo' Bends {} initializing...", ModStatics.VERSION);
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        goblinbob.mobends.core.network.SharedNetworkConfiguration.init();

        LOGGER.info("Mo' Bends common setup complete");
    }

    private void onModConfigEvent(final ModConfigEvent event)
    {
        if (event.getConfig().getSpec() == NeoForgeConfig.SPEC)
        {
            NeoForgeConfig.sync();
        }
    }

    private void clientSetup(final FMLClientSetupEvent event)
    {
        PlatformServices.set(new NeoForgePlatformServices());
        LOGGER.info("Mo' Bends platform services initialized: {}", PlatformServices.get().getPlatformName());

        IPlayerSkinProvider.Holder.setProvider(new NeoForgePlayerSkinProvider());

        CoreClient.createAsClient();

        Core.getInstance().onClientSetup();

        AddonHelper.registerAddon(ModStatics.MODID, new NeoForgeAddon());

        Core.getInstance().applyConfigurationToEntityBenders();

        NeoForge.EVENT_BUS.register(new RenderingEventHandler());
        NeoForge.EVENT_BUS.register(new KeyboardEventHandler());
        NeoForge.EVENT_BUS.register(new goblinbob.mobends.neoforge.network.ConfigSyncClientHandler());
        LOGGER.info("Mo' Bends event handlers registered");

        ModCompatManager.init();

        LOGGER.info("Mo' Bends client setup complete");
    }

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
