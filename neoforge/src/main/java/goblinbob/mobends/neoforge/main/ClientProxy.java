package goblinbob.mobends.neoforge.main;

import goblinbob.mobends.neoforge.CoreClient;
import goblinbob.mobends.core.addon.AddonHelper;
import goblinbob.mobends.standard.DefaultAddon;
import goblinbob.mobends.neoforge.client.event.RenderingEventHandler;
import goblinbob.mobends.standard.main.ModStatics;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Client-side proxy for Mo' Bends.
 * Updated for Minecraft 1.20.1 - entity rendering registration is now done
 * through the FMLClientSetupEvent in the main mod class.
 */
public class ClientProxy extends CommonProxy
{

	@Override
	public void preInit()
	{
		// Entity rendering registration is now done through EntityRenderers.register()
		// in the main mod class during FMLClientSetupEvent
	}

	@Override
	public void init()
	{
		NeoForge.EVENT_BUS.register(new RenderingEventHandler());

		// Registering the standard set of animations.
		AddonHelper.registerAddon(ModStatics.MODID, new DefaultAddon());
	}

	@Override
	public void postInit() {}

	@Override
	public void createCore()
	{
		CoreClient.createAsClient();
	}

}
