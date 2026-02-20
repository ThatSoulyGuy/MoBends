package goblinbob.mobends.core.client.gui.widget;

import goblinbob.mobends.api.gui.ILayoutParams;
import goblinbob.mobends.api.gui.IViewFactory;
import goblinbob.mobends.api.gui.view.ILinearLayout;
import goblinbob.mobends.api.gui.view.ITextView;
import goblinbob.mobends.api.gui.view.IView;
import goblinbob.mobends.core.client.gui.theme.MoBendsTheme;
import net.minecraft.client.resources.language.I18n;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Tab bar widget.
 * Uses horizontal LinearLayout with clickable text views for tabs.
 */
public class TabBarWidget
{
    private static final int TAB_GAP = 2;

    private final IViewFactory factory;
    private final ILinearLayout rootLayout;
    private final List<TabInfo> tabs;
    private int selectedIndex;
    private Consumer<Integer> onTabChanged;

    /**
     * Creates a new tab bar.
     *
     * @param factory The view factory to use for creating views
     */
    public TabBarWidget(IViewFactory factory)
    {
        this.factory = factory;
        this.tabs = new ArrayList<>();
        this.selectedIndex = -1;

        // Create horizontal layout for tabs
        this.rootLayout = factory.createLinearLayout(IViewFactory.HORIZONTAL);
        this.rootLayout.setLayoutParams(factory.createLayoutParams(
                ILayoutParams.MATCH_PARENT,
                MoBendsTheme.TAB_HEIGHT
        ));
    }

    /**
     * Adds a tab with the given label key and accent color.
     *
     * @param labelKey     The translation key for the tab label
     * @param accentColor  The accent color for the selected indicator
     * @return This tab bar for chaining
     */
    public TabBarWidget addTab(String labelKey, int accentColor)
    {
        int tabIndex = tabs.size();

        // Create tab container (vertical layout for text + indicator)
        ILinearLayout tabContainer = factory.createLinearLayout(IViewFactory.VERTICAL);

        // Create tab label
        ITextView label = factory.createTextView(I18n.get(labelKey));
        label.setTextColor(MoBendsTheme.TEXT_SECONDARY);
        label.setTextSize(14);
        label.setGravity(ILinearLayout.GRAVITY_CENTER);
        label.setPadding(MoBendsTheme.PADDING_LARGE, MoBendsTheme.PADDING_SMALL,
                        MoBendsTheme.PADDING_LARGE, MoBendsTheme.PADDING_SMALL);

        // Create selection indicator (a colored bar at bottom)
        IView indicator = factory.createView();
        indicator.setBackgroundColor(accentColor);
        indicator.setVisibility(IView.GONE);
        indicator.setLayoutParams(factory.createLayoutParams(
                ILayoutParams.MATCH_PARENT,
                3
        ));

        // Add label and indicator to container
        // Label uses weight=1 to fill remaining vertical space, pushing indicator to the bottom
        tabContainer.addView(label, factory.createLayoutParams(
                ILayoutParams.WRAP_CONTENT,
                0,
                1.0f
        ));
        tabContainer.addView(indicator, factory.createLayoutParams(
                ILayoutParams.MATCH_PARENT,
                3
        ));

        // Set click handler
        tabContainer.setOnClickListener(() -> selectTab(tabIndex));
        tabContainer.setBackgroundColor(MoBendsTheme.BG_TAB_INACTIVE);

        // Add to root layout with margin
        ILayoutParams params = factory.createLayoutParams(
                ILayoutParams.WRAP_CONTENT,
                ILayoutParams.MATCH_PARENT
        );
        params.setMargins(0, 0, TAB_GAP, 0);
        rootLayout.addView(tabContainer, params);

        // Store tab info
        TabInfo info = new TabInfo(labelKey, accentColor, tabContainer, label, indicator);
        tabs.add(info);

        // Auto-select first tab
        if (tabs.size() == 1)
        {
            selectTab(0);
        }

        return this;
    }

    /**
     * Sets the callback for tab changes.
     *
     * @param callback Called with the new tab index when selection changes
     */
    public void setOnTabChanged(Consumer<Integer> callback)
    {
        this.onTabChanged = callback;
    }

    /**
     * Selects a tab by index.
     *
     * @param index The index of the tab to select
     */
    public void selectTab(int index)
    {
        if (index < 0 || index >= tabs.size() || index == selectedIndex)
        {
            return;
        }

        // Deselect old tab
        if (selectedIndex >= 0 && selectedIndex < tabs.size())
        {
            TabInfo oldTab = tabs.get(selectedIndex);
            oldTab.label.setTextColor(MoBendsTheme.TEXT_SECONDARY);
            oldTab.indicator.setVisibility(IView.GONE);
            oldTab.container.setBackgroundColor(MoBendsTheme.BG_TAB_INACTIVE);
        }

        // Select new tab
        selectedIndex = index;
        TabInfo newTab = tabs.get(index);
        newTab.label.setTextColor(MoBendsTheme.TEXT_PRIMARY);
        newTab.indicator.setVisibility(IView.VISIBLE);
        newTab.container.setBackgroundColor(MoBendsTheme.BG_TAB_ACTIVE);

        // Notify listener
        if (onTabChanged != null)
        {
            onTabChanged.accept(index);
        }
    }

    /**
     * @return The currently selected tab index
     */
    public int getSelectedIndex()
    {
        return selectedIndex;
    }

    /**
     * @return The number of tabs
     */
    public int getTabCount()
    {
        return tabs.size();
    }

    /**
     * @return The root view that should be added to the layout
     */
    public IView getView()
    {
        return rootLayout;
    }

    /**
     * Internal class to store tab information.
     */
    private static class TabInfo
    {
        final String labelKey;
        final int accentColor;
        final ILinearLayout container;
        final ITextView label;
        final IView indicator;

        TabInfo(String labelKey, int accentColor, ILinearLayout container,
                ITextView label, IView indicator)
        {
            this.labelKey = labelKey;
            this.accentColor = accentColor;
            this.container = container;
            this.label = label;
            this.indicator = indicator;
        }
    }
}
