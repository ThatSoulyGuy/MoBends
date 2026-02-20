package goblinbob.mobends.api.gui;

import goblinbob.mobends.api.gui.view.IView;

/**
 * Builder interface for creating UI screens.
 * Implementations provide the screen title and content.
 */
public interface IScreenBuilder
{
    String getTitle();

    IView buildContent(IViewFactory factory);
}
