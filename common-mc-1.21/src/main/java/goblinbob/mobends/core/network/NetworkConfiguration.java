package goblinbob.mobends.core.network;

/**
 * Stub NetworkConfiguration for common-mc.
 * Platform-specific implementations provide actual network configuration.
 */
public class NetworkConfiguration
{
    public static NetworkConfiguration instance = new NetworkConfiguration();

    /**
     * @return Whether bends packs are allowed on the server
     */
    public boolean areBendsPacksAllowed()
    {
        return true;
    }

    /**
     * @return Whether movement is limited
     */
    public boolean isMovementLimited()
    {
        return false;
    }
}
