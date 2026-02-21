package goblinbob.mobends.core.client.gui.vanilla;

import goblinbob.mobends.api.gui.ILayoutParams;
import goblinbob.mobends.api.gui.view.ITextView;
import goblinbob.mobends.core.client.gui.theme.MoBendsTheme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public class VanillaTextView extends VanillaView implements ITextView
{
    private String text;
    private int textColor = MoBendsTheme.TEXT_PRIMARY;
    private float textSize = 1.0f;
    private int textGravity = ILayoutParams.GRAVITY_NO_GRAVITY;
    private boolean bold = false;

    public VanillaTextView(String text)
    {
        this.text = text;
    }

    @Override
    public void setText(String text) { this.text = text; }

    @Override
    public String getText() { return text; }

    @Override
    public void setTextColor(int color) { this.textColor = color; }

    @Override
    public void setTextSize(float sizeSp) { this.textSize = sizeSp / 14.0f; }

    @Override
    public void setGravity(int gravity) { this.textGravity = gravity; }

    @Override
    public void setBold(boolean bold) { this.bold = bold; }

    @Override
    public void setItalic(boolean italic) { /* Not easily supported with MC font */ }

    @Override
    public void setMaxLines(int maxLines) { /* Not implemented */ }

    @Override
    public void setTextIsSelectable(boolean selectable) { /* Not supported */ }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick)
    {
        if (visibility != VISIBLE) return;

        if (backgroundColor != 0)
        {
            int a = (int) (((backgroundColor >>> 24) & 0xFF) * alpha);
            int color = (a << 24) | (backgroundColor & 0x00FFFFFF);
            guiGraphics.fill(x, y, x + measuredWidth, y + measuredHeight, color);
        }

        if (text == null || text.isEmpty()) return;

        var font = Minecraft.getInstance().font;
        String displayText = bold ? "\u00a7l" + text : text;
        int textW = font.width(displayText);

        // Horizontal alignment
        int textX;
        int horizontalGravity = textGravity & 0x07;
        if (horizontalGravity == 0x05)
        {
            textX = x + measuredWidth - paddingRight - textW;
        }
        else if (horizontalGravity == 0x01)
        {
            textX = x + (measuredWidth - textW) / 2;
        }
        else
        {
            textX = x + paddingLeft;
        }

        // Vertical alignment
        int textY;
        int verticalGravity = textGravity & 0x70;
        if (verticalGravity == 0x50)
        {
            textY = y + measuredHeight - paddingBottom - font.lineHeight;
        }
        else if (verticalGravity == 0x10)
        {
            textY = y + (measuredHeight - font.lineHeight) / 2;
        }
        else
        {
            textY = y + paddingTop;
        }

        // Apply alpha
        int a = (int) (((textColor >>> 24) & 0xFF) * alpha);
        int color = (a << 24) | (textColor & 0x00FFFFFF);

        if (textSize != 1.0f)
        {
            guiGraphics.pose().pushPose();
            guiGraphics.pose().scale(textSize, textSize, 1.0f);
            int scaledX = (int) (textX / textSize);
            int scaledY = (int) (textY / textSize);
            guiGraphics.drawString(font, displayText, scaledX, scaledY, color, true);
            guiGraphics.pose().popPose();
        }
        else
        {
            guiGraphics.drawString(font, displayText, textX, textY, color, true);
        }
    }

    @Override
    public void measure(int availableWidth, int availableHeight)
    {
        int lpW = layoutParams != null ? layoutParams.getWidth() : ILayoutParams.WRAP_CONTENT;
        int lpH = layoutParams != null ? layoutParams.getHeight() : ILayoutParams.WRAP_CONTENT;

        var font = Minecraft.getInstance().font;
        String displayText = bold ? "\u00a7l" + text : text;
        int textW = (text != null) ? font.width(displayText) : 0;
        int contentW = (int) (textW * textSize) + paddingLeft + paddingRight;
        int contentH = (int) (font.lineHeight * textSize) + paddingTop + paddingBottom;

        measuredWidth = resolveSize(lpW, availableWidth, Math.max(contentW, minWidth));
        measuredHeight = resolveSize(lpH, availableHeight, Math.max(contentH, minHeight));
    }
}
