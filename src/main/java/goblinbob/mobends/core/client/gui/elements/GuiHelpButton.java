package goblinbob.mobends.core.client.gui.elements;

import com.mojang.blaze3d.systems.RenderSystem;
import goblinbob.mobends.core.client.gui.GuiBendsMenu;
import goblinbob.mobends.core.util.Draw;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public class GuiHelpButton
{

    public static final int WIDTH = 20;
    public static final int HEIGHT = 20;

    protected int x, y;
    protected boolean hovered;

    public GuiHelpButton()
    {
        this.x = 0;
        this.y = 0;
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
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int textureY = hovered ? 64 : 44;
        guiGraphics.blit(GuiBendsMenu.ICONS_TEXTURE, x, y, 88, textureY, WIDTH, HEIGHT);
    }

    public boolean mouseClicked(int mouseX, int mouseY, int state)
    {
        return hovered;
    }

}
