package goblinbob.mobends.core.network;

public class SharedNetworkConfiguration
{
    public static final SharedNetworkConfiguration INSTANCE = new SharedNetworkConfiguration();

    private final SharedConfig sharedConfig;
    private final SharedBooleanProp allowBendspacks;
    private final SharedBooleanProp limitMovement;

    private SharedNetworkConfiguration()
    {
        this.sharedConfig = new SharedConfig();
        this.allowBendspacks = new SharedBooleanProp("allow_bendspacks", true);
        this.limitMovement = new SharedBooleanProp("limit_movement", false);

        this.sharedConfig.addProperty(allowBendspacks);
        this.sharedConfig.addProperty(limitMovement);
    }

    public SharedConfig getSharedConfig()
    {
        return sharedConfig;
    }

    public boolean areBendsPacksAllowed()
    {
        return allowBendspacks.getValue();
    }

    public boolean isMovementLimited()
    {
        return limitMovement.getValue();
    }


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
}
