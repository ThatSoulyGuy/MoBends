package goblinbob.mobends.core.client.gui.modernui.widget;

import goblinbob.mobends.api.gui.modernui.ILayoutParams;
import goblinbob.mobends.api.gui.modernui.IViewFactory;
import goblinbob.mobends.api.gui.modernui.view.*;
import goblinbob.mobends.core.bender.EntityBender;
import goblinbob.mobends.core.bender.EntityBenderRegistry;
import goblinbob.mobends.core.client.gui.modernui.theme.MoBendsTheme;
import goblinbob.mobends.core.configuration.CoreClientConfig;
import net.minecraft.client.resources.language.I18n;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

/**
 * Modern UI bender list widget.
 * Displays a scrollable list of entity benders with expandable settings.
 */
public class MuiBenderListView
{
    private static final int ITEM_HEIGHT = 32;
    private static final int EXPANDED_ANIMATION_ROW_HEIGHT = 24;

    private final IViewFactory factory;
    private final IMuiScrollView scrollView;
    private final IMuiLinearLayout contentLayout;
    private final List<BenderItemInfo> items;

    @Nullable
    private BenderItemInfo selectedItem;
    @Nullable
    private Consumer<EntityBender<?>> onBenderSelected;
    @Nullable
    private Consumer<String> onAnimationSelected;

    /**
     * Creates a new bender list view.
     *
     * @param factory The view factory
     */
    public MuiBenderListView(IViewFactory factory)
    {
        this.factory = factory;
        this.items = new ArrayList<>();

        // Create scrollable container
        this.scrollView = factory.createScrollView();
        this.scrollView.setLayoutParams(factory.createMatchParent());
        this.scrollView.setBackgroundColor(MoBendsTheme.BG_LIST);

        // Create vertical layout for items
        this.contentLayout = factory.createLinearLayout(IViewFactory.VERTICAL);
        this.contentLayout.setLayoutParams(factory.createLayoutParams(
                ILayoutParams.MATCH_PARENT,
                ILayoutParams.WRAP_CONTENT
        ));
        this.contentLayout.setPadding(0, MoBendsTheme.PADDING_SMALL, 0, MoBendsTheme.PADDING_SMALL);

        scrollView.addView(contentLayout, factory.createMatchParent());
    }

    /**
     * Sets the callback for bender selection.
     *
     * @param callback Called when a bender is selected
     */
    public void setOnBenderSelected(Consumer<EntityBender<?>> callback)
    {
        this.onBenderSelected = callback;
    }

    /**
     * Sets the callback for animation selection.
     *
     * @param callback Called when an animation is clicked for preview
     */
    public void setOnAnimationSelected(Consumer<String> callback)
    {
        this.onAnimationSelected = callback;
    }

    /**
     * Populates the list with benders from the registry.
     *
     * @param filter Optional filter for the benders
     */
    public void populateFromRegistry(@Nullable EntityBenderRegistry.Filter filter)
    {
        clear();

        Collection<EntityBender<?>> benders;
        if (filter != null)
        {
            benders = EntityBenderRegistry.instance.getRegistered(filter);
        }
        else
        {
            benders = EntityBenderRegistry.instance.getRegistered();
        }

        for (EntityBender<?> bender : benders)
        {
            addBender(bender);
        }
    }

    /**
     * Adds a bender to the list.
     *
     * @param bender The bender to add
     */
    public void addBender(EntityBender<?> bender)
    {
        BenderItemInfo item = createBenderItem(bender);
        items.add(item);

        ILayoutParams params = factory.createLayoutParams(
                ILayoutParams.MATCH_PARENT,
                ILayoutParams.WRAP_CONTENT
        );
        params.setMargins(MoBendsTheme.PADDING_SMALL, MoBendsTheme.PADDING_SMALL,
                         MoBendsTheme.PADDING_SMALL, 0);
        contentLayout.addView(item.rootLayout, params);
    }

    /**
     * Clears all items from the list.
     */
    public void clear()
    {
        contentLayout.removeAllViews();
        items.clear();
        selectedItem = null;
    }

    /**
     * Filters the list by search query.
     *
     * @param query The search query
     */
    public void filter(String query)
    {
        String lowerQuery = query.toLowerCase();

        for (BenderItemInfo item : items)
        {
            boolean matches = query.isEmpty() ||
                    item.bender.getLocalizedName().toLowerCase().contains(lowerQuery);

            item.rootLayout.setVisibility(matches ? IMuiView.VISIBLE : IMuiView.GONE);
        }
    }

    /**
     * Selects a bender by reference.
     *
     * @param bender The bender to select
     */
    public void selectBender(@Nullable EntityBender<?> bender)
    {
        // Deselect previous
        if (selectedItem != null)
        {
            updateItemSelection(selectedItem, false);
        }

        // Find and select new
        selectedItem = null;
        if (bender != null)
        {
            for (BenderItemInfo item : items)
            {
                if (item.bender == bender)
                {
                    selectedItem = item;
                    updateItemSelection(item, true);
                    break;
                }
            }
        }
    }

    /**
     * @return The currently selected bender, or null
     */
    @Nullable
    public EntityBender<?> getSelectedBender()
    {
        return selectedItem != null ? selectedItem.bender : null;
    }

    /**
     * @return The root view to add to the layout
     */
    public IMuiView getView()
    {
        return scrollView;
    }

    // ==================== Private Methods ====================

    private BenderItemInfo createBenderItem(EntityBender<?> bender)
    {
        // Create root layout for item (vertical for header + expandable content)
        IMuiLinearLayout rootLayout = factory.createLinearLayout(IViewFactory.VERTICAL);
        rootLayout.setBackgroundColor(MoBendsTheme.BG_LIST_ITEM);

        // Create header row (horizontal)
        IMuiLinearLayout headerLayout = factory.createLinearLayout(IViewFactory.HORIZONTAL);
        headerLayout.setLayoutParams(factory.createLayoutParams(
                ILayoutParams.MATCH_PARENT,
                ITEM_HEIGHT
        ));
        headerLayout.setPadding(MoBendsTheme.PADDING, 0, MoBendsTheme.PADDING, 0);
        headerLayout.setGravity(IMuiLinearLayout.GRAVITY_CENTER_VERTICAL);

        // Accent bar (left edge)
        IMuiView accentBar = factory.createView();
        accentBar.setBackgroundColor(bender.isAnimated() ? MoBendsTheme.TOGGLE_ON : MoBendsTheme.TOGGLE_OFF);
        accentBar.setLayoutParams(factory.createLayoutParams(3, ILayoutParams.MATCH_PARENT));

        // Entity name
        IMuiTextView nameView = factory.createTextView(bender.getLocalizedName());
        nameView.setTextColor(MoBendsTheme.TEXT_PRIMARY);
        nameView.setTextSize(14);
        ILayoutParams nameParams = factory.createLayoutParams(
                ILayoutParams.WRAP_CONTENT,
                ILayoutParams.WRAP_CONTENT
        );
        nameParams.setMargins(MoBendsTheme.PADDING, 0, 0, 0);

        // Expand indicator
        IMuiTextView expandIndicator = factory.createTextView("\u25B6"); // Right arrow
        expandIndicator.setTextColor(MoBendsTheme.TEXT_HINT);
        expandIndicator.setTextSize(12);
        ILayoutParams expandParams = factory.createLayoutParams(
                ILayoutParams.WRAP_CONTENT,
                ILayoutParams.WRAP_CONTENT
        );
        expandParams.setMargins(MoBendsTheme.PADDING, 0, MoBendsTheme.PADDING, 0);

        // Master toggle
        IMuiToggle toggle = factory.createToggle(bender.isAnimated());
        toggle.setOnCheckedChangeListener(checked -> {
            bender.setAnimate(checked);
            CoreClientConfig.getInstance().setEntityEnabled(bender.getKey(), checked);
            accentBar.setBackgroundColor(checked ? MoBendsTheme.TOGGLE_ON : MoBendsTheme.TOGGLE_OFF);
        });

        // Add views to header
        headerLayout.addView(accentBar, factory.createLayoutParams(3, ILayoutParams.MATCH_PARENT));
        headerLayout.addView(nameView, nameParams);
        // Add spacer
        IMuiView spacer = factory.createView();
        ILayoutParams spacerParams = factory.createLayoutParams(0, 0);
        spacerParams.setMargins(0, 0, 0, 0);
        // Use weight simulation - Modern UI LinearLayout supports this internally
        headerLayout.addView(spacer, factory.createLayoutParams(ILayoutParams.MATCH_PARENT, 1));
        headerLayout.addView(expandIndicator, expandParams);
        headerLayout.addView(toggle, factory.createLayoutParams(
                ILayoutParams.WRAP_CONTENT,
                ILayoutParams.WRAP_CONTENT
        ));

        // Create animations container (initially hidden)
        IMuiLinearLayout animationsLayout = factory.createLinearLayout(IViewFactory.VERTICAL);
        animationsLayout.setLayoutParams(factory.createLayoutParams(
                ILayoutParams.MATCH_PARENT,
                ILayoutParams.WRAP_CONTENT
        ));
        animationsLayout.setVisibility(IMuiView.GONE);
        animationsLayout.setPadding(MoBendsTheme.PADDING_LARGE, 0,
                                   MoBendsTheme.PADDING, MoBendsTheme.PADDING_SMALL);

        // Add animation rows
        String[] animations = bender.getSupportedAnimations();
        List<AnimationRowInfo> animationRows = new ArrayList<>();
        for (String animType : animations)
        {
            AnimationRowInfo rowInfo = createAnimationRow(animType);
            animationRows.add(rowInfo);
            animationsLayout.addView(rowInfo.layout, factory.createLayoutParams(
                    ILayoutParams.MATCH_PARENT,
                    EXPANDED_ANIMATION_ROW_HEIGHT
            ));
        }

        // Add to root
        rootLayout.addView(headerLayout, factory.createLayoutParams(
                ILayoutParams.MATCH_PARENT,
                ITEM_HEIGHT
        ));
        rootLayout.addView(animationsLayout, factory.createLayoutParams(
                ILayoutParams.MATCH_PARENT,
                ILayoutParams.WRAP_CONTENT
        ));

        // Create item info
        BenderItemInfo itemInfo = new BenderItemInfo(
                bender, rootLayout, headerLayout, accentBar,
                expandIndicator, toggle, animationsLayout, animationRows
        );

        // Set click handlers
        headerLayout.setOnClickListener(() -> onItemHeaderClicked(itemInfo));

        return itemInfo;
    }

    private AnimationRowInfo createAnimationRow(String animationType)
    {
        IMuiLinearLayout rowLayout = factory.createLinearLayout(IViewFactory.HORIZONTAL);
        rowLayout.setGravity(IMuiLinearLayout.GRAVITY_CENTER_VERTICAL);
        rowLayout.setBackgroundColor(0x00000000); // Transparent initially

        // Selection indicator
        IMuiView indicator = factory.createView();
        indicator.setBackgroundColor(MoBendsTheme.ACCENT_INFO);
        indicator.setVisibility(IMuiView.GONE);
        indicator.setLayoutParams(factory.createLayoutParams(3, ILayoutParams.MATCH_PARENT));

        // Animation name
        String labelKey = "mobends.animation." + animationType;
        String label = I18n.exists(labelKey) ? I18n.get(labelKey) : capitalizeFirst(animationType);

        IMuiTextView nameView = factory.createTextView(label);
        nameView.setTextColor(MoBendsTheme.TEXT_SECONDARY);
        nameView.setTextSize(12);
        ILayoutParams nameParams = factory.createLayoutParams(
                ILayoutParams.WRAP_CONTENT,
                ILayoutParams.WRAP_CONTENT
        );
        nameParams.setMargins(MoBendsTheme.PADDING, 0, 0, 0);

        rowLayout.addView(indicator, factory.createLayoutParams(3, ILayoutParams.MATCH_PARENT));
        rowLayout.addView(nameView, nameParams);

        AnimationRowInfo rowInfo = new AnimationRowInfo(animationType, rowLayout, indicator, nameView);

        // Click handler
        rowLayout.setOnClickListener(() -> onAnimationRowClicked(rowInfo));

        return rowInfo;
    }

    private void onItemHeaderClicked(BenderItemInfo item)
    {
        // Toggle expansion
        boolean wasExpanded = item.isExpanded;
        item.isExpanded = !wasExpanded;

        // Update UI
        item.animationsLayout.setVisibility(item.isExpanded ? IMuiView.VISIBLE : IMuiView.GONE);
        item.expandIndicator.setText(item.isExpanded ? "\u25BC" : "\u25B6"); // Down/Right arrow

        // Select this item
        if (selectedItem != item)
        {
            if (selectedItem != null)
            {
                updateItemSelection(selectedItem, false);
            }
            selectedItem = item;
            updateItemSelection(item, true);

            if (onBenderSelected != null)
            {
                onBenderSelected.accept(item.bender);
            }
        }
    }

    private void onAnimationRowClicked(AnimationRowInfo row)
    {
        // Deselect all animation rows in selected item
        if (selectedItem != null)
        {
            for (AnimationRowInfo r : selectedItem.animationRows)
            {
                updateAnimationRowSelection(r, false);
            }
        }

        // Select this row
        updateAnimationRowSelection(row, true);

        if (onAnimationSelected != null)
        {
            onAnimationSelected.accept(row.animationType);
        }
    }

    private void updateItemSelection(BenderItemInfo item, boolean selected)
    {
        item.rootLayout.setBackgroundColor(
                selected ? MoBendsTheme.BG_LIST_ITEM_SELECTED : MoBendsTheme.BG_LIST_ITEM
        );
    }

    private void updateAnimationRowSelection(AnimationRowInfo row, boolean selected)
    {
        row.indicator.setVisibility(selected ? IMuiView.VISIBLE : IMuiView.GONE);
        row.nameView.setTextColor(selected ? MoBendsTheme.TEXT_PRIMARY : MoBendsTheme.TEXT_SECONDARY);
        row.layout.setBackgroundColor(selected ? 0x20448888 : 0x00000000);
    }

    private String capitalizeFirst(String str)
    {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1).replace("_", " ");
    }

    // ==================== Inner Classes ====================

    private static class BenderItemInfo
    {
        final EntityBender<?> bender;
        final IMuiLinearLayout rootLayout;
        final IMuiLinearLayout headerLayout;
        final IMuiView accentBar;
        final IMuiTextView expandIndicator;
        final IMuiToggle toggle;
        final IMuiLinearLayout animationsLayout;
        final List<AnimationRowInfo> animationRows;
        boolean isExpanded = false;

        BenderItemInfo(EntityBender<?> bender, IMuiLinearLayout rootLayout,
                      IMuiLinearLayout headerLayout, IMuiView accentBar,
                      IMuiTextView expandIndicator, IMuiToggle toggle,
                      IMuiLinearLayout animationsLayout, List<AnimationRowInfo> animationRows)
        {
            this.bender = bender;
            this.rootLayout = rootLayout;
            this.headerLayout = headerLayout;
            this.accentBar = accentBar;
            this.expandIndicator = expandIndicator;
            this.toggle = toggle;
            this.animationsLayout = animationsLayout;
            this.animationRows = animationRows;
        }
    }

    private static class AnimationRowInfo
    {
        final String animationType;
        final IMuiLinearLayout layout;
        final IMuiView indicator;
        final IMuiTextView nameView;

        AnimationRowInfo(String animationType, IMuiLinearLayout layout,
                        IMuiView indicator, IMuiTextView nameView)
        {
            this.animationType = animationType;
            this.layout = layout;
            this.indicator = indicator;
            this.nameView = nameView;
        }
    }
}
