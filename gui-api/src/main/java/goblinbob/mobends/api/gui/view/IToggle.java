package goblinbob.mobends.api.gui.view;

import java.util.function.Consumer;

/**
 * Abstraction for a toggleable on/off control.
 */
public interface IToggle extends IView
{
    void setChecked(boolean checked);
    boolean isChecked();
    void toggle();
    void setOnCheckedChangeListener(Consumer<Boolean> listener);
    void setText(String text);
    String getText();
}
