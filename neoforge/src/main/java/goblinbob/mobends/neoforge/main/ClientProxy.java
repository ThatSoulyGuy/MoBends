package goblinbob.mobends.neoforge.main;

import goblinbob.mobends.neoforge.CoreClient;
import goblinbob.mobends.core.addon.AddonHelper;
import goblinbob.mobends.standard.DefaultAddon;
import goblinbob.mobends.neoforge.client.event.RenderingEventHandler;
import goblinbob.mobends.standard.main.ModStatics;
import net.neoforged.neoforge.common.NeoForge;

public class ClientProxy extends CommonProxy
{

	@Override
	public void preInit()
	{
	}

	@Override
	public void init()
	{
		NeoForge.EVENT_BUS.register(new RenderingEventHandler());

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
