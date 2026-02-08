package goblinbob.mobends.api.gui.modernui.view;

/**
 * Abstraction for Modern UI Button.
 * A clickable button with text.
 */
public interface IMuiButton extends IMuiView
{
    /**
     * Sets the button text.
     *
     * @param text The text to display
     */
    void setText(String text);

    /**
     * @return The current button text
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
     * Sets an icon/drawable to display alongside the text.
     *
     * @param drawable The native drawable object, or null to remove
     */
    void setIcon(Object drawable);
}
