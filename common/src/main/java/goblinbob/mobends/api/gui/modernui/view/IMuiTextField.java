package goblinbob.mobends.api.gui.modernui.view;

import java.util.function.Consumer;

/**
 * Abstraction for Modern UI EditText/TextField.
 * A text input field.
 */
public interface IMuiTextField extends IMuiView
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
     * Sets the hint/placeholder text shown when empty.
     *
     * @param hint The hint text
     */
    void setHint(String hint);

    /**
     * @return The current hint text
     */
    String getHint();

    /**
     * Sets the text color.
     *
     * @param color The color in ARGB format
     */
    void setTextColor(int color);

    /**
     * Sets the hint text color.
     *
     * @param color The color in ARGB format
     */
    void setHintTextColor(int color);

    /**
     * Sets the text size.
     *
     * @param sizeSp The text size in sp (scaled pixels)
     */
    void setTextSize(float sizeSp);

    /**
     * Sets a listener for text changes.
     *
     * @param listener Callback receiving the new text, or null to remove
     */
    void setOnTextChangedListener(Consumer<String> listener);

    /**
     * Sets whether this is a single-line or multi-line text field.
     *
     * @param singleLine True for single line, false for multi-line
     */
    void setSingleLine(boolean singleLine);

    /**
     * Sets the maximum length of text allowed.
     *
     * @param maxLength Maximum character count, or -1 for unlimited
     */
    void setMaxLength(int maxLength);

    /**
     * Requests focus on this text field.
     */
    void requestFocus();

    /**
     * Clears focus from this text field.
     */
    void clearFocus();
}
