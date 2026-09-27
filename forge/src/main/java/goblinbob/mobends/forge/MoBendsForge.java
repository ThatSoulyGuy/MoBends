package goblinbob.mobends.forge;

import goblinbob.mobends.api.player.IPlayerSkinProvider;
import goblinbob.mobends.api.platform.PlatformServices;
import goblinbob.mobends.core.Core;
import goblinbob.mobends.api.addon.AddonHelper;
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

@Mod(MoBendsForge.MOD_ID)
public class MoBendsForge
{
    public static final String MOD_ID = "mobends";

    public MoBendsForge()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::commonSetup);

        ModLoadingContext.get().registerConfig(net.minecraftforge.fml.config.ModConfig.Type.CLIENT, ForgeConfig.SPEC);
        ModLoadingContext.get().registerConfig(net.minecraftforge.fml.config.ModConfig.Type.SERVER, ForgeServerConfig.SPEC);
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
        ForgeNetworkHandler.register();
    }

    private void onModConfigEvent(final ModConfigEvent event)
    {
        if (event.getConfig().getSpec() == ForgeConfig.SPEC)
        {
            ForgeConfig.sync();
        }
        else if (event.getConfig().getSpec() == ForgeServerConfig.SPEC)
        {
            ForgeServerConfig.sync();
        }
    }

    private void clientSetup(final FMLClientSetupEvent event)
    {
        PlatformServices.set(new ForgePlatformServices());

        IPlayerSkinProvider.Holder.setProvider(new ForgePlayerSkinProvider());

        ForgeCore.createAsClient();

        Core.getInstance().onClientSetup();

        goblinbob.mobends.api.addon.Addons.flushPending();

        AddonHelper.registerAddon(ModStatics.MODID, new DefaultAddon());

        Core.getInstance().applyConfigurationToEntityBenders();

        MinecraftForge.EVENT_BUS.register(new RenderingEventHandler());
        MinecraftForge.EVENT_BUS.register(new KeyboardEventHandler());
        MinecraftForge.EVENT_BUS.register(new goblinbob.mobends.forge.network.ConfigSyncClientHandler());

        ModCompatManager.init();

        goblinbob.mobends.forge.compat.LegendsEventBridge.register();
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
        ForgeServerConfig.sync();
    }
}
