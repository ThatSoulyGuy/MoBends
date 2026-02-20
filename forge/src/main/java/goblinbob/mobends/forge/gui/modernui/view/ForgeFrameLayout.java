package goblinbob.mobends.forge.gui.modernui.view;

import goblinbob.mobends.api.gui.ILayoutParams;
import goblinbob.mobends.api.gui.IViewFactory;
import goblinbob.mobends.api.gui.view.IFrameLayout;
import goblinbob.mobends.api.gui.view.IView;
import icyllis.modernui.view.View;
import icyllis.modernui.view.ViewGroup;
import icyllis.modernui.widget.FrameLayout;

/**
 * Forge wrapper for Modern UI FrameLayout.
 */
public class ForgeFrameLayout extends ForgeViewGroup implements IFrameLayout
{
    private final FrameLayout nativeFrameLayout;
    private final IViewFactory factory;

    public ForgeFrameLayout(FrameLayout nativeView, IViewFactory factory)
    {
        super(nativeView);
        this.nativeFrameLayout = nativeView;
        this.factory = factory;
    }

    @Override
    public void addView(IView child, ILayoutParams params)
    {
        ViewGroup.LayoutParams nativeParams = (ViewGroup.LayoutParams) params.getNativeLayoutParams();

        // Convert to FrameLayout.LayoutParams if needed
        if (!(nativeParams instanceof FrameLayout.LayoutParams))
        {
            FrameLayout.LayoutParams frameParams = new FrameLayout.LayoutParams(
                    nativeParams.width,
                    nativeParams.height
            );
            if (nativeParams instanceof ViewGroup.MarginLayoutParams marginParams)
            {
                frameParams.setMargins(
                        marginParams.leftMargin,
                        marginParams.topMargin,
                        marginParams.rightMargin,
                        marginParams.bottomMargin
                );
            }
            nativeParams = frameParams;
        }

        nativeFrameLayout.addView((View) child.getNativeView(), nativeParams);
    }

    @Override
    public void setMeasureAllChildren(boolean measureAll)
    {
        nativeFrameLayout.setMeasureAllChildren(measureAll);
    }
}
