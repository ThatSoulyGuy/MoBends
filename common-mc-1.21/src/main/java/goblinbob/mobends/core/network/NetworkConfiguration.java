package goblinbob.mobends.core.network;

public class NetworkConfiguration
{
    public static NetworkConfiguration instance = new NetworkConfiguration();

    public boolean areBendsPacksAllowed()
    {
        return true;
    }

    public boolean isMovementLimited()
    {
        return false;
    }
}
