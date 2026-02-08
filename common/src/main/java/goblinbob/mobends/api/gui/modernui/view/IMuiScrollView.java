package goblinbob.mobends.api.gui.modernui.view;

/**
 * Abstraction for Modern UI ScrollView.
 * A container that scrolls its single child vertically.
 */
public interface IMuiScrollView extends IMuiViewGroup
{
    /**
     * Scrolls to a specific Y position.
     *
     * @param y The Y position to scroll to
     */
    void scrollTo(int y);

    /**
     * Scrolls by a relative amount.
     *
     * @param dy The amount to scroll by
     */
    void scrollBy(int dy);

    /**
     * Smoothly scrolls to a Y position with animation.
     *
     * @param y The Y position to scroll to
     */
    void smoothScrollTo(int y);

    /**
     * @return The current vertical scroll position
     */
    int getScrollY();

    /**
     * Sets whether the scroll bar should be visible.
     *
     * @param visible True to show the scroll bar
     */
    void setVerticalScrollBarEnabled(boolean visible);

    /**
     * Sets whether overscroll effects are enabled.
     *
     * @param enabled True to enable overscroll
     */
    void setOverScrollEnabled(boolean enabled);
}
