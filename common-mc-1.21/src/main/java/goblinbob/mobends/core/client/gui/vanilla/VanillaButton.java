package goblinbob.mobends.core.client.gui.vanilla;

import goblinbob.mobends.core.client.gui.theme.MoBendsTheme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

public class VanillaButton extends VanillaView
{
    private String text;
    private int textColor = MoBendsTheme.TEXT_PRIMARY;
    private float textScale = 1.0f;
    @Nullable
    private ResourceLocation icon;

    public VanillaButton(String text)
    {
        this.text = text;
        this.backgroundColor = MoBendsTheme.BG_BUTTON;
    }

    public void setText(String text) { this.text = text; }

    public String getText() { return text; }

    public void setTextColor(int color) { this.textColor = color; }

    public void setTextSize(float sizeSp) { this.textScale = sizeSp / 14.0f; }

    public void setIcon(Object drawable)
    {
        this.icon = (drawable instanceof ResourceLocation rl) ? rl : null;
    }

    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick)
    {
        if (visibility != VISIBLE) return;

        boolean hovered = isInBounds(mouseX, mouseY) && enabled;
        int bgColor;
        if (!enabled)
        {
            bgColor = MoBendsTheme.BG_BUTTON_DISABLED;
        }
        else if (backgroundColor != 0 && backgroundColor != MoBendsTheme.BG_BUTTON)
        {
            bgColor = hovered ? brighten(backgroundColor) : backgroundColor;
        }
        else
        {
            bgColor = hovered ? MoBendsTheme.BG_BUTTON_HOVER : MoBendsTheme.BG_BUTTON;
        }

        guiGraphics.fill(x, y, x + measuredWidth, y + measuredHeight, bgColor);

        // Border so buttons (especially disabled ones) read as distinct elements.
        int borderColor = enabled ? MoBendsTheme.BORDER : MoBendsTheme.BG_BUTTON_PRESSED;
        guiGraphics.fill(x, y, x + measuredWidth, y + 1, borderColor);
        guiGraphics.fill(x, y + measuredHeight - 1, x + measuredWidth, y + measuredHeight, borderColor);
        guiGraphics.fill(x, y, x + 1, y + measuredHeight, borderColor);
        guiGraphics.fill(x + measuredWidth - 1, y, x + measuredWidth, y + measuredHeight, borderColor);

        // Optional leading icon.
        int leftOffset = paddingLeft;
        if (icon != null)
        {
            int iconSize = Math.min(16, measuredHeight - 6);
            if (iconSize > 0)
            {
                guiGraphics.blit(icon, x + 4, y + (measuredHeight - iconSize) / 2, 0, 0, iconSize, iconSize);
                leftOffset = 4 + iconSize + 4;
            }
        }

        if (text != null && !text.isEmpty())
        {
            var font = Minecraft.getInstance().font;
            int color = enabled ? textColor : MoBendsTheme.TEXT_DISABLED;
            int textW = (int) (font.width(text) * textScale);
            int textH = (int) (font.lineHeight * textScale);

            int regionLeft = x + leftOffset;
            int regionRight = x + measuredWidth - paddingRight;
            int textX = regionLeft + Math.max(0, (regionRight - regionLeft - textW) / 2);
            int textY = y + (measuredHeight - textH) / 2;

            if (textScale != 1.0f)
            {
                guiGraphics.pose().pushPose();
                guiGraphics.pose().scale(textScale, textScale, 1.0f);
                guiGraphics.drawString(font, text, (int) (textX / textScale), (int) (textY / textScale), color, true);
                guiGraphics.pose().popPose();
            }
            else
            {
                guiGraphics.drawString(font, text, textX, textY, color, true);
            }
        }
    }

    public void measure(int availableWidth, int availableHeight)
    {
        int lpW = layoutParams != null ? layoutParams.getWidth() : VanillaLayoutParams.WRAP_CONTENT;
        int lpH = layoutParams != null ? layoutParams.getHeight() : VanillaLayoutParams.WRAP_CONTENT;

        var font = Minecraft.getInstance().font;
        int textW = (text != null ? (int) (font.width(text) * textScale) : 0);
        int iconW = icon != null ? 20 : 0;
        int contentW = textW + iconW + paddingLeft + paddingRight + 16;
        int contentH = (int) (font.lineHeight * textScale) + paddingTop + paddingBottom + 8;

        measuredWidth = resolveSize(lpW, availableWidth, Math.max(contentW, minWidth));
        measuredHeight = resolveSize(lpH, availableHeight, Math.max(contentH, minHeight));
    }

    private static int brighten(int color)
    {
        int a = (color >>> 24) & 0xFF;
        int r = Math.min(255, ((color >> 16) & 0xFF) + 20);
        int g = Math.min(255, ((color >> 8) & 0xFF) + 20);
        int b = Math.min(255, (color & 0xFF) + 20);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
