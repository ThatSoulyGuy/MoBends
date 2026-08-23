package goblinbob.mobends.api.addon;

public interface IAddon
{

	void registerContent(AddonAnimationRegistry registry);

	String getDisplayName();

	default void onRenderTick(float partialTicks)
	{
	}

	default void onClientTick()
	{
	}

	default void onRefresh()
	{
	}

}
