package goblinbob.mobends.api.gui.view;

/**
 * Abstraction for a text display label.
 */
public interface ITextView extends IView
{
    void setText(String text);
    String getText();
    void setTextColor(int color);
    void setTextSize(float sizeSp);
    void setGravity(int gravity);
    void setBold(boolean bold);
    void setItalic(boolean italic);
    void setMaxLines(int maxLines);
    void setTextIsSelectable(boolean selectable);
}
