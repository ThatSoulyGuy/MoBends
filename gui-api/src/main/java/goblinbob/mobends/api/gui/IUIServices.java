package goblinbob.mobends.api.gui;

import javax.annotation.Nullable;

/**
 * Central service interface for UI backend integration.
 * Abstracts API differences between different UI backends
 * and their versions.
 */
public interface IUIServices
{
    boolean isAvailable();

    String getBackendVersion();

    IViewFactory getViewFactory(Object context);

    default IViewFactory getViewFactory()
    {
        return null;
    }

    IThemeProvider getThemeProvider();

    void openScreen(IScreenBuilder screenBuilder);

    default boolean supportsAnimatedDrawables()
    {
        return false;
    }

    default boolean supportsSubpixelText()
    {
        return false;
    }

    class Holder
    {
        private static IUIServices instance;

        public static void setServices(IUIServices services)
        {
            if (instance != null)
            {
                throw new IllegalStateException("IUIServices already initialized");
            }
            instance = services;
        }

        @Nullable
        public static IUIServices get()
        {
            return instance;
        }

        public static boolean isAvailable()
        {
            return instance != null && instance.isAvailable();
        }
    }
}
