package goblinbob.mobends.core.client.gui;

import goblinbob.mobends.api.gui.IUIServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bridge for UI backend integration.
 * The UI backend is a required dependency - MoBends will not function without it.
 */
public final class UIBridge
{
    private static final Logger LOGGER = LoggerFactory.getLogger(UIBridge.class);
    private static Boolean backendAvailable = null;

    private UIBridge()
    {
        // Utility class
    }

    /**
     * Check if the UI backend is available and properly initialized.
     * Since the UI backend is required, this should always return true in a properly configured environment.
     *
     * @return True if the UI backend is available and services are registered
     */
    public static boolean isBackendAvailable()
    {
        if (backendAvailable == null)
        {
            backendAvailable = checkBackendAvailable();
        }
        return backendAvailable;
    }

    /**
     * Resets the cached availability check.
     * Used for testing or when UI backend state changes.
     */
    public static void resetAvailabilityCheck()
    {
        backendAvailable = null;
    }

    private static boolean checkBackendAvailable()
    {
        // The UI backend is a required dependency enforced by the mod loader.
        // We just need to verify the services were registered by the platform module.
        if (!IUIServices.Holder.isAvailable())
        {
            LOGGER.error("UI services not registered! MoBends requires the UI backend to be installed.");
            return false;
        }

        LOGGER.info("UI services available: {}", IUIServices.Holder.get().getBackendVersion());
        return true;
    }

    /**
     * Opens the MoBends settings screen using the UI backend.
     * The UI backend is required - this will throw an exception if not available.
     */
    public static void openSettingsScreen()
    {
        if (!isBackendAvailable())
        {
            throw new IllegalStateException(
                "UI backend is required but not available. Please install the UI backend to use MoBends.");
        }

        IUIServices services = IUIServices.Holder.get();
        if (services == null)
        {
            throw new IllegalStateException(
                "UI services not initialized. This is a bug - please report it.");
        }

        services.openScreen(new MoBendsScreenBuilder());
    }

    /**
     * @return The UI backend version string
     */
    public static String getBackendVersion()
    {
        IUIServices services = IUIServices.Holder.get();
        if (services == null)
        {
            return "Not Available";
        }
        return services.getBackendVersion();
    }

    /**
     * @return Whether animated drawables are supported (3.11.x feature)
     */
    public static boolean supportsAnimatedDrawables()
    {
        IUIServices services = IUIServices.Holder.get();
        return services != null && services.supportsAnimatedDrawables();
    }

    /**
     * @return Whether subpixel text rendering is supported (3.11.x feature)
     */
    public static boolean supportsSubpixelText()
    {
        IUIServices services = IUIServices.Holder.get();
        return services != null && services.supportsSubpixelText();
    }
}
