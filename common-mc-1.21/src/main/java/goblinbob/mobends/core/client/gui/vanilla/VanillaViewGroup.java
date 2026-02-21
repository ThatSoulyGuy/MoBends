package goblinbob.mobends.core.client.gui.vanilla;

import goblinbob.mobends.api.gui.ILayoutParams;
import goblinbob.mobends.api.gui.view.IView;
import goblinbob.mobends.api.gui.view.IViewGroup;
import net.minecraft.client.gui.GuiGraphics;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class VanillaViewGroup extends VanillaView implements IViewGroup
{
    protected final List<VanillaView> children = new ArrayList<>();

    @Override
    public void addView(IView child)
    {
        if (child instanceof VanillaView vv)
        {
            children.add(vv);
        }
    }

    @Override
    public void addView(IView child, ILayoutParams params)
    {
        child.setLayoutParams(params);
        addView(child);
    }

    @Override
    public void addView(IView child, int index)
    {
        if (child instanceof VanillaView vv)
        {
            children.add(index, vv);
        }
    }

    @Override
    public void removeView(IView child)
    {
        if (child instanceof VanillaView vv)
        {
            children.remove(vv);
        }
    }

    @Override
    public void removeViewAt(int index)
    {
        children.remove(index);
    }

    @Override
    public void removeAllViews()
    {
        children.clear();
    }

    @Override
    public int getChildCount()
    {
        return children.size();
    }

    @Override
    public IView getChildAt(int index)
    {
        return children.get(index);
    }

    @Nullable
    @Override
    public IView findViewById(int id)
    {
        if (this.id == id) return this;
        for (VanillaView child : children)
        {
            if (child.id == id) return child;
            if (child instanceof VanillaViewGroup vg)
            {
                IView found = vg.findViewById(id);
                if (found != null) return found;
            }
        }
        return null;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick)
    {
        if (visibility != VISIBLE) return;
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        for (VanillaView child : children)
        {
            child.render(guiGraphics, mouseX, mouseY, partialTick);
        }
    }

    @Override
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

    @Override
    public boolean handleMouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY)
    {
        for (int i = children.size() - 1; i >= 0; i--)
        {
            if (children.get(i).handleMouseDragged(mouseX, mouseY, button, dragX, dragY)) return true;
        }
        return false;
    }

    @Override
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

    @Override
    public void handleMouseReleased(double mouseX, double mouseY, int button)
    {
        for (VanillaView child : children)
        {
            child.handleMouseReleased(mouseX, mouseY, button);
        }
    }

    @Override
    public boolean handleKeyPressed(int keyCode, int scanCode, int modifiers)
    {
        for (VanillaView child : children)
        {
            if (child.handleKeyPressed(keyCode, scanCode, modifiers)) return true;
        }
        return false;
    }

    @Override
    public boolean handleCharTyped(char ch, int modifiers)
    {
        for (VanillaView child : children)
        {
            if (child.handleCharTyped(ch, modifiers)) return true;
        }
        return false;
    }
}
