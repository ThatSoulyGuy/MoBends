package goblinbob.mobends.core.client.gui.widget;

import goblinbob.mobends.core.client.gui.vanilla.*;

import goblinbob.mobends.core.client.gui.theme.MoBendsTheme;
import net.minecraft.client.resources.language.I18n;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class TabBarWidget
{
    private static final int TAB_GAP = 2;

    private final VanillaLinearLayout rootLayout;
    private final List<TabInfo> tabs;
    private int selectedIndex;
    private Consumer<Integer> onTabChanged;

    public TabBarWidget()
    {
        this.tabs = new ArrayList<>();
        this.selectedIndex = -1;

        this.rootLayout = new VanillaLinearLayout(VanillaLinearLayout.HORIZONTAL);
        this.rootLayout.setLayoutParams(new VanillaLayoutParams(
                VanillaLayoutParams.MATCH_PARENT,
                MoBendsTheme.TAB_HEIGHT
        ));
    }

    public TabBarWidget addTab(String labelKey, int accentColor)
    {
        int tabIndex = tabs.size();

        VanillaLinearLayout tabContainer = new VanillaLinearLayout(VanillaLinearLayout.VERTICAL);

        VanillaTextView label = new VanillaTextView(I18n.get(labelKey));
        label.setTextColor(MoBendsTheme.TEXT_SECONDARY);
        label.setTextSize(14);
        label.setGravity(VanillaLayoutParams.GRAVITY_CENTER);
        label.setPadding(MoBendsTheme.PADDING_LARGE, MoBendsTheme.PADDING_SMALL,
                        MoBendsTheme.PADDING_LARGE, MoBendsTheme.PADDING_SMALL);

        VanillaView indicator = new VanillaView();
        indicator.setBackgroundColor(accentColor);
        indicator.setVisibility(VanillaView.GONE);
        indicator.setLayoutParams(new VanillaLayoutParams(
                VanillaLayoutParams.MATCH_PARENT,
                3
        ));

        tabContainer.addView(label, new VanillaLayoutParams(
                VanillaLayoutParams.WRAP_CONTENT,
                0,
                1.0f
        ));
        tabContainer.addView(indicator, new VanillaLayoutParams(
                VanillaLayoutParams.MATCH_PARENT,
                3
        ));

        tabContainer.setOnClickListener(() -> selectTab(tabIndex));
        tabContainer.setBackgroundColor(MoBendsTheme.BG_TAB_INACTIVE);

        VanillaLayoutParams params = new VanillaLayoutParams(
                VanillaLayoutParams.WRAP_CONTENT,
                VanillaLayoutParams.MATCH_PARENT
        );
        params.setMargins(0, 0, TAB_GAP, 0);
        rootLayout.addView(tabContainer, params);

        TabInfo info = new TabInfo(labelKey, accentColor, tabContainer, label, indicator);
        tabs.add(info);

        if (tabs.size() == 1)
        {
            selectTab(0);
        }

        return this;
    }

    public void setOnTabChanged(Consumer<Integer> callback)
    {
        this.onTabChanged = callback;
    }

    public void selectTab(int index)
    {
        if (index < 0 || index >= tabs.size() || index == selectedIndex)
        {
            return;
        }

        if (selectedIndex >= 0 && selectedIndex < tabs.size())
        {
            TabInfo oldTab = tabs.get(selectedIndex);
            oldTab.label.setTextColor(MoBendsTheme.TEXT_SECONDARY);
            oldTab.indicator.setVisibility(VanillaView.GONE);
            oldTab.container.setBackgroundColor(MoBendsTheme.BG_TAB_INACTIVE);
        }

        selectedIndex = index;
        TabInfo newTab = tabs.get(index);
        newTab.label.setTextColor(MoBendsTheme.TEXT_PRIMARY);
        newTab.indicator.setVisibility(VanillaView.VISIBLE);
        newTab.container.setBackgroundColor(MoBendsTheme.BG_TAB_ACTIVE);

        if (onTabChanged != null)
        {
            onTabChanged.accept(index);
        }
    }

    public VanillaView getView()
    {
        return rootLayout;
    }

    private static class TabInfo
    {
        final String labelKey;
        final int accentColor;
        final VanillaLinearLayout container;
        final VanillaTextView label;
        final VanillaView indicator;

        TabInfo(String labelKey, int accentColor, VanillaLinearLayout container,
                VanillaTextView label, VanillaView indicator)
        {
            this.labelKey = labelKey;
            this.accentColor = accentColor;
            this.container = container;
            this.label = label;
            this.indicator = indicator;
        }
    }
}
