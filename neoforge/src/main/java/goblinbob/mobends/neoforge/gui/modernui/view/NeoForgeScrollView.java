package goblinbob.mobends.neoforge.gui.modernui.view;

import goblinbob.mobends.api.gui.modernui.view.IMuiScrollView;
import icyllis.modernui.widget.ScrollView;

/**
 * NeoForge wrapper for Modern UI ScrollView.
 * Uses Modern UI 3.11.x API.
 */
public class NeoForgeScrollView extends NeoForgeViewGroup implements IMuiScrollView
{
    private final ScrollView nativeScrollView;

    public NeoForgeScrollView(ScrollView nativeView)
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
        // Modern UI 3.11.x may not have smoothScrollTo, use regular scrollTo
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
        // Modern UI 3.11.x may not have this method
        // This is a no-op placeholder
    }
}
