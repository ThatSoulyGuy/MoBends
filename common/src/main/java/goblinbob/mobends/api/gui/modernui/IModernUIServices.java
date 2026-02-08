package goblinbob.mobends.api.gui.modernui;

import javax.annotation.Nullable;

/**
 * Central service interface for Modern UI integration.
 * Abstracts API differences between Modern UI 3.9.x (Forge 1.20.1)
 * and 3.11.x (NeoForge 1.21.1).
 *
 * Modern UI is an optional dependency - this service will only be available
 * when Modern UI is installed.
 */
public interface IModernUIServices
{
    /**
     * @return Whether Modern UI is available and properly initialized
     */
    boolean isAvailable();

    /**
     * @return The Modern UI version string (e.g., "3.9.x" or "3.11.x")
     */
    String getModernUIVersion();

    /**
     * Gets a context-aware view factory for creating UI views.
     * The context is required for Modern UI 3.9.x (Forge), optional for 3.11.x (NeoForge).
     *
     * @param context The native Modern UI Context (typically from Fragment.getContext())
     * @return Factory for creating UI views within the given context
     */
    IViewFactory getViewFactory(Object context);

    /**
     * Gets a view factory without context.
     * Only works on platforms where views don't require context (e.g., Modern UI 3.11.x).
     * For broader compatibility, prefer using {@link #getViewFactory(Object)}.
     *
     * @return Factory for creating UI views, or null if context is required
     */
    default IViewFactory getViewFactory()
    {
        return null;
    }

    /**
     * @return Theme provider for styling UI components
     */
    IThemeProvider getThemeProvider();

    /**
     * Opens a Modern UI screen built from the given builder.
     *
     * @param screenBuilder Builder for the screen content
     */
    void openScreen(IMuiScreenBuilder screenBuilder);

    /**
     * @return Whether this version supports animated drawables (3.11.x feature)
     */
    default boolean supportsAnimatedDrawables()
    {
        return false;
    }

    /**
     * @return Whether this version supports subpixel text rendering (3.11.x feature)
     */
    default boolean supportsSubpixelText()
    {
        return false;
    }

    /**
     * Singleton holder pattern (matching existing codebase conventions).
     */
    class Holder
    {
        private static IModernUIServices instance;

        public static void setServices(IModernUIServices services)
        {
            if (instance != null)
            {
                throw new IllegalStateException("IModernUIServices already initialized");
            }
            instance = services;
        }

        @Nullable
        public static IModernUIServices get()
        {
            return instance;
        }

        public static boolean isAvailable()
        {
            return instance != null && instance.isAvailable();
        }
    }
}
