package goblinbob.mobends.core.client.gui.elements;

import net.minecraft.client.gui.GuiGraphics;

public interface IGuiListElement
{

    void initGui(int x, int y);

    boolean handleMouseClicked(int mouseX, int mouseY, int state);

    void update(int mouseX, int mouseY);

    void draw(GuiGraphics guiGraphics, float partialTicks);

    int getX();

    int getY();

    int getHeight();

    int getOrder();

    void setOrder(int order);

}
