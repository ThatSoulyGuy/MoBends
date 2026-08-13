package goblinbob.mobends.api.platform;

import java.util.ServiceLoader;

public final class PlatformServices
{
    private static IPlatformServices instance;

    private PlatformServices()
    {
    }

    public static IPlatformServices get()
    {
        if (instance == null)
        {
            instance = load();
        }
        return instance;
    }

    public static void set(IPlatformServices services)
    {
        if (instance != null)
        {
            throw new IllegalStateException("PlatformServices already initialized with: " + instance.getPlatformName());
        }
        instance = services;
    }

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
