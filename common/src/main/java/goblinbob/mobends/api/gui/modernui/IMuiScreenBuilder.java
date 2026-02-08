package goblinbob.mobends.api.gui.modernui;

import goblinbob.mobends.api.gui.modernui.view.IMuiView;

/**
 * Builder interface for creating Modern UI screens.
 * Implementations provide the screen title and content.
 */
public interface IMuiScreenBuilder
{
    /**
     * @return The screen title for display
     */
    String getTitle();

    /**
     * Builds and returns the root view for the screen content.
     * Called within the Fragment context where views can be created.
     *
     * @param factory A context-aware view factory for creating UI elements
     * @return The root view for the screen content
     */
    IMuiView buildContent(IViewFactory factory);
}
