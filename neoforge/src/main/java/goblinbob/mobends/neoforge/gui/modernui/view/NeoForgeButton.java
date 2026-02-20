package goblinbob.mobends.neoforge.gui.modernui.view;

import goblinbob.mobends.api.gui.view.IButton;
import icyllis.modernui.graphics.drawable.Drawable;
import icyllis.modernui.widget.Button;

/**
 * NeoForge wrapper for Modern UI Button.
 */
public class NeoForgeButton extends NeoForgeView implements IButton
{
    private final Button nativeButton;

    public NeoForgeButton(Button nativeView)
    {
        super(nativeView);
        this.nativeButton = nativeView;
    }

    @Override
    public void setText(String text)
    {
        nativeButton.setText(text);
    }

    @Override
    public String getText()
    {
        CharSequence text = nativeButton.getText();
        return text != null ? text.toString() : "";
    }

    @Override
    public void setTextColor(int color)
    {
        nativeButton.setTextColor(color);
    }

    @Override
    public void setTextSize(float sizeSp)
    {
        nativeButton.setTextSize(sizeSp);
    }

    @Override
    public void setIcon(Object drawable)
    {
        if (drawable instanceof Drawable d)
        {
            nativeButton.setCompoundDrawablesWithIntrinsicBounds(d, null, null, null);
        }
    }
}
