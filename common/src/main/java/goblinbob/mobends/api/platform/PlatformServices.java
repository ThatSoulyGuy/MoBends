package goblinbob.mobends.api.platform;

import java.util.ServiceLoader;

/**
 * Service loader for platform services.
 * Uses Java's ServiceLoader to discover and load the platform implementation.
 */
public final class PlatformServices
{
    private static IPlatformServices instance;

    private PlatformServices()
    {
    }

    /**
     * Gets the platform services instance.
     * The instance is loaded lazily using ServiceLoader.
     *
     * @return The platform services instance
     * @throws IllegalStateException if no platform implementation is found
     */
    public static IPlatformServices get()
    {
        if (instance == null)
        {
            instance = load();
        }
        return instance;
    }

    /**
     * Sets the platform services instance.
     * This should only be called once during platform initialization.
     *
     * @param services The platform services implementation
     * @throws IllegalStateException if already initialized
     */
    public static void set(IPlatformServices services)
    {
        if (instance != null)
        {
            throw new IllegalStateException("PlatformServices already initialized with: " + instance.getPlatformName());
        }
        instance = services;
    }

    /**
     * Checks if platform services have been initialized
     * @return true if initialized
     */
    public static boolean isInitialized()
    {
        return instance != null;
    }

    private static IPlatformServices load()
    {
        ServiceLoader<IPlatformServices> loader = ServiceLoader.load(IPlatformServices.class);

        for (IPlatformServices services : loader)
        {
            return services;
        }

        throw new IllegalStateException(
                "No IPlatformServices implementation found! " +
                        "Make sure a platform module (neoforge, forge, fabric) is present and properly configured."
        );
    }
}
