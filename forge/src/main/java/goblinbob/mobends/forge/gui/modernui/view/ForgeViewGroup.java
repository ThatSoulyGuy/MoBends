package goblinbob.mobends.forge.gui.modernui.view;

import goblinbob.mobends.api.gui.modernui.ILayoutParams;
import goblinbob.mobends.api.gui.modernui.view.IMuiView;
import goblinbob.mobends.api.gui.modernui.view.IMuiViewGroup;
import goblinbob.mobends.forge.gui.modernui.ForgeLayoutParams;
import icyllis.modernui.view.View;
import icyllis.modernui.view.ViewGroup;

import javax.annotation.Nullable;

/**
 * Forge wrapper for Modern UI ViewGroup.
 */
public class ForgeViewGroup extends ForgeView implements IMuiViewGroup
{
    protected final ViewGroup nativeViewGroup;

    public ForgeViewGroup(ViewGroup nativeView)
    {
        super(nativeView);
        this.nativeViewGroup = nativeView;
    }

    @Override
    public void addView(IMuiView child)
    {
        nativeViewGroup.addView((View) child.getNativeView());
    }

    @Override
    public void addView(IMuiView child, ILayoutParams params)
    {
        nativeViewGroup.addView((View) child.getNativeView(),
                               (ViewGroup.LayoutParams) params.getNativeLayoutParams());
    }

    @Override
    public void addView(IMuiView child, int index)
    {
        nativeViewGroup.addView((View) child.getNativeView(), index);
    }

    @Override
    public void removeView(IMuiView child)
    {
        nativeViewGroup.removeView((View) child.getNativeView());
    }

    @Override
    public void removeViewAt(int index)
    {
        nativeViewGroup.removeViewAt(index);
    }

    @Override
    public void removeAllViews()
    {
        nativeViewGroup.removeAllViews();
    }

    @Override
    public int getChildCount()
    {
        return nativeViewGroup.getChildCount();
    }

    @Nullable
    @Override
    public IMuiView getChildAt(int index)
    {
        View child = nativeViewGroup.getChildAt(index);
        if (child == null)
        {
            return null;
        }
        return new ForgeView(child);
    }

    @Nullable
    @Override
    public IMuiView findViewById(int id)
    {
        View view = nativeViewGroup.findViewById(id);
        if (view == null)
        {
            return null;
        }
        return new ForgeView(view);
    }
}
