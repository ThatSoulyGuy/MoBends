package goblinbob.mobends.neoforge.network;

import goblinbob.mobends.core.network.NetworkConfiguration;

/**
 * NeoForge-specific NetworkConfiguration that provides SharedConfig for network sync.
 */
public class NeoForgeNetworkConfiguration extends NetworkConfiguration
{
    public static final NeoForgeNetworkConfiguration INSTANCE = new NeoForgeNetworkConfiguration();

    private final SharedConfig sharedConfig;
    private final SharedBooleanProp allowBendspacks;
    private final SharedBooleanProp limitMovement;

    private NeoForgeNetworkConfiguration()
    {
        this.sharedConfig = new SharedConfig();
        this.allowBendspacks = new SharedBooleanProp("allow_bendspacks", true, "Whether bendspacks are allowed");
        this.limitMovement = new SharedBooleanProp("limit_movement", false, "Whether movement is limited");

        this.sharedConfig.addProperty(allowBendspacks);
        this.sharedConfig.addProperty(limitMovement);
    }

    public SharedConfig getSharedConfig()
    {
        return sharedConfig;
    }

    @Override
    public boolean areBendsPacksAllowed()
    {
        return allowBendspacks.getValue();
    }

    @Override
    public boolean isMovementLimited()
    {
        return limitMovement.getValue();
    }

    /**
     * Initialize the NeoForge network configuration.
     * Should be called during mod initialization.
     */
    public static void init()
    {
        // Replace the common-mc stub with our implementation
        NetworkConfiguration.instance = INSTANCE;
    }
}
