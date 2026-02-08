package goblinbob.mobends.api.gui.modernui;

import goblinbob.mobends.api.gui.modernui.view.IMuiView;

import javax.annotation.Nullable;

/**
 * Provides theming and styling for Modern UI components.
 * Abstracts theme differences between Modern UI versions.
 */
public interface IThemeProvider
{
    /**
     * Applies the primary theme color to a view.
     *
     * @param view The view to style
     * @param color The color in ARGB format
     */
    void applyPrimaryColor(IMuiView view, int color);

    /**
     * Applies a background color to a view.
     *
     * @param view The view to style
     * @param color The color in ARGB format
     */
    void applyBackgroundColor(IMuiView view, int color);

    /**
     * Applies text color to a view (if applicable).
     *
     * @param view The view to style
     * @param color The color in ARGB format
     */
    void applyTextColor(IMuiView view, int color);

    /**
     * Creates a themed background drawable.
     *
     * @param style The theme style to apply
     * @return A drawable object, or null if not supported
     */
    @Nullable
    Object createThemedBackground(ThemeStyle style);

    /**
     * Creates a rounded rectangle drawable.
     *
     * @param color The fill color in ARGB format
     * @param cornerRadius The corner radius in dp
     * @return A drawable object
     */
    Object createRoundRectDrawable(int color, float cornerRadius);

    /**
     * Creates a gradient drawable.
     *
     * @param startColor The start color in ARGB format
     * @param endColor The end color in ARGB format
     * @param orientation The gradient orientation
     * @return A drawable object
     */
    Object createGradientDrawable(int startColor, int endColor, GradientOrientation orientation);

    /**
     * Theme styles for common UI elements.
     */
    enum ThemeStyle
    {
        PANEL,
        BUTTON,
        BUTTON_HOVER,
        TOGGLE_ON,
        TOGGLE_OFF,
        LIST_ITEM,
        LIST_ITEM_SELECTED,
        SCROLL_BAR,
        TEXT_FIELD
    }

    /**
     * Gradient orientations.
     */
    enum GradientOrientation
    {
        TOP_BOTTOM,
        BOTTOM_TOP,
        LEFT_RIGHT,
        RIGHT_LEFT
    }
}
