package goblinbob.mobends.core.client.gui.vanilla;

import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;

public class VanillaViewGroup extends VanillaView
{
    protected final List<VanillaView> children = new ArrayList<>();

    public void addView(VanillaView child)
    {
        children.add(child);
    }

    public void addView(VanillaView child, VanillaLayoutParams params)
    {
        child.setLayoutParams(params);
        addView(child);
    }

    public void addView(VanillaView child, int index)
    {
        children.add(index, child);
    }

    public void removeAllViews()
    {
        children.clear();
    }

    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick)
    {
        if (visibility != VISIBLE) return;
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        for (VanillaView child : children)
        {
            child.render(guiGraphics, mouseX, mouseY, partialTick);
        }
    }

    public boolean handleClick(double mouseX, double mouseY, int button)
    {
        if (visibility != VISIBLE || !enabled) return false;
        if (!isInBounds(mouseX, mouseY)) return false;

        for (int i = children.size() - 1; i >= 0; i--)
        {
            if (children.get(i).handleClick(mouseX, mouseY, button)) return true;
        }

        return super.handleClick(mouseX, mouseY, button);
    }

    public boolean handleMouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY)
    {
        for (int i = children.size() - 1; i >= 0; i--)
        {
            if (children.get(i).handleMouseDragged(mouseX, mouseY, button, dragX, dragY)) return true;
        }
        return false;
    }

    public boolean handleMouseScrolled(double mouseX, double mouseY, double scrollY)
    {
        if (visibility != VISIBLE) return false;
        if (!isInBounds(mouseX, mouseY)) return false;

        for (int i = children.size() - 1; i >= 0; i--)
        {
            if (children.get(i).handleMouseScrolled(mouseX, mouseY, scrollY)) return true;
        }
        return false;
    }

    public void handleMouseReleased(double mouseX, double mouseY, int button)
    {
        for (VanillaView child : children)
        {
            child.handleMouseReleased(mouseX, mouseY, button);
        }
    }

    public boolean handleKeyPressed(int keyCode, int scanCode, int modifiers)
    {
        for (VanillaView child : children)
        {
            if (child.handleKeyPressed(keyCode, scanCode, modifiers)) return true;
        }
        return false;
    }

    public boolean handleCharTyped(char ch, int modifiers)
    {
        for (VanillaView child : children)
        {
            if (child.handleCharTyped(ch, modifiers)) return true;
        }
        return false;
    }
}
