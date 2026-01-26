package goblinbob.mobends.core.client.gui.addonswindow;

import goblinbob.mobends.core.addon.Addons;
import goblinbob.mobends.core.addon.IAddon;
import goblinbob.mobends.core.util.ResourceLocationFactory;
import goblinbob.mobends.standard.main.ModStatics;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;

public class GuiAddonsWindow
{
    public static final ResourceLocation BACKGROUND_TEXTURE = ResourceLocationFactory.create(ModStatics.MODID,
            "textures/gui/addons_window.png");

    private static final int WIDTH = 210;
    private static final int HEIGHT = 122;
    static final int SCROLLBAR_WIDTH = 5;

    private int x, y;
    private Font font;

    public GuiAddonsWindow()
    {
        this.font = Minecraft.getInstance().font;
    }

    public void initGui(int x, int y)
    {
        this.x = x - WIDTH/2;
        this.y = y - HEIGHT/2;
    }

    public void onOpened()
    {
    }

    public void display(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks)
    {
        guiGraphics.blit(BACKGROUND_TEXTURE, this.x, this.y, 0, 0, WIDTH, HEIGHT);

        String title = I18n.get("mobends.gui.addons");
        guiGraphics.drawCenteredString(this.font, title, this.x + WIDTH/2, this.y + 4, 0xFFFFFF);

        int y = this.y + 50;
        for (IAddon addon : Addons.getRegistered()) {
            guiGraphics.drawCenteredString(this.font, addon.getDisplayName(), this.x + WIDTH/2, y, 0xFFFFFF);
            y += 50;
        }
    }

    public void update(int mouseX, int mouseY)
    {
    }

    public boolean mouseClicked(int mouseX, int mouseY, int state)
    {
        return false;
    }

    public void mouseReleased(int mouseX, int mouseY, int event)
    {

    }
}
