package goblinbob.mobends.core.client.gui.settingswindow;

import com.mojang.blaze3d.systems.RenderSystem;
import goblinbob.mobends.core.client.gui.elements.GuiList;
import goblinbob.mobends.core.client.gui.packswindow.GuiPacksWindow;
import goblinbob.mobends.core.util.Draw;
import net.minecraft.client.gui.GuiGraphics;

import java.util.LinkedList;

public class GuiBenderList extends GuiList<GuiBenderSettings>
{

    private final LinkedList<GuiBenderSettings> elements;

    public GuiBenderList(int x, int y, int width, int height)
    {
        super(x, y, width, height, 5, 3, 3, 15);
        this.elements = new LinkedList<>();
    }

    @Override
    protected void drawBackground(GuiGraphics guiGraphics, float partialTicks)
    {
        RenderSystem.setShaderTexture(0, GuiPacksWindow.BACKGROUND_TEXTURE);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        Draw.borderBox(0, 0, this.width, this.height, 4, 36, 117);
    }

    @Override
    public LinkedList<GuiBenderSettings> getListElements()
    {
        return elements;
    }

    @Override
    protected int getScrollSpeed()
    {
        return 20;
    }

}
