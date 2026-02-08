package goblinbob.mobends.forge.gui.modernui.view;

import goblinbob.mobends.api.gui.modernui.ILayoutParams;
import goblinbob.mobends.api.gui.modernui.IViewFactory;
import goblinbob.mobends.api.gui.modernui.view.IMuiLinearLayout;
import goblinbob.mobends.api.gui.modernui.view.IMuiView;
import icyllis.modernui.view.View;
import icyllis.modernui.view.ViewGroup;
import icyllis.modernui.widget.LinearLayout;

/**
 * Forge wrapper for Modern UI LinearLayout.
 */
public class ForgeLinearLayout extends ForgeViewGroup implements IMuiLinearLayout
{
    private final LinearLayout nativeLinearLayout;
    private final IViewFactory factory;
    private int spacing = 0;

    public ForgeLinearLayout(LinearLayout nativeView, IViewFactory factory)
    {
        super(nativeView);
        this.nativeLinearLayout = nativeView;
        this.factory = factory;
    }

    @Override
    public void addView(IMuiView child, ILayoutParams params)
    {
        ViewGroup.LayoutParams nativeParams = (ViewGroup.LayoutParams) params.getNativeLayoutParams();

        // Convert to LinearLayout.LayoutParams if needed
        if (!(nativeParams instanceof LinearLayout.LayoutParams))
        {
            LinearLayout.LayoutParams linearParams = new LinearLayout.LayoutParams(
                    nativeParams.width,
                    nativeParams.height
            );
            if (nativeParams instanceof ViewGroup.MarginLayoutParams marginParams)
            {
                linearParams.setMargins(
                        marginParams.leftMargin,
                        marginParams.topMargin,
                        marginParams.rightMargin,
                        marginParams.bottomMargin
                );
            }
            nativeParams = linearParams;
        }

        nativeLinearLayout.addView((View) child.getNativeView(), nativeParams);
    }

    @Override
    public void setOrientation(int orientation)
    {
        nativeLinearLayout.setOrientation(orientation);
    }

    @Override
    public int getOrientation()
    {
        return nativeLinearLayout.getOrientation();
    }

    @Override
    public void setGravity(int gravity)
    {
        nativeLinearLayout.setGravity(gravity);
    }

    @Override
    public void setSpacing(int spacing)
    {
        this.spacing = factory.dp(spacing);
    }
}
