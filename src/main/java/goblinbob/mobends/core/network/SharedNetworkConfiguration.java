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

    // Server-side setters. The wire protocol was always complete -- the client asks, the server
    // serialises this config to NBT and sends it back -- but nothing ever wrote these values, so
    // the server serialised its defaults and the answer was the same whatever the server wanted.
    // Each loader's SERVER config spec pushes its values in here when a world loads.

    public void setBendsPacksAllowed(boolean allowed)
    {
        allowBendspacks.setValue(allowed);
    }

    public void setMovementLimited(boolean limited)
    {
        limitMovement.setValue(limited);
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
