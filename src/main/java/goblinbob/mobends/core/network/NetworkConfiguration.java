package goblinbob.mobends.core.network;

import net.minecraft.client.Minecraft;

/**
 * These are options that are provided by a server the player's playing on.
 * They are default when playing on single-player.
 */
public class NetworkConfiguration
{

	public static NetworkConfiguration instance = new NetworkConfiguration();

	private final SharedConfig sharedConfig = new SharedConfig();
	private final SharedProperty<Boolean> modelScalingAllowed;
    private final SharedProperty<Boolean> bendsPacksAllowed;
    private final SharedProperty<Boolean> movementLimited;

    public NetworkConfiguration()
    {
        sharedConfig.addProperty(modelScalingAllowed = new SharedBooleanProp(
                "modelScalingAllowed",
                false,
                "Does the server allow scaling of the player model more than the normal size?"));
        sharedConfig.addProperty(bendsPacksAllowed = new SharedBooleanProp(
                "bendsPacksAllowed",
                true,
                "Does the server allow the use of bends packs?"));
        sharedConfig.addProperty(movementLimited = new SharedBooleanProp(
                "movementLimited",
                true,
                "Does the server limit excessive bends pack transformation?"));
    }

    /**
     * Sets up the default permissions before receiving the server's config.
     * Used when connecting to a server that has Mo' Bends installed.
     * Sets restrictive defaults that the server can then relax via config response.
     */
	public void onWorldJoin()
    {
        this.modelScalingAllowed.setValue(Minecraft.getInstance().isSingleplayer());
        this.bendsPacksAllowed.setValue(true);
        this.movementLimited.setValue(!Minecraft.getInstance().isSingleplayer());
    }

    /**
     * Sets up permissive defaults for vanilla servers that don't have Mo' Bends.
     * Allows all features since there's no server-side enforcement anyway.
     */
    public void onVanillaServerJoin()
    {
        // Vanilla servers can't enforce restrictions, so allow everything (like singleplayer)
        this.modelScalingAllowed.setValue(true);
        this.bendsPacksAllowed.setValue(true);
        this.movementLimited.setValue(false);
    }

    public SharedConfig getSharedConfig()
    {
        return sharedConfig;
    }

	public boolean isModelScalingAllowed()
	{
		return modelScalingAllowed.getValue();
	}

	public boolean areBendsPacksAllowed()
    {
	    return bendsPacksAllowed.getValue();
    }

    public boolean isMovementLimited()
    {
        return movementLimited.getValue();
    }

}
