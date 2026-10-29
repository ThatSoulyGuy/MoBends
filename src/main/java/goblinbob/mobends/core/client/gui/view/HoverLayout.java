package goblinbob.mobends.core.client.gui.view;

import goblinbob.mobends.core.client.gui.theme.MoBendsTheme;
import net.minecraft.client.gui.GuiGraphics;

public class HoverLayout extends LinearLayout
{
    public HoverLayout(int orientation)
    {
        super(orientation);
    }

    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick)
    {
        if (visibility != VISIBLE) return;

        if (backgroundColor != 0)
        {
            guiGraphics.fill(x, y, x + measuredWidth, y + measuredHeight, backgroundColor);
        }

        if (clickListener != null && enabled && isInBounds(mouseX, mouseY))
        {
            guiGraphics.fill(x, y, x + measuredWidth, y + measuredHeight, MoBendsTheme.BG_LIST_ITEM_HOVER);
        }

        for (View child : children)
        {
            child.render(guiGraphics, mouseX, mouseY, partialTick);
        }
    }
}
