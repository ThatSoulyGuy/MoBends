package goblinbob.mobends.forge.gui.modernui.view;

import goblinbob.mobends.api.gui.ILayoutParams;
import goblinbob.mobends.api.gui.view.IView;
import goblinbob.mobends.api.gui.view.IViewGroup;
import goblinbob.mobends.forge.gui.modernui.ForgeLayoutParams;
import icyllis.modernui.view.View;
import icyllis.modernui.view.ViewGroup;

import javax.annotation.Nullable;

/**
 * Forge wrapper for Modern UI ViewGroup.
 */
public class ForgeViewGroup extends ForgeView implements IViewGroup
{
    protected final ViewGroup nativeViewGroup;

    public ForgeViewGroup(ViewGroup nativeView)
    {
        super(nativeView);
        this.nativeViewGroup = nativeView;
    }

    @Override
    public void addView(IView child)
    {
        nativeViewGroup.addView((View) child.getNativeView());
    }

    @Override
    public void addView(IView child, ILayoutParams params)
    {
        nativeViewGroup.addView((View) child.getNativeView(),
                               (ViewGroup.LayoutParams) params.getNativeLayoutParams());
    }

    @Override
    public void addView(IView child, int index)
    {
        nativeViewGroup.addView((View) child.getNativeView(), index);
    }

    @Override
    public void removeView(IView child)
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
    public IView getChildAt(int index)
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
    public IView findViewById(int id)
    {
        View view = nativeViewGroup.findViewById(id);
        if (view == null)
        {
            return null;
        }
        return new ForgeView(view);
    }
}
