package goblinbob.mobends.core.client.gui.vanilla;

import goblinbob.mobends.api.gui.IScreenBuilder;
import goblinbob.mobends.api.gui.IThemeProvider;
import goblinbob.mobends.api.gui.IUIServices;
import goblinbob.mobends.api.gui.IViewFactory;
import net.minecraft.client.Minecraft;

public class VanillaUIServices implements IUIServices
{
    private final VanillaThemeProvider themeProvider = new VanillaThemeProvider();

    @Override
    public boolean isAvailable() { return true; }

    @Override
    public String getBackendVersion() { return "Vanilla MC"; }

    @Override
    public IViewFactory getViewFactory(Object context)
    {
        return new VanillaViewFactory();
    }

    @Override
    public IViewFactory getViewFactory()
    {
        return new VanillaViewFactory();
    }

    @Override
    public IThemeProvider getThemeProvider() { return themeProvider; }

    @Override
    public void openScreen(IScreenBuilder screenBuilder)
    {
        Minecraft.getInstance().setScreen(new VanillaMoBendsScreen(screenBuilder));
    }
}
