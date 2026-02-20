package goblinbob.mobends.api.gui.view;

import java.util.function.Consumer;

/**
 * Abstraction for a text input field.
 */
public interface ITextField extends IView
{
    void setText(String text);
    String getText();
    void setHint(String hint);
    String getHint();
    void setTextColor(int color);
    void setHintTextColor(int color);
    void setTextSize(float sizeSp);
    void setOnTextChangedListener(Consumer<String> listener);
    void setSingleLine(boolean singleLine);
    void setMaxLength(int maxLength);
    void requestFocus();
    void clearFocus();
}
