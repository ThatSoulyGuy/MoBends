package goblinbob.mobends.core.client.gui.modernui;

import goblinbob.mobends.api.gui.modernui.IModernUIServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bridge for Modern UI integration.
 * Modern UI is a required dependency - MoBends will not function without it.
 */
public final class MuiBridge
{
    private static final Logger LOGGER = LoggerFactory.getLogger(MuiBridge.class);
    private static Boolean modernUIAvailable = null;

    private MuiBridge()
    {
        // Utility class
    }

    /**
     * Check if Modern UI is available and properly initialized.
     * Since Modern UI is required, this should always return true in a properly configured environment.
     *
     * @return True if Modern UI is available and services are registered
     */
    public static boolean isModernUIAvailable()
    {
        if (modernUIAvailable == null)
        {
            modernUIAvailable = checkModernUIAvailable();
        }
        return modernUIAvailable;
    }

    /**
     * Resets the cached availability check.
     * Used for testing or when Modern UI state changes.
     */
    public static void resetAvailabilityCheck()
    {
        modernUIAvailable = null;
    }

    private static boolean checkModernUIAvailable()
    {
        // Modern UI is a required dependency enforced by the mod loader.
        // We just need to verify the services were registered by the platform module.
        if (!IModernUIServices.Holder.isAvailable())
        {
            LOGGER.error("Modern UI services not registered! MoBends requires Modern UI to be installed.");
            return false;
        }

        LOGGER.info("Modern UI services available: {}", IModernUIServices.Holder.get().getModernUIVersion());
        return true;
    }

    /**
     * Opens the MoBends settings screen using Modern UI.
     * Modern UI is required - this will throw an exception if not available.
     */
    public static void openSettingsScreen()
    {
        if (!isModernUIAvailable())
        {
            throw new IllegalStateException(
                "Modern UI is required but not available. Please install Modern UI to use MoBends.");
        }

        IModernUIServices services = IModernUIServices.Holder.get();
        if (services == null)
        {
            throw new IllegalStateException(
                "Modern UI services not initialized. This is a bug - please report it.");
        }

        services.openScreen(new MuiMoBendsScreen());
    }

    /**
     * @return The Modern UI version string
     */
    public static String getModernUIVersion()
    {
        IModernUIServices services = IModernUIServices.Holder.get();
        if (services == null)
        {
            return "Not Available";
        }
        return services.getModernUIVersion();
    }

    /**
     * @return Whether animated drawables are supported (3.11.x feature)
     */
    public static boolean supportsAnimatedDrawables()
    {
        IModernUIServices services = IModernUIServices.Holder.get();
        return services != null && services.supportsAnimatedDrawables();
    }

    /**
     * @return Whether subpixel text rendering is supported (3.11.x feature)
     */
    public static boolean supportsSubpixelText()
    {
        IModernUIServices services = IModernUIServices.Holder.get();
        return services != null && services.supportsSubpixelText();
    }
}
