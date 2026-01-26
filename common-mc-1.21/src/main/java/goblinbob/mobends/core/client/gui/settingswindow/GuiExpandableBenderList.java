package goblinbob.mobends.core.client.gui.settingswindow;

import goblinbob.mobends.core.bender.EntityBender;
import goblinbob.mobends.core.client.gui.elements.GuiScrollPanel;
import net.minecraft.client.gui.GuiGraphics;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * A scrollable list of expandable bender settings.
 * Supports single selection and expansion.
 */
public class GuiExpandableBenderList extends GuiScrollPanel
{
    private static final int ELEMENT_SPACING = 3;
    private static final int PADDING = 5;

    private final List<GuiExpandableBenderSettings> elements;

    @Nullable
    private GuiExpandableBenderSettings selectedElement;
    @Nullable
    private Consumer<EntityBender<?>> onBenderSelected;
    @Nullable
    private Consumer<String> onAnimationSelected;

    private int scrollSpeed = 20;

    public GuiExpandableBenderList(int x, int y, int width, int height)
    {
        super(null, x, y, width, height);
        this.elements = new ArrayList<>();
    }

    public void initGui(int x, int y)
    {
        this.x = x;
        this.y = y;
        relayoutElements();
    }

    public void setOnBenderSelected(Consumer<EntityBender<?>> callback)
    {
        this.onBenderSelected = callback;
    }

    public void setOnAnimationSelected(Consumer<String> callback)
    {
        this.onAnimationSelected = callback;
        // Apply to existing elements
        for (GuiExpandableBenderSettings element : elements)
        {
            element.setOnAnimationSelected(callback);
        }
    }

    public void setScrollSpeed(int speed)
    {
        this.scrollSpeed = speed;
    }

    @Override
    protected int getScrollSpeed()
    {
        return scrollSpeed;
    }

    public void addElement(GuiExpandableBenderSettings element)
    {
        element.setWidth(width - PADDING * 2 - scrollBarWidth - 4);
        if (onAnimationSelected != null)
        {
            element.setOnAnimationSelected(onAnimationSelected);
        }
        elements.add(element);
        element.setOrder(elements.size() - 1);
        relayoutElements();
    }

    public void clearElements()
    {
        elements.clear();
        selectedElement = null;
        relayoutElements();
    }

    private void relayoutElements()
    {
        // Use relative positions within the scroll panel's coordinate space
        // The scroll panel's draw() method applies a translation of (x, y)
        int currentY = PADDING;
        for (GuiExpandableBenderSettings element : elements)
        {
            element.initGui(PADDING, currentY);
            currentY += element.getHeight() + ELEMENT_SPACING;
        }

        // Update content size for scrolling
        recalculateContentSize();
    }

    private void recalculateContentSize()
    {
        int totalHeight = PADDING * 2;
        for (GuiExpandableBenderSettings element : elements)
        {
            totalHeight += element.getHeight() + ELEMENT_SPACING;
        }
        this.contentSize = totalHeight;
    }

    public void notifyBenderSelected(EntityBender<?> bender)
    {
        // Deselect previous
        if (selectedElement != null)
        {
            selectedElement.setSelected(false);
        }

        // Find and select new
        for (GuiExpandableBenderSettings element : elements)
        {
            if (element.getBender() == bender)
            {
                element.setSelected(true);
                selectedElement = element;
                break;
            }
        }

        // Notify callback
        if (onBenderSelected != null)
        {
            onBenderSelected.accept(bender);
        }

        // Relayout since heights may have changed
        relayoutElements();
    }

    @Override
    public void update(int mouseX, int mouseY)
    {
        super.update(mouseX, mouseY);

        // Transform mouse position to element-local coordinates
        // Elements use relative positions within the scroll panel
        int localMouseX = mouseX - x;
        int localMouseY = mouseY - y + scrollAmount;

        for (GuiExpandableBenderSettings element : elements)
        {
            element.update(localMouseX, localMouseY);
        }

        // Check if we need to relayout (expansion changed)
        relayoutElements();
    }

    @Override
    public boolean handleMouseClicked(int mouseX, int mouseY, int button)
    {
        // Let parent handle scrollbar first
        if (super.handleMouseClicked(mouseX, mouseY, button))
        {
            return true;
        }

        if (!isInBounds(mouseX, mouseY))
        {
            return false;
        }

        // Transform mouse position to element-local coordinates
        int localMouseX = mouseX - x;
        int localMouseY = mouseY - y + scrollAmount;

        for (GuiExpandableBenderSettings element : elements)
        {
            if (element.handleMouseClicked(localMouseX, localMouseY, button))
            {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean handleMouseReleased(int mouseX, int mouseY, int button)
    {
        super.handleMouseReleased(mouseX, mouseY, button);

        // Transform mouse position to element-local coordinates
        int localMouseX = mouseX - x;
        int localMouseY = mouseY - y + scrollAmount;

        for (GuiExpandableBenderSettings element : elements)
        {
            element.handleMouseReleased(localMouseX, localMouseY, button);
        }
        return false;
    }

    @Override
    public boolean handleMouseScroll(double mouseX, double mouseY, double scrollDelta)
    {
        if (isInBounds((int) mouseX, (int) mouseY))
        {
            if (scrollDelta != 0)
            {
                int direction = scrollDelta > 0 ? -1 : 1;
                scroll(direction * getScrollSpeed());
            }
            return true;
        }
        return false;
    }

    @Override
    protected void drawContent(GuiGraphics guiGraphics, float partialTicks)
    {
        for (GuiExpandableBenderSettings element : elements)
        {
            // Only draw visible elements (using relative positions)
            int elementY = element.getY();
            int elementHeight = element.getHeight();

            // Elements use relative Y positions; check against visible scroll window
            if (elementY + elementHeight > scrollAmount && elementY < scrollAmount + height)
            {
                element.draw(guiGraphics, partialTicks);
            }
        }
    }

    private boolean isInBounds(int mouseX, int mouseY)
    {
        return mouseX >= x && mouseX < x + width &&
               mouseY >= y && mouseY < y + height;
    }

    @Override
    protected void drawBackground(GuiGraphics guiGraphics, float partialTicks)
    {
        // Background handled by parent screen
    }

    @Override
    protected int getBackgroundColor()
    {
        return 0x00000000; // Transparent
    }
}
