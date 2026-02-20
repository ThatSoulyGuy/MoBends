package goblinbob.mobends.api.gui.view;

/**
 * Abstraction for a scrollable container.
 */
public interface IScrollView extends IViewGroup
{
    void scrollTo(int y);
    void scrollBy(int dy);
    void smoothScrollTo(int y);
    int getScrollY();
    void setVerticalScrollBarEnabled(boolean visible);
    void setOverScrollEnabled(boolean enabled);
}
