package goblinbob.mobends.api.gui.modernui.view;

/**
 * Abstraction for Modern UI TextView.
 * Displays static text labels.
 */
public interface IMuiTextView extends IMuiView
{
    /**
     * Sets the text content.
     *
     * @param text The text to display
     */
    void setText(String text);

    /**
     * @return The current text content
     */
    String getText();

    /**
     * Sets the text color.
     *
     * @param color The color in ARGB format
     */
    void setTextColor(int color);

    /**
     * Sets the text size.
     *
     * @param sizeSp The text size in sp (scaled pixels)
     */
    void setTextSize(float sizeSp);

    /**
     * Sets the text alignment/gravity.
     *
     * @param gravity Gravity flags
     */
    void setGravity(int gravity);

    /**
     * Sets whether the text should be bold.
     *
     * @param bold True for bold text
     */
    void setBold(boolean bold);

    /**
     * Sets whether the text should be italic.
     *
     * @param italic True for italic text
     */
    void setItalic(boolean italic);

    /**
     * Sets the maximum number of lines to display.
     *
     * @param maxLines Maximum line count, or Integer.MAX_VALUE for unlimited
     */
    void setMaxLines(int maxLines);

    /**
     * Sets whether text should be selectable by the user.
     *
     * @param selectable True to allow selection
     */
    void setTextIsSelectable(boolean selectable);
}
