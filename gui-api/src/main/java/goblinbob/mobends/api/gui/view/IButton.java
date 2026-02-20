package goblinbob.mobends.api.gui.view;

/**
 * Abstraction for a clickable button with text.
 */
public interface IButton extends IView
{
    void setText(String text);
    String getText();
    void setTextColor(int color);
    void setTextSize(float sizeSp);
    void setIcon(Object drawable);
}
