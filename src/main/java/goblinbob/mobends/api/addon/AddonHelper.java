package goblinbob.mobends.api.addon;

public class AddonHelper
{

	public static void registerAddon(String modId, IAddon addon)
	{
		Addons.registerAddon(modId, addon);
	}

}
