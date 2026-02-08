package goblinbob.mobends.forge.gui.modernui.view;

import goblinbob.mobends.api.gui.modernui.view.IMuiScrollView;
import icyllis.modernui.widget.ScrollView;

/**
 * Forge wrapper for Modern UI ScrollView.
 */
public class ForgeScrollView extends ForgeViewGroup implements IMuiScrollView
{
    private final ScrollView nativeScrollView;

    public ForgeScrollView(ScrollView nativeView)
    {
        super(nativeView);
        this.nativeScrollView = nativeView;
    }

    @Override
    public void scrollTo(int y)
    {
        nativeScrollView.scrollTo(0, y);
    }

    @Override
    public void scrollBy(int dy)
    {
        nativeScrollView.scrollBy(0, dy);
    }

    @Override
    public void smoothScrollTo(int y)
    {
        // Modern UI 3.9.x may not have smoothScrollTo, use regular scrollTo
        nativeScrollView.scrollTo(0, y);
    }

    @Override
    public int getScrollY()
    {
        return nativeScrollView.getScrollY();
    }

    @Override
    public void setVerticalScrollBarEnabled(boolean visible)
    {
        nativeScrollView.setVerticalScrollBarEnabled(visible);
    }

    @Override
    public void setOverScrollEnabled(boolean enabled)
    {
        // Modern UI 3.9.x may not have this method
        // This is a no-op placeholder
    }
}
