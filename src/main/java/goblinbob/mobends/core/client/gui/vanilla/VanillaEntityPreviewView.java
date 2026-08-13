package goblinbob.mobends.core.client.gui.vanilla;

import goblinbob.mobends.core.client.gui.EntityPreviewRenderer;
import net.minecraft.client.gui.GuiGraphics;

public class VanillaEntityPreviewView extends VanillaView
{
    private final EntityPreviewRenderer renderer;
    private boolean dragging = false;
    private boolean interactive = true;

    public VanillaEntityPreviewView(EntityPreviewRenderer renderer)
    {
        this.renderer = renderer;
    }

    public void setInteractive(boolean interactive)
    {
        this.interactive = interactive;
    }

    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick)
    {
        if (visibility != VISIBLE) return;

        if (backgroundColor != 0)
        {
            guiGraphics.fill(x, y, x + measuredWidth, y + measuredHeight, backgroundColor);
        }

        if (renderer.hasEntity())
        {
            renderer.update();

            guiGraphics.flush();

            renderer.render(guiGraphics, x, y, measuredWidth, measuredHeight, partialTick);

            guiGraphics.flush();
        }
    }

    public boolean handleClick(double mouseX, double mouseY, int button)
    {
        if (!interactive) return false;
        if (visibility != VISIBLE || !enabled) return false;
        if (!isInBounds(mouseX, mouseY)) return false;
        if (button == 0)
        {
            dragging = true;
            return true;
        }
        return false;
    }

    public boolean handleMouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY)
    {
        if (!dragging || button != 0) return false;

        float rotX = renderer.getRotationX() + (float) dragY * 0.5f;
        float rotY = renderer.getRotationY() - (float) dragX * 0.5f;
        renderer.setRotation(rotX, rotY);
        return true;
    }

    public void handleMouseReleased(double mouseX, double mouseY, int button)
    {
        if (button == 0)
        {
            dragging = false;
        }
    }

    public boolean handleMouseScrolled(double mouseX, double mouseY, double scrollY)
    {
        if (!interactive) return false;
        if (!isInBounds(mouseX, mouseY)) return false;

        float newScale = renderer.getScale() + (float) scrollY * 2.0f;
        renderer.setScale(newScale);
        return true;
    }
}
