package goblinbob.mobends.api.gui.modernui.view;

import goblinbob.mobends.api.gui.modernui.ILayoutParams;

import javax.annotation.Nullable;

/**
 * Base abstraction for Modern UI View.
 * Wraps the native View class to provide a platform-agnostic interface.
 */
public interface IMuiView
{
    // ==================== Visibility Constants ====================
    // These must match Modern UI / Android View visibility constants

    int VISIBLE = 0;
    int INVISIBLE = 4;  // 0x04 - View takes up space but is not drawn
    int GONE = 8;       // 0x08 - View is completely hidden and takes no space

    // ==================== Identity ====================

    /**
     * Sets a unique ID for this view.
     *
     * @param id The view ID
     */
    void setId(int id);

    /**
     * @return The view ID, or NO_ID if not set
     */
    int getId();

    // ==================== Layout ====================

    /**
     * Sets the layout parameters for this view.
     *
     * @param params The layout parameters
     */
    void setLayoutParams(ILayoutParams params);

    /**
     * @return The current layout parameters
     */
    @Nullable
    ILayoutParams getLayoutParams();

    /**
     * Sets padding inside this view.
     *
     * @param left Left padding in dp
     * @param top Top padding in dp
     * @param right Right padding in dp
     * @param bottom Bottom padding in dp
     */
    void setPadding(int left, int top, int right, int bottom);

    // ==================== Size ====================

    /**
     * @return The measured width of the view after layout
     */
    int getWidth();

    /**
     * @return The measured height of the view after layout
     */
    int getHeight();

    /**
     * Sets a minimum width for this view.
     *
     * @param minWidth Minimum width in dp
     */
    void setMinimumWidth(int minWidth);

    /**
     * Sets a minimum height for this view.
     *
     * @param minHeight Minimum height in dp
     */
    void setMinimumHeight(int minHeight);

    // ==================== Visibility ====================

    /**
     * Sets the visibility of this view.
     *
     * @param visibility VISIBLE, INVISIBLE, or GONE
     */
    void setVisibility(int visibility);

    /**
     * @return The current visibility (VISIBLE, INVISIBLE, or GONE)
     */
    int getVisibility();

    // ==================== State ====================

    /**
     * Sets whether this view is enabled for interaction.
     *
     * @param enabled True to enable, false to disable
     */
    void setEnabled(boolean enabled);

    /**
     * @return Whether this view is enabled
     */
    boolean isEnabled();

    // ==================== Background ====================

    /**
     * Sets the background drawable.
     *
     * @param drawable The native drawable object
     */
    void setBackground(@Nullable Object drawable);

    /**
     * Sets a solid background color.
     *
     * @param color The color in ARGB format
     */
    void setBackgroundColor(int color);

    // ==================== Events ====================

    /**
     * Sets a click listener for this view.
     *
     * @param listener The click callback, or null to remove
     */
    void setOnClickListener(@Nullable Runnable listener);

    // ==================== Native Access ====================

    /**
     * @return The native Modern UI View object
     */
    Object getNativeView();
}
