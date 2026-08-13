package goblinbob.mobends.core.client.gui.vanilla;

import goblinbob.mobends.core.client.gui.theme.MoBendsTheme;
import net.minecraft.client.gui.GuiGraphics;

import javax.annotation.Nullable;

public class VanillaTileView extends VanillaLinearLayout
{
    private int bulge = 3;
    private boolean hovered;

    public boolean isHovered()
    {
        return hovered;
    }
    private boolean drawBorder = true;
    private int idleColor = MoBendsTheme.BG_LIST;

    public void setBulge(int bulge)
    {
        this.bulge = bulge;
    }

    public void setDrawBorder(boolean drawBorder)
    {
        this.drawBorder = drawBorder;
    }

    @Nullable
    private Runnable ticker;

    public void setIdleColor(int idleColor)
    {
        this.idleColor = idleColor;
    }

    public void setTicker(@Nullable Runnable ticker)
    {
        this.ticker = ticker;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick)
    {
        if (visibility != VISIBLE) return;

        if (ticker != null)
        {
            ticker.run();
        }

        hovered = enabled && isInBounds(mouseX, mouseY);

        guiGraphics.pose().pushPose();

        if (hovered)
        {
            float centerX = x + measuredWidth / 2.0F;
            float centerY = y + measuredHeight / 2.0F;
            float scale = 1.0F + bulge / 100.0F;

            guiGraphics.pose().translate(centerX, centerY, 0.0F);
            guiGraphics.pose().scale(scale, scale, 1.0F);
            guiGraphics.pose().translate(-centerX, -centerY, 0.0F);
        }

        int left = x;
        int top = y;
        int right = x + measuredWidth;
        int bottom = y + measuredHeight;

        guiGraphics.fill(left, top, right, bottom, hovered ? MoBendsTheme.BG_TILE_HOVER : idleColor);

        if (hovered && drawBorder)
        {
            guiGraphics.fill(left, top, right, top + 1, MoBendsTheme.ACCENT_PRIMARY);
            guiGraphics.fill(left, bottom - 1, right, bottom, MoBendsTheme.ACCENT_PRIMARY);
            guiGraphics.fill(left, top, left + 1, bottom, MoBendsTheme.ACCENT_PRIMARY);
            guiGraphics.fill(right - 1, top, right, bottom, MoBendsTheme.ACCENT_PRIMARY);
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);

        guiGraphics.pose().popPose();
    }
}
