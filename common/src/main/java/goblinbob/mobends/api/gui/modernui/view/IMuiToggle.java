package goblinbob.mobends.api.gui.modernui.view;

import java.util.function.Consumer;

/**
 * Abstraction for Modern UI Switch/CheckBox.
 * A toggleable on/off control.
 */
public interface IMuiToggle extends IMuiView
{
    /**
     * Sets the checked/toggled state.
     *
     * @param checked True for on, false for off
     */
    void setChecked(boolean checked);

    /**
     * @return Whether the toggle is currently checked/on
     */
    boolean isChecked();

    /**
     * Toggles the state.
     */
    void toggle();

    /**
     * Sets a listener for state changes.
     *
     * @param listener Callback receiving the new checked state, or null to remove
     */
    void setOnCheckedChangeListener(Consumer<Boolean> listener);

    /**
     * Sets the text label shown next to the toggle.
     *
     * @param text The label text, or null for no label
     */
    void setText(String text);

    /**
     * @return The current label text
     */
    String getText();
}
