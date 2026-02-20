package goblinbob.mobends.neoforge.gui.modernui;

import goblinbob.mobends.api.gui.IScreenBuilder;

import javax.annotation.Nullable;

/**
 * Lifecycle handler for the MoBends screen.
 * Entity preview rendering is handled directly by NeoForgeEntityPreviewView's onDraw().
 */
public class MoBendsScreenRenderHandler
{
    private static MoBendsScreenRenderHandler INSTANCE;

    @Nullable
    private IScreenBuilder currentScreenBuilder;

    private MoBendsScreenRenderHandler()
    {
    }

    public static MoBendsScreenRenderHandler getInstance()
    {
        if (INSTANCE == null)
        {
            INSTANCE = new MoBendsScreenRenderHandler();
        }
        return INSTANCE;
    }

    public void activate(IScreenBuilder screenBuilder)
    {
        this.currentScreenBuilder = screenBuilder;
    }

    public void deactivate()
    {
        this.currentScreenBuilder = null;
    }
}
