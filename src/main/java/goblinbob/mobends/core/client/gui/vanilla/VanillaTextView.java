package goblinbob.mobends.core.client.gui.vanilla;

import goblinbob.mobends.core.client.gui.theme.MoBendsTheme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;

import java.util.Collections;
import java.util.List;

public class VanillaTextView extends VanillaView
{
    private String text;
    private int textColor = MoBendsTheme.TEXT_PRIMARY;
    private float textSize = 1.0f;
    private int textGravity = VanillaLayoutParams.GRAVITY_NO_GRAVITY;
    private boolean bold = false;
    private boolean italic = false;
    private int maxLines = Integer.MAX_VALUE;

    private List<FormattedCharSequence> lines = Collections.emptyList();

    public VanillaTextView(String text)
    {
        this.text = text;
    }

    public void setText(String text) { this.text = text; }

    public String getText() { return text; }

    public void setTextColor(int color) { this.textColor = color; }

    public void setTextSize(float sizeSp) { this.textSize = sizeSp / 14.0f; }

    public void setGravity(int gravity) { this.textGravity = gravity; }

    public void setBold(boolean bold) { this.bold = bold; }

    public void setItalic(boolean italic) { this.italic = italic; }

    public void setMaxLines(int maxLines) { this.maxLines = maxLines <= 0 ? Integer.MAX_VALUE : maxLines; }

    public void setTextIsSelectable(boolean selectable) {  }

    private List<FormattedCharSequence> buildLines(int wrapWidthFontSpace)
    {
        if (text == null || text.isEmpty()) return Collections.emptyList();

        Font font = Minecraft.getInstance().font;
        Component styled = Component.literal(text).setStyle(Style.EMPTY.withBold(bold).withItalic(italic));
        int width = wrapWidthFontSpace > 0 ? wrapWidthFontSpace : Integer.MAX_VALUE;

        List<FormattedCharSequence> wrapped = font.split(styled, width);
        if (wrapped.size() > maxLines)
        {
            wrapped = wrapped.subList(0, maxLines);
        }
        return wrapped;
    }

    public void measure(int availableWidth, int availableHeight)
    {
        int lpW = layoutParams != null ? layoutParams.getWidth() : VanillaLayoutParams.WRAP_CONTENT;
        int lpH = layoutParams != null ? layoutParams.getHeight() : VanillaLayoutParams.WRAP_CONTENT;

        Font font = Minecraft.getInstance().font;
        int horizPad = paddingLeft + paddingRight;

        int wrapPixels;
        if (lpW == VanillaLayoutParams.MATCH_PARENT) wrapPixels = availableWidth - horizPad;
        else if (lpW >= 0) wrapPixels = lpW - horizPad;
        else wrapPixels = -1;
        int wrapFontSpace = wrapPixels > 0 ? (int) (wrapPixels / textSize) : -1;

        this.lines = buildLines(wrapFontSpace);

        int maxLineW = 0;
        for (FormattedCharSequence line : lines)
        {
            maxLineW = Math.max(maxLineW, font.width(line));
        }

        int contentW = (int) (maxLineW * textSize) + horizPad;
        int contentH = (int) (Math.max(1, lines.size()) * font.lineHeight * textSize) + paddingTop + paddingBottom;

        measuredWidth = resolveSize(lpW, availableWidth, Math.max(contentW, minWidth));
        measuredHeight = resolveSize(lpH, availableHeight, Math.max(contentH, minHeight));
    }

    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick)
    {
        if (visibility != VISIBLE) return;

        if (backgroundColor != 0)
        {
            int a = (int) (((backgroundColor >>> 24) & 0xFF) * alpha);
            int color = (a << 24) | (backgroundColor & 0x00FFFFFF);
            guiGraphics.fill(x, y, x + measuredWidth, y + measuredHeight, color);
        }

        if (lines.isEmpty()) return;

        Font font = Minecraft.getInstance().font;
        int lineH = font.lineHeight;
        int blockH = (int) (lines.size() * lineH * textSize);

        int verticalGravity = textGravity & 0x70;
        int startY;
        if (verticalGravity == 0x50)
        {
            startY = y + measuredHeight - paddingBottom - blockH;
        }
        else if (verticalGravity == 0x10)
        {
            startY = y + (measuredHeight - blockH) / 2;
        }
        else
        {
            startY = y + paddingTop;
        }

        int a = (int) (((textColor >>> 24) & 0xFF) * alpha);
        int color = (a << 24) | (textColor & 0x00FFFFFF);

        boolean scaled = textSize != 1.0f;
        if (scaled)
        {
            guiGraphics.pose().pushPose();
            guiGraphics.pose().scale(textSize, textSize, 1.0f);
        }

        int horizontalGravity = textGravity & 0x07;
        for (int i = 0; i < lines.size(); i++)
        {
            FormattedCharSequence line = lines.get(i);
            int lineW = (int) (font.width(line) * textSize);

            int drawX;
            if (horizontalGravity == 0x05)
            {
                drawX = x + measuredWidth - paddingRight - lineW;
            }
            else if (horizontalGravity == 0x01)
            {
                drawX = x + (measuredWidth - lineW) / 2;
            }
            else
            {
                drawX = x + paddingLeft;
            }

            int drawY = startY + (int) (i * lineH * textSize);

            if (scaled)
            {
                guiGraphics.drawString(font, line, (int) (drawX / textSize), (int) (drawY / textSize), color, true);
            }
            else
            {
                guiGraphics.drawString(font, line, drawX, drawY, color, true);
            }
        }

        if (scaled)
        {
            guiGraphics.pose().popPose();
        }
    }
}
