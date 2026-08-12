package goblinbob.mobends.core.network;

public class SharedNetworkConfiguration extends NetworkConfiguration
{
    public static final SharedNetworkConfiguration INSTANCE = new SharedNetworkConfiguration();

    private final SharedConfig sharedConfig;
    private final SharedBooleanProp allowBendspacks;
    private final SharedBooleanProp limitMovement;

    private SharedNetworkConfiguration()
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

    public void resetToDefaults()
    {
        sharedConfig.resetToDefaults();
    }

    public static void init()
    {
        NetworkConfiguration.instance = INSTANCE;
    }
}
