package goblinbob.mobends.forge.gui.modernui.view;

import goblinbob.mobends.api.gui.modernui.ILayoutParams;
import goblinbob.mobends.api.gui.modernui.view.IMuiView;
import goblinbob.mobends.forge.gui.modernui.ForgeLayoutParams;
import icyllis.modernui.graphics.drawable.Drawable;
import icyllis.modernui.graphics.drawable.ShapeDrawable;
import icyllis.modernui.view.View;
import icyllis.modernui.view.ViewGroup;

import javax.annotation.Nullable;

/**
 * Forge wrapper for Modern UI View.
 */
public class ForgeView implements IMuiView
{
    protected final View nativeView;

    public ForgeView(View nativeView)
    {
        this.nativeView = nativeView;
    }

    @Override
    public void setId(int id)
    {
        nativeView.setId(id);
    }

    @Override
    public int getId()
    {
        return nativeView.getId();
    }

    @Override
    public void setLayoutParams(ILayoutParams params)
    {
        nativeView.setLayoutParams((ViewGroup.LayoutParams) params.getNativeLayoutParams());
    }

    @Nullable
    @Override
    public ILayoutParams getLayoutParams()
    {
        ViewGroup.LayoutParams params = nativeView.getLayoutParams();
        return params != null ? new ForgeLayoutParams(params) : null;
    }

    @Override
    public void setPadding(int left, int top, int right, int bottom)
    {
        nativeView.setPadding(dp(left), dp(top), dp(right), dp(bottom));
    }

    @Override
    public int getWidth()
    {
        return nativeView.getWidth();
    }

    @Override
    public int getHeight()
    {
        return nativeView.getHeight();
    }

    @Override
    public void setMinimumWidth(int minWidth)
    {
        nativeView.setMinimumWidth(dp(minWidth));
    }

    @Override
    public void setMinimumHeight(int minHeight)
    {
        nativeView.setMinimumHeight(dp(minHeight));
    }

    @Override
    public void setVisibility(int visibility)
    {
        nativeView.setVisibility(visibility);
    }

    @Override
    public int getVisibility()
    {
        return nativeView.getVisibility();
    }

    @Override
    public void setEnabled(boolean enabled)
    {
        nativeView.setEnabled(enabled);
    }

    @Override
    public boolean isEnabled()
    {
        return nativeView.isEnabled();
    }

    @Override
    public void setBackground(@Nullable Object drawable)
    {
        if (drawable instanceof Drawable d)
        {
            nativeView.setBackground(d);
        }
        else if (drawable == null)
        {
            nativeView.setBackground(null);
        }
    }

    @Override
    public void setBackgroundColor(int color)
    {
        // Modern UI 3.9.x doesn't have setBackgroundColor, use drawable instead
        ShapeDrawable bg = new ShapeDrawable();
        bg.setColor(color);
        nativeView.setBackground(bg);
    }

    @Override
    public void setOnClickListener(@Nullable Runnable listener)
    {
        if (listener != null)
        {
            nativeView.setOnClickListener(v -> listener.run());
        }
        else
        {
            nativeView.setOnClickListener(null);
        }
    }

    @Override
    public Object getNativeView()
    {
        return nativeView;
    }

    /**
     * Converts dp to pixels.
     */
    protected int dp(int dp)
    {
        // Modern UI handles density-independent pixels internally
        // In most cases, 1dp = 1px at default density
        return dp;
    }
}
