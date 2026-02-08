package goblinbob.mobends.forge.gui.modernui;

import goblinbob.mobends.api.gui.modernui.IThemeProvider;
import goblinbob.mobends.api.gui.modernui.view.IMuiView;
import goblinbob.mobends.core.client.gui.modernui.theme.MoBendsTheme;
import icyllis.modernui.graphics.drawable.ShapeDrawable;
import icyllis.modernui.view.View;

import javax.annotation.Nullable;

/**
 * Forge 1.20.1 theme provider implementation.
 * Uses Modern UI 3.9.x drawable API.
 */
public class ForgeThemeProvider implements IThemeProvider
{
    @Override
    public void applyPrimaryColor(IMuiView view, int color)
    {
        View nativeView = (View) view.getNativeView();
        // Modern UI 3.9.x doesn't have setBackgroundColor, use drawable instead
        ShapeDrawable bg = new ShapeDrawable();
        bg.setColor(color);
        nativeView.setBackground(bg);
    }

    @Override
    public void applyBackgroundColor(IMuiView view, int color)
    {
        View nativeView = (View) view.getNativeView();
        // Modern UI 3.9.x doesn't have setBackgroundColor, use drawable instead
        ShapeDrawable bg = new ShapeDrawable();
        bg.setColor(color);
        nativeView.setBackground(bg);
    }

    @Override
    public void applyTextColor(IMuiView view, int color)
    {
        View nativeView = (View) view.getNativeView();
        if (nativeView instanceof icyllis.modernui.widget.TextView textView)
        {
            textView.setTextColor(color);
        }
    }

    @Nullable
    @Override
    public Object createThemedBackground(ThemeStyle style)
    {
        int color = switch (style)
        {
            case PANEL -> MoBendsTheme.BG_PANEL;
            case BUTTON -> MoBendsTheme.BG_BUTTON;
            case BUTTON_HOVER -> MoBendsTheme.BG_BUTTON_HOVER;
            case TOGGLE_ON -> MoBendsTheme.TOGGLE_ON;
            case TOGGLE_OFF -> MoBendsTheme.TOGGLE_OFF;
            case LIST_ITEM -> MoBendsTheme.BG_LIST_ITEM;
            case LIST_ITEM_SELECTED -> MoBendsTheme.BG_LIST_ITEM_SELECTED;
            case SCROLL_BAR -> MoBendsTheme.SCROLLBAR_THUMB;
            case TEXT_FIELD -> MoBendsTheme.BG_CONTENT;
        };

        ShapeDrawable drawable = new ShapeDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(MoBendsTheme.CORNER_RADIUS);
        return drawable;
    }

    @Override
    public Object createRoundRectDrawable(int color, float cornerRadius)
    {
        ShapeDrawable drawable = new ShapeDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(cornerRadius);
        return drawable;
    }

    @Override
    public Object createGradientDrawable(int startColor, int endColor, GradientOrientation orientation)
    {
        // Modern UI 3.9.x has limited gradient support
        // For now, return a solid color drawable using the start color
        ShapeDrawable drawable = new ShapeDrawable();
        drawable.setColor(startColor);
        drawable.setCornerRadius(MoBendsTheme.CORNER_RADIUS);
        return drawable;
    }
}
