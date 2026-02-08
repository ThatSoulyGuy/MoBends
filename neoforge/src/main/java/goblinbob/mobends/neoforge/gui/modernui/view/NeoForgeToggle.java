package goblinbob.mobends.neoforge.gui.modernui.view;

import goblinbob.mobends.api.gui.modernui.view.IMuiToggle;
import icyllis.modernui.widget.CheckBox;

import java.util.function.Consumer;

/**
 * NeoForge wrapper for Modern UI CheckBox (used as toggle).
 * Uses Modern UI 3.11.x API - Switch class doesn't exist, using CheckBox instead.
 */
public class NeoForgeToggle extends NeoForgeView implements IMuiToggle
{
    private final CheckBox nativeCheckBox;
    private Consumer<Boolean> checkedListener;

    public NeoForgeToggle(CheckBox nativeView)
    {
        super(nativeView);
        this.nativeCheckBox = nativeView;

        nativeCheckBox.setOnCheckedChangeListener((buttonView, isChecked) ->
        {
            if (checkedListener != null)
            {
                checkedListener.accept(isChecked);
            }
        });
    }

    @Override
    public void setChecked(boolean checked)
    {
        nativeCheckBox.setChecked(checked);
    }

    @Override
    public boolean isChecked()
    {
        return nativeCheckBox.isChecked();
    }

    @Override
    public void toggle()
    {
        nativeCheckBox.toggle();
    }

    @Override
    public void setOnCheckedChangeListener(Consumer<Boolean> listener)
    {
        this.checkedListener = listener;
    }

    @Override
    public void setText(String text)
    {
        nativeCheckBox.setText(text);
    }

    @Override
    public String getText()
    {
        CharSequence text = nativeCheckBox.getText();
        return text != null ? text.toString() : "";
    }
}
