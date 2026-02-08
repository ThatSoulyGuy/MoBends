package goblinbob.mobends.forge.gui.modernui;

import goblinbob.mobends.api.gui.modernui.*;
import goblinbob.mobends.api.gui.modernui.view.IMuiView;
import icyllis.modernui.fragment.Fragment;
import icyllis.modernui.view.LayoutInflater;
import icyllis.modernui.view.View;
import icyllis.modernui.view.ViewGroup;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Forge 1.20.1 implementation of IModernUIServices.
 * Wraps Modern UI 3.9.x API.
 */
public class ForgeModernUIServices implements IModernUIServices
{
    private final ForgeThemeProvider themeProvider;

    public ForgeModernUIServices()
    {
        this.themeProvider = new ForgeThemeProvider();
    }

    @Override
    public boolean isAvailable()
    {
        return true; // We only instantiate this if Modern UI is present
    }

    @Override
    public String getModernUIVersion()
    {
        return "3.9.x";
    }

    @Override
    public IViewFactory getViewFactory(Object context)
    {
        if (context instanceof icyllis.modernui.core.Context ctx)
        {
            return new ForgeViewFactory(ctx);
        }
        throw new IllegalArgumentException("Expected Modern UI Context, got: " +
                (context != null ? context.getClass().getName() : "null"));
    }

    @Override
    public IViewFactory getViewFactory()
    {
        // Context is required in 3.9.x
        return null;
    }

    @Override
    public IThemeProvider getThemeProvider()
    {
        return themeProvider;
    }

    @Override
    public void openScreen(IMuiScreenBuilder screenBuilder)
    {
        try
        {
            // Store the builder and services for the Fragment to retrieve
            // (Fragments require a no-arg constructor for recreation, so we can't pass these directly)
            MoBendsScreenFragment.pendingScreenBuilder = screenBuilder;
            MoBendsScreenFragment.pendingServices = this;

            // Create a Fragment that will build the UI
            Fragment fragment = new MoBendsScreenFragment();
            icyllis.modernui.mc.forge.MuiForgeApi.openScreen(fragment);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Failed to open Modern UI screen", e);
        }
    }

    @Override
    public boolean supportsAnimatedDrawables()
    {
        return false; // 3.9.x doesn't have the extended drawable API
    }

    @Override
    public boolean supportsSubpixelText()
    {
        return false; // 3.9.x doesn't have subpixel positioning
    }

    /**
     * Fragment implementation that builds the MoBends screen content.
     * Must be public static with a no-arg constructor for Modern UI Fragment recreation.
     */
    public static class MoBendsScreenFragment extends Fragment
    {
        // Static fields for passing data to new Fragment instances
        // (Fragments require no-arg constructors, so we use this pattern)
        static IMuiScreenBuilder pendingScreenBuilder;
        static ForgeModernUIServices pendingServices;

        private IMuiScreenBuilder screenBuilder;
        private ForgeModernUIServices services;

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
            IMuiView rootView = screenBuilder.buildContent(factory);

            // Return the native view
            return (View) rootView.getNativeView();
        }
    }
}
