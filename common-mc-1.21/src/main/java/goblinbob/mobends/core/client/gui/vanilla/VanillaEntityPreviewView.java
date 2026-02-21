package goblinbob.mobends.core.client.gui.vanilla;

import goblinbob.mobends.api.gui.IEntityRenderer;
import goblinbob.mobends.core.client.gui.EntityPreviewRenderer;
import net.minecraft.client.gui.GuiGraphics;

public class VanillaEntityPreviewView extends VanillaView
{
    private final IEntityRenderer renderer;
    private boolean dragging = false;

    public VanillaEntityPreviewView(IEntityRenderer renderer)
    {
        this.renderer = renderer;
    }

    @Override
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

            // Flush pending screen draws before entity rendering changes GL state
            guiGraphics.flush();

            if (renderer instanceof EntityPreviewRenderer epr)
            {
                // Use the screen's GuiGraphics for correct buffer source ordering
                epr.render(guiGraphics, x, y, measuredWidth, measuredHeight, partialTick);
            }
            else
            {
                renderer.render(x, y, measuredWidth, measuredHeight, partialTick);
            }

            // Flush entity draws before subsequent screen content
            guiGraphics.flush();
        }
    }

    @Override
    public boolean handleClick(double mouseX, double mouseY, int button)
    {
        if (visibility != VISIBLE || !enabled) return false;
        if (!isInBounds(mouseX, mouseY)) return false;
        if (button == 0)
        {
            dragging = true;
            return true;
        }
        return false;
    }

    @Override
    public boolean handleMouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY)
    {
        if (!dragging || button != 0) return false;

        float rotX = renderer.getRotationX() + (float) dragY * 0.5f;
        float rotY = renderer.getRotationY() - (float) dragX * 0.5f;
        renderer.setRotation(rotX, rotY);
        return true;
    }

    @Override
    public void handleMouseReleased(double mouseX, double mouseY, int button)
    {
        if (button == 0)
        {
            dragging = false;
        }
    }

    @Override
    public boolean handleMouseScrolled(double mouseX, double mouseY, double scrollY)
    {
        if (!isInBounds(mouseX, mouseY)) return false;

        float newScale = renderer.getScale() + (float) scrollY * 2.0f;
        renderer.setScale(newScale);
        return true;
    }
}
