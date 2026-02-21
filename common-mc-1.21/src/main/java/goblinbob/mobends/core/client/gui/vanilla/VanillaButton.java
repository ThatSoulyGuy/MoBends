package goblinbob.mobends.core.client.gui.vanilla;

import goblinbob.mobends.api.gui.ILayoutParams;
import goblinbob.mobends.api.gui.view.IButton;
import goblinbob.mobends.core.client.gui.theme.MoBendsTheme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public class VanillaButton extends VanillaView implements IButton
{
    private String text;
    private int textColor = MoBendsTheme.TEXT_PRIMARY;

    public VanillaButton(String text)
    {
        this.text = text;
        this.backgroundColor = MoBendsTheme.BG_BUTTON;
    }

    @Override
    public void setText(String text) { this.text = text; }

    @Override
    public String getText() { return text; }

    @Override
    public void setTextColor(int color) { this.textColor = color; }

    @Override
    public void setTextSize(float sizeSp) { /* MC font doesn't support runtime size changes */ }

    @Override
    public void setIcon(Object drawable) { /* No-op */ }

    @Override
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

        if (text != null && !text.isEmpty())
        {
            var font = Minecraft.getInstance().font;
            int textW = font.width(text);
            int textX = x + (measuredWidth - textW) / 2;
            int textY = y + (measuredHeight - font.lineHeight) / 2;
            guiGraphics.drawString(font, text, textX, textY, textColor, true);
        }
    }

    @Override
    public void measure(int availableWidth, int availableHeight)
    {
        int lpW = layoutParams != null ? layoutParams.getWidth() : ILayoutParams.WRAP_CONTENT;
        int lpH = layoutParams != null ? layoutParams.getHeight() : ILayoutParams.WRAP_CONTENT;

        var font = Minecraft.getInstance().font;
        int contentW = (text != null ? font.width(text) : 0) + paddingLeft + paddingRight + 16;
        int contentH = font.lineHeight + paddingTop + paddingBottom + 8;

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
