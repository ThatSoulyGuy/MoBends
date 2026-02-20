package goblinbob.mobends.api.gui.view;

import goblinbob.mobends.api.gui.ILayoutParams;

import javax.annotation.Nullable;

/**
 * Base abstraction for a UI view.
 * Wraps the native view class to provide a backend-agnostic interface.
 */
public interface IView
{
    int VISIBLE = 0;
    int INVISIBLE = 4;
    int GONE = 8;

    void setId(int id);
    int getId();

    void setLayoutParams(ILayoutParams params);
    @Nullable ILayoutParams getLayoutParams();
    void setPadding(int left, int top, int right, int bottom);

    int getWidth();
    int getHeight();
    void setMinimumWidth(int minWidth);
    void setMinimumHeight(int minHeight);

    void setVisibility(int visibility);
    int getVisibility();

    void setEnabled(boolean enabled);
    boolean isEnabled();

    void setAlpha(float alpha);
    float getAlpha();

    /**
     * Animates the view's alpha to the target value over the given duration.
     * Default implementation just sets the alpha immediately (no animation).
     */
    default void animateAlpha(float targetAlpha, int durationMs)
    {
        setAlpha(targetAlpha);
    }

    void setBackground(@Nullable Object drawable);
    void setBackgroundColor(int color);

    void setOnClickListener(@Nullable Runnable listener);

    Object getNativeView();
}
