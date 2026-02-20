package goblinbob.mobends.api.gui;

import goblinbob.mobends.api.gui.view.IView;

import javax.annotation.Nullable;

/**
 * Provides theming and styling for UI components.
 * Abstracts theme differences between UI backend versions.
 */
public interface IThemeProvider
{
    void applyPrimaryColor(IView view, int color);
    void applyBackgroundColor(IView view, int color);
    void applyTextColor(IView view, int color);

    @Nullable
    Object createThemedBackground(ThemeStyle style);
    Object createRoundRectDrawable(int color, float cornerRadius);
    Object createGradientDrawable(int startColor, int endColor, GradientOrientation orientation);

    enum ThemeStyle
    {
        PANEL, BUTTON, BUTTON_HOVER, TOGGLE_ON, TOGGLE_OFF,
        LIST_ITEM, LIST_ITEM_SELECTED, SCROLL_BAR, TEXT_FIELD
    }

    enum GradientOrientation
    {
        TOP_BOTTOM, BOTTOM_TOP, LEFT_RIGHT, RIGHT_LEFT
    }
}
