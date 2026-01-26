package goblinbob.mobends.core.client.gui.elements;

import net.minecraft.client.gui.GuiGraphics;

import java.util.LinkedList;

public abstract class GuiElement implements IGuiElement, IGuiElementsContainer
{

    protected int x;
    protected int y;

    protected IGuiElement parent;
    protected LinkedList<IGuiElement> children;

    public GuiElement(IGuiElement parent, int x, int y)
    {
        this.parent = parent;
        this.x = x;
        this.y = y;
        this.children = new LinkedList<>();
    }

    @Override
    public void initGui()
    {
        this.children.clear();
    }

    @Override
    public IGuiElement getParent() {
        return this.parent;
    }

    @Override
    public LinkedList<IGuiElement> getElements() {
        return this.children;
    }

    @Override
    public void update(int mouseX, int mouseY)
    {
        this.updateChildren(mouseX, mouseY);
    }

    @Override
    public boolean handleMouseClicked(int mouseX, int mouseY, int button)
    {
        return this.handleMouseClickedChildren(mouseX, mouseY, button);
    }

    @Override
    public boolean handleMouseReleased(int mouseX, int mouseY, int button)
    {
        return this.handleMouseReleasedChildren(mouseX, mouseY, button);
    }

    @Override
    public void draw(GuiGraphics guiGraphics, float partialTicks)
    {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(this.getViewX(), this.getViewY(), 0);

        this.drawBackground(guiGraphics, partialTicks);
        this.drawChildren(guiGraphics, partialTicks);
        this.drawForeground(guiGraphics, partialTicks);

        guiGraphics.pose().popPose();
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

    protected abstract void drawBackground(GuiGraphics guiGraphics, float partialTicks);

    protected abstract void drawForeground(GuiGraphics guiGraphics, float partialTicks);

}
