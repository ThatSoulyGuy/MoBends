package goblinbob.mobends.forge.gui.modernui.view;

import goblinbob.mobends.api.gui.modernui.view.IMuiToggle;
import icyllis.modernui.widget.CheckBox;
import icyllis.modernui.widget.CompoundButton;

import java.util.function.Consumer;

/**
 * Forge wrapper for Modern UI CheckBox (used as toggle in 3.9.x since Switch is unavailable).
 */
public class ForgeToggle extends ForgeView implements IMuiToggle
{
    private final CheckBox nativeCheckBox;
    private Consumer<Boolean> checkedListener;

    public ForgeToggle(CheckBox nativeView)
    {
        super(nativeView);
        this.nativeCheckBox = nativeView;

        // Set up the native listener to forward to our listener
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
