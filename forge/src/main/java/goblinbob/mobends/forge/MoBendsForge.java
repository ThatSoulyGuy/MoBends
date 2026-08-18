package goblinbob.mobends.forge;

import com.mojang.logging.LogUtils;
import goblinbob.mobends.api.player.IPlayerSkinProvider;
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
import goblinbob.mobends.compat.ModCompatManager;
import goblinbob.mobends.forge.network.ForgeNetworkHandler;
import goblinbob.mobends.forge.player.ForgePlayerSkinProvider;
import goblinbob.mobends.forge.platform.ForgePlatformServices;
import goblinbob.mobends.standard.DefaultAddon;
import goblinbob.mobends.standard.main.ModStatics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.slf4j.Logger;

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

        modEventBus.addListener(this::commonSetup);

        ModLoadingContext.get().registerConfig(net.minecraftforge.fml.config.ModConfig.Type.CLIENT, ForgeConfig.SPEC);
        modEventBus.addListener((ModConfigEvent.Loading event) -> onModConfigEvent(event));
        modEventBus.addListener((ModConfigEvent.Reloading event) -> onModConfigEvent(event));

        if (FMLEnvironment.dist == Dist.CLIENT)
        {
            ModLoadingContext.get().registerExtensionPoint(
                    net.minecraftforge.client.ConfigScreenHandler.ConfigScreenFactory.class,
                    () -> new net.minecraftforge.client.ConfigScreenHandler.ConfigScreenFactory(
                            (minecraft, parent) -> goblinbob.mobends.core.client.gui.UIBridge.createConfigScreen()));

            modEventBus.addListener(this::clientSetup);
            modEventBus.addListener(KeyboardEventHandler::registerKeyMappings);
            modEventBus.addListener(goblinbob.mobends.forge.client.event.EntityRendererRegistrar::registerRenderers);
        }

        MinecraftForge.EVENT_BUS.register(this);

    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {

        goblinbob.mobends.core.network.SharedNetworkConfiguration.init();

        ForgeNetworkHandler.register();
    }

    private void onModConfigEvent(final ModConfigEvent event)
    {
        if (event.getConfig().getSpec() == ForgeConfig.SPEC)
        {
            ForgeConfig.sync();
        }
    }

    private void clientSetup(final FMLClientSetupEvent event)
    {
        PlatformServices.set(new ForgePlatformServices());

        IPlayerSkinProvider.Holder.setProvider(new ForgePlayerSkinProvider());

        ForgeCore.createAsClient();

        Core.getInstance().onClientSetup();

        AddonHelper.registerAddon(ModStatics.MODID, new DefaultAddon());

        Core.getInstance().applyConfigurationToEntityBenders();

        MinecraftForge.EVENT_BUS.register(new RenderingEventHandler());
        MinecraftForge.EVENT_BUS.register(new KeyboardEventHandler());
        MinecraftForge.EVENT_BUS.register(new goblinbob.mobends.forge.network.ConfigSyncClientHandler());

        ModCompatManager.init();

    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
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
