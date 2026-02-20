package goblinbob.mobends.neoforge.gui.modernui.view;

import goblinbob.mobends.api.gui.view.ITextView;
import icyllis.modernui.widget.TextView;

/**
 * NeoForge wrapper for Modern UI TextView.
 * Uses Modern UI 3.11.x API.
 */
public class NeoForgeTextView extends NeoForgeView implements ITextView
{
    private final TextView nativeTextView;
    private boolean isBold = false;
    private boolean isItalic = false;

    public NeoForgeTextView(TextView nativeView)
    {
        super(nativeView);
        this.nativeTextView = nativeView;
    }

    @Override
    public void setText(String text)
    {
        nativeTextView.setText(text);
    }

    @Override
    public String getText()
    {
        CharSequence text = nativeTextView.getText();
        return text != null ? text.toString() : "";
    }

    @Override
    public void setTextColor(int color)
    {
        nativeTextView.setTextColor(color);
    }

    @Override
    public void setTextSize(float sizeSp)
    {
        nativeTextView.setTextSize(sizeSp);
    }

    @Override
    public void setGravity(int gravity)
    {
        nativeTextView.setGravity(gravity);
    }

    @Override
    public void setBold(boolean bold)
    {
        this.isBold = bold;
        updateTypeface();
    }

    @Override
    public void setItalic(boolean italic)
    {
        this.isItalic = italic;
        updateTypeface();
    }

    private void updateTypeface()
    {
        int style = 0;
        if (isBold) style |= 1;
        if (isItalic) style |= 2;
        nativeTextView.getPaint().setTextStyle(style);
        nativeTextView.invalidate();
    }

    @Override
    public void setMaxLines(int maxLines)
    {
        nativeTextView.setMaxLines(maxLines);
    }

    @Override
    public void setTextIsSelectable(boolean selectable)
    {
        nativeTextView.setTextIsSelectable(selectable);
    }
}
