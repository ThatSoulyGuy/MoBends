package goblinbob.mobends.core.client.gui.elements;

import com.mojang.blaze3d.systems.RenderSystem;
import goblinbob.mobends.core.client.gui.GuiBendsMenu;
import net.minecraft.client.gui.GuiGraphics;

public class GuiIconButton
{

    public static final int WIDTH = 20;
    public static final int HEIGHT = 20;

    protected int x, y;
    protected int iconU;
    protected int iconV;
    protected int iconWidth;
    protected int iconHeight;

    protected boolean hovered;

    public GuiIconButton(int iconU, int iconV, int iconWidth, int iconHeight)
    {
        this.x = 0;
        this.y = 0;
        this.iconU = iconU;
        this.iconV = iconV;
        this.iconWidth = iconWidth;
        this.iconHeight = iconHeight;
        this.hovered = false;
    }

    public void initGui(int x, int y)
    {
        this.x = x;
        this.y = y;
    }

    public void update(int mouseX, int mouseY)
    {
        hovered = mouseX >= x && mouseX <= x + WIDTH &&
                  mouseY >= y && mouseY <= y + HEIGHT;
    }

    public void display(GuiGraphics guiGraphics)
    {
        int bgTextureY = hovered ? 64 : 44;

        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        guiGraphics.blit(GuiBendsMenu.ICONS_TEXTURE, x, y, 88, bgTextureY, WIDTH, HEIGHT);
        guiGraphics.blit(GuiBendsMenu.ICONS_TEXTURE, x + WIDTH/2 - this.iconWidth / 2, y + HEIGHT/2 - this.iconHeight / 2, this.iconU, this.iconV, this.iconWidth, this.iconHeight);
    }

    public boolean mouseClicked(int mouseX, int mouseY, int state)
    {
        return hovered;
    }

}
