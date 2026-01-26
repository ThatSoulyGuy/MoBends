package goblinbob.mobends.core.client.gui.elements;

import com.mojang.blaze3d.systems.RenderSystem;
import goblinbob.mobends.core.util.Draw;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;

/**
 * A generic tab button widget for the new tabbed UI.
 * Supports text labels and animated selection transitions.
 */
public class GuiTab
{
    public static final int HEIGHT = 24;
    public static final int MIN_WIDTH = 60;
    public static final int PADDING = 8;

    private final String labelKey;
    private final int accentColor;
    private int x;
    private int y;
    private int width;
    private boolean hovered;
    private boolean selected;
    private float hoverTween;
    private float selectTween;

    public GuiTab(String labelKey, int accentColor)
    {
        this.labelKey = labelKey;
        this.accentColor = accentColor;
        this.hovered = false;
        this.selected = false;
        this.hoverTween = 0;
        this.selectTween = 0;
    }

    public void initGui(int x, int y)
    {
        this.x = x;
        this.y = y;

        Minecraft mc = Minecraft.getInstance();
        String label = I18n.get(labelKey);
        this.width = Math.max(MIN_WIDTH, mc.font.width(label) + PADDING * 2);
    }

    public int getWidth()
    {
        return width;
    }

    public void update(int mouseX, int mouseY)
    {
        this.hovered = mouseX >= x && mouseX <= x + width &&
                       mouseY >= y && mouseY <= y + HEIGHT;

        // Animate hover
        if (hovered)
        {
            this.hoverTween = Math.min(this.hoverTween + 0.15F, 1F);
        }
        else
        {
            this.hoverTween = Math.max(this.hoverTween - 0.1F, 0F);
        }

        // Animate selection
        if (selected)
        {
            this.selectTween = Math.min(this.selectTween + 0.2F, 1F);
        }
        else
        {
            this.selectTween = Math.max(this.selectTween - 0.15F, 0F);
        }
    }

    public void draw(GuiGraphics guiGraphics, int mouseX, int mouseY)
    {
        update(mouseX, mouseY);
        Minecraft mc = Minecraft.getInstance();

        // Background
        int bgAlpha = (int) (40 + 40 * hoverTween + 60 * selectTween);
        int bgColor = (bgAlpha << 24) | 0x000000;

        Draw.rectangle(x, y, x + width, y + HEIGHT, bgColor);

        // Selection indicator (bottom bar)
        if (selectTween > 0)
        {
            int indicatorHeight = (int) (3 * selectTween);
            int r = (accentColor >> 16) & 0xFF;
            int g = (accentColor >> 8) & 0xFF;
            int b = accentColor & 0xFF;
            int indicatorColor = (255 << 24) | (r << 16) | (g << 8) | b;
            Draw.rectangle(x, y + HEIGHT - indicatorHeight, x + width, y + HEIGHT, indicatorColor);
        }

        // Label
        String label = I18n.get(labelKey);
        int textX = x + (width - mc.font.width(label)) / 2;
        int textY = y + (HEIGHT - 8) / 2;

        int textAlpha = (int) (180 + 75 * (selected ? 1 : hoverTween));
        int textColor = (textAlpha << 24) | 0xFFFFFF;

        guiGraphics.drawString(mc.font, label, textX, textY, textColor, false);
    }

    public boolean mouseClicked(int mouseX, int mouseY, int button)
    {
        return hovered;
    }

    public void setSelected(boolean selected)
    {
        this.selected = selected;
    }

    public boolean isSelected()
    {
        return selected;
    }

    public String getLabelKey()
    {
        return labelKey;
    }
}
