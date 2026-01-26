package goblinbob.mobends.core.client.gui.elements;

import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * A horizontal tab bar that manages multiple GuiTab instances.
 * Supports tab selection and change callbacks.
 */
public class GuiTabBar
{
    private static final int TAB_GAP = 2;

    private int x;
    private int y;
    private int width;
    private final List<GuiTab> tabs;
    private GuiTab selectedTab;
    private Consumer<GuiTab> onTabChanged;

    public GuiTabBar()
    {
        this.tabs = new ArrayList<>();
        this.selectedTab = null;
    }

    public void initGui(int x, int y, int width)
    {
        this.x = x;
        this.y = y;
        this.width = width;

        // Layout tabs horizontally
        int currentX = x;
        for (GuiTab tab : tabs)
        {
            tab.initGui(currentX, y);
            currentX += tab.getWidth() + TAB_GAP;
        }
    }

    public GuiTab addTab(String labelKey, int accentColor)
    {
        GuiTab tab = new GuiTab(labelKey, accentColor);
        tabs.add(tab);

        // Auto-select first tab
        if (tabs.size() == 1)
        {
            selectTab(tab);
        }

        return tab;
    }

    public void setOnTabChanged(Consumer<GuiTab> callback)
    {
        this.onTabChanged = callback;
    }

    public void update(int mouseX, int mouseY)
    {
        for (GuiTab tab : tabs)
        {
            tab.update(mouseX, mouseY);
        }
    }

    public void draw(GuiGraphics guiGraphics, int mouseX, int mouseY)
    {
        // Draw non-selected tabs first
        for (GuiTab tab : tabs)
        {
            if (!tab.isSelected())
            {
                tab.draw(guiGraphics, mouseX, mouseY);
            }
        }

        // Draw selected tab on top
        if (selectedTab != null)
        {
            selectedTab.draw(guiGraphics, mouseX, mouseY);
        }
    }

    public boolean mouseClicked(int mouseX, int mouseY, int button)
    {
        for (GuiTab tab : tabs)
        {
            if (tab.mouseClicked(mouseX, mouseY, button))
            {
                if (tab != selectedTab)
                {
                    selectTab(tab);
                }
                return true;
            }
        }
        return false;
    }

    public void selectTab(int index)
    {
        if (index >= 0 && index < tabs.size())
        {
            selectTab(tabs.get(index));
        }
    }

    public void selectTab(GuiTab tab)
    {
        if (selectedTab != tab)
        {
            for (GuiTab t : tabs)
            {
                t.setSelected(false);
            }
            tab.setSelected(true);
            selectedTab = tab;

            if (onTabChanged != null)
            {
                onTabChanged.accept(tab);
            }
        }
    }

    public GuiTab getSelectedTab()
    {
        return selectedTab;
    }

    public int getSelectedIndex()
    {
        return tabs.indexOf(selectedTab);
    }

    public int getHeight()
    {
        return GuiTab.HEIGHT;
    }

    public List<GuiTab> getTabs()
    {
        return tabs;
    }
}
