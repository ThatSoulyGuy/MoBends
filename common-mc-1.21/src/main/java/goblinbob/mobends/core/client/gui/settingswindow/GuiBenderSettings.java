package goblinbob.mobends.core.client.gui.settingswindow;

import com.mojang.blaze3d.systems.RenderSystem;
import goblinbob.mobends.core.bender.EntityBender;
import goblinbob.mobends.core.client.gui.elements.GuiSmallToggleButton;
import goblinbob.mobends.core.client.gui.elements.IGuiListElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public class GuiBenderSettings implements IGuiListElement
{

    private final EntityBender<?> bender;
    private final Minecraft mc;
    private final GuiSmallToggleButton toggleButton;

    private int x, y;
    private int listOrder;

    public GuiBenderSettings(EntityBender<?> bender)
    {
        this.bender = bender;
        this.mc = Minecraft.getInstance();
        this.toggleButton = new GuiSmallToggleButton();
        this.toggleButton.setToggleState(bender.isAnimated());
    }

    public void initGui(int x, int y)
    {
        this.x = x;
        this.y = y;
        this.toggleButton.initGui(x + 4, y + 4);
    }

    @Override
    public boolean handleMouseClicked(int mouseX, int mouseY, int mouseButton)
    {
        if (toggleButton.mouseClicked(mouseX, mouseY, mouseButton))
        {
            bender.setAnimate(toggleButton.getToggleState());
            return true;
        }

        return false;
    }

    public void update(int mouseX, int mouseY)
    {
        toggleButton.update(mouseX, mouseY);
    }

    public void draw(GuiGraphics guiGraphics, float partialTicks)
    {
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);

        guiGraphics.drawString(mc.font, bender.getLocalizedName(), this.x + 38, this.y + 10, 0xffffff, true);

        toggleButton.draw(guiGraphics);
    }

    @Override
    public int getX()
    {
        return x;
    }

    @Override
    public int getY()
    {
        return y;
    }

    @Override
    public int getHeight()
    {
        return 20;
    }

    @Override
    public int getOrder()
    {
        return listOrder;
    }

    @Override
    public void setOrder(int order)
    {
        listOrder = order;
    }

}
