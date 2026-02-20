package goblinbob.mobends.forge.gui.modernui.view;

import goblinbob.mobends.api.gui.ILayoutParams;
import goblinbob.mobends.api.gui.view.IView;
import goblinbob.mobends.forge.gui.modernui.ForgeLayoutParams;
import icyllis.modernui.graphics.drawable.Drawable;
import icyllis.modernui.graphics.drawable.ShapeDrawable;
import icyllis.modernui.view.View;
import icyllis.modernui.view.ViewGroup;

import javax.annotation.Nullable;

/**
 * Forge wrapper for Modern UI View.
 */
public class ForgeView implements IView
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
    public void setAlpha(float alpha)
    {
        nativeView.setAlpha(alpha);
    }

    @Override
    public float getAlpha()
    {
        return nativeView.getAlpha();
    }

    @Override
    public void animateAlpha(float targetAlpha, int durationMs)
    {
        icyllis.modernui.animation.ObjectAnimator anim =
                icyllis.modernui.animation.ObjectAnimator.ofFloat(nativeView, View.ALPHA, targetAlpha);
        anim.setDuration(durationMs);
        anim.start();
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
        // Reuse existing ShapeDrawable if possible to avoid excessive allocations
        Drawable existing = nativeView.getBackground();
        if (existing instanceof ShapeDrawable shape)
        {
            shape.setColor(color);
            existing.invalidateSelf();
        }
        else
        {
            ShapeDrawable bg = new ShapeDrawable();
            bg.setColor(color);
            nativeView.setBackground(bg);
        }
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
        return nativeView.dp(dp);
    }
}
