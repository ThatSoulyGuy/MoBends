package goblinbob.mobends.neoforge.gui.modernui;

import goblinbob.mobends.api.gui.*;
import goblinbob.mobends.api.gui.view.IView;
import icyllis.modernui.fragment.Fragment;
import icyllis.modernui.mc.ScreenCallback;
import icyllis.modernui.mc.neoforge.MuiForgeApi;
import icyllis.modernui.view.LayoutInflater;
import icyllis.modernui.view.View;
import icyllis.modernui.view.ViewGroup;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * NeoForge 1.21.1 implementation of IUIServices.
 * Wraps Modern UI 3.11.x API.
 */
public class NeoForgeModernUIServices implements IUIServices
{
    private final NeoForgeThemeProvider themeProvider;

    public NeoForgeModernUIServices()
    {
        this.themeProvider = new NeoForgeThemeProvider();
    }

    @Override
    public boolean isAvailable()
    {
        return true; // We only instantiate this if Modern UI is present
    }

    @Override
    public String getBackendVersion()
    {
        return "3.11.x";
    }

    @Override
    public IViewFactory getViewFactory(Object context)
    {
        if (context instanceof icyllis.modernui.core.Context ctx)
        {
            return new NeoForgeViewFactory(ctx);
        }
        throw new IllegalArgumentException("Expected Modern UI Context, got: " +
                (context != null ? context.getClass().getName() : "null"));
    }

    @Override
    public IViewFactory getViewFactory()
    {
        // Context is also required in 3.11.x
        return null;
    }

    @Override
    public IThemeProvider getThemeProvider()
    {
        return themeProvider;
    }

    @Override
    public void openScreen(IScreenBuilder screenBuilder)
    {
        try
        {
            // Store the builder and services for the Fragment to retrieve
            // (Fragments require a no-arg constructor for recreation, so we can't pass these directly)
            MoBendsScreenFragment.pendingScreenBuilder = screenBuilder;
            MoBendsScreenFragment.pendingServices = this;

            // Create a Fragment that will build the UI
            Fragment fragment = new MoBendsScreenFragment();

            // Activate the render handler for entity rendering
            MoBendsScreenRenderHandler.getInstance().activate(screenBuilder);

            // Create a simple ScreenCallback for screen properties
            ScreenCallback callback = new MoBendsScreenCallback();

            // Use MuiForgeApi to create the screen, then display it
            Screen screen = MuiForgeApi.get().createScreen(
                fragment,
                callback,
                Minecraft.getInstance().screen, // previousScreen
                screenBuilder.getTitle() // CharSequence title
            );

            Minecraft.getInstance().setScreen(screen);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Failed to open Modern UI screen", e);
        }
    }

    /**
     * ScreenCallback implementation for screen properties.
     * Note: ScreenCallback does NOT have render methods - rendering is handled
     * via NeoForge's ScreenEvent.Render.Post in MoBendsScreenRenderHandler.
     */
    private static class MoBendsScreenCallback implements ScreenCallback
    {
        @Override
        public boolean isPauseScreen()
        {
            // Don't pause the game when the MoBends settings screen is open
            return false;
        }

        @Override
        public boolean hasDefaultBackground()
        {
            // Use Modern UI's background handling
            return true;
        }
    }

    @Override
    public boolean supportsAnimatedDrawables()
    {
        return true; // 3.11.x has AnimationDrawable
    }

    @Override
    public boolean supportsSubpixelText()
    {
        return true; // 3.11.x has subpixel positioning
    }

    /**
     * Fragment implementation that builds the MoBends screen content.
     * Must be public static with a no-arg constructor for Modern UI Fragment recreation.
     */
    public static class MoBendsScreenFragment extends Fragment
    {
        // Static fields for passing data to new Fragment instances
        // (Fragments require no-arg constructors, so we use this pattern)
        static IScreenBuilder pendingScreenBuilder;
        static NeoForgeModernUIServices pendingServices;

        private IScreenBuilder screenBuilder;
        private NeoForgeModernUIServices services;

        /**
         * Required public no-arg constructor for Fragment recreation.
         */
        public MoBendsScreenFragment()
        {
        }

        @Override
        public void onCreate(@Nullable icyllis.modernui.util.DataSet savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            // Retrieve the pending builder and services
            this.screenBuilder = pendingScreenBuilder;
            this.services = pendingServices;
        }

        @Nullable
        @Override
        public View onCreateView(@Nonnull LayoutInflater inflater,
                                 @Nullable ViewGroup container,
                                 @Nullable icyllis.modernui.util.DataSet savedInstanceState)
        {
            if (screenBuilder == null || services == null)
            {
                throw new IllegalStateException("MoBendsScreenFragment was recreated without pending data");
            }

            // Get context from the fragment
            icyllis.modernui.core.Context context = requireContext();

            // Create a context-aware view factory
            IViewFactory factory = services.getViewFactory(context);

            // Build the content using the factory
            IView rootView = screenBuilder.buildContent(factory);

            // Return the native view
            return (View) rootView.getNativeView();
        }

        @Override
        public void onDestroy()
        {
            super.onDestroy();
            // Deactivate the render handler when the screen closes
            MoBendsScreenRenderHandler.getInstance().deactivate();
        }
    }
}
