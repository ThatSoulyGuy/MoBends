package goblinbob.mobends.api.addon;

import goblinbob.mobends.api.annotation.Internal;

public interface IAddon
{

	@Internal
	default void registerContent(AddonAnimationRegistry registry)
	{
	}

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
