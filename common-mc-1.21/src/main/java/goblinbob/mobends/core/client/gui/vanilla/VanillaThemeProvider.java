package goblinbob.mobends.core.client.gui.vanilla;

import goblinbob.mobends.api.gui.IThemeProvider;
import goblinbob.mobends.api.gui.view.ITextView;
import goblinbob.mobends.api.gui.view.IView;
import goblinbob.mobends.core.client.gui.theme.MoBendsTheme;

public class VanillaThemeProvider implements IThemeProvider
{
    @Override
    public void applyPrimaryColor(IView view, int color)
    {
        view.setBackgroundColor(color);
    }

    @Override
    public void applyBackgroundColor(IView view, int color)
    {
        view.setBackgroundColor(color);
    }

    @Override
    public void applyTextColor(IView view, int color)
    {
        if (view instanceof ITextView textView)
        {
            textView.setTextColor(color);
        }
    }

    @Override
    public Object createThemedBackground(ThemeStyle style)
    {
        return switch (style)
        {
            case PANEL -> MoBendsTheme.BG_PANEL;
            case BUTTON -> MoBendsTheme.BG_BUTTON;
            case BUTTON_HOVER -> MoBendsTheme.BG_BUTTON_HOVER;
            case TOGGLE_ON -> MoBendsTheme.TOGGLE_ON;
            case TOGGLE_OFF -> MoBendsTheme.TOGGLE_OFF;
            case LIST_ITEM -> MoBendsTheme.BG_LIST_ITEM;
            case LIST_ITEM_SELECTED -> MoBendsTheme.BG_LIST_ITEM_SELECTED;
            case SCROLL_BAR -> MoBendsTheme.SCROLLBAR_THUMB;
            case TEXT_FIELD -> MoBendsTheme.BG_LIST;
        };
    }

    @Override
    public Object createRoundRectDrawable(int color, float cornerRadius)
    {
        return color;
    }

    @Override
    public Object createGradientDrawable(int startColor, int endColor, GradientOrientation orientation)
    {
        return startColor;
    }
}
