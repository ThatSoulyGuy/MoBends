package goblinbob.mobends.forge.gui.modernui.view;

import goblinbob.mobends.api.gui.modernui.view.IMuiButton;
import icyllis.modernui.graphics.drawable.Drawable;
import icyllis.modernui.widget.Button;

/**
 * Forge wrapper for Modern UI Button.
 */
public class ForgeButton extends ForgeView implements IMuiButton
{
    private final Button nativeButton;

    public ForgeButton(Button nativeView)
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
            // Modern UI Button may support compound drawables
            // This is a simplified implementation
            nativeButton.setCompoundDrawablesWithIntrinsicBounds(d, null, null, null);
        }
    }
}
