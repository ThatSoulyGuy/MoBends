package goblinbob.mobends.core.client.gui.modernui.widget;

import com.mojang.logging.LogUtils;
import goblinbob.mobends.api.gui.modernui.ILayoutParams;
import goblinbob.mobends.api.gui.modernui.IViewFactory;
import goblinbob.mobends.api.gui.modernui.view.*;
import goblinbob.mobends.core.client.gui.modernui.theme.MoBendsTheme;
import goblinbob.mobends.core.pack.IBendsPack;
import goblinbob.mobends.core.pack.InvalidPackFormatException;
import goblinbob.mobends.core.pack.PackManager;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Modern UI pack list widget.
 * Displays a scrollable list of bends packs with toggle switches for activation.
 */
public class MuiPackListView
{
    private static final Logger LOG = LogUtils.getLogger();
    private static final int ITEM_HEIGHT = 48;
    private static final int THUMBNAIL_SIZE = 32;

    private final IViewFactory factory;
    private final IMuiScrollView scrollView;
    private final IMuiLinearLayout contentLayout;
    private final List<PackItemInfo> items;

    @Nullable
    private PackItemInfo selectedItem;
    @Nullable
    private Consumer<IBendsPack> onPackSelected;

    public MuiPackListView(IViewFactory factory)
    {
        this.factory = factory;
        this.items = new ArrayList<>();

        this.scrollView = factory.createScrollView();
        this.scrollView.setLayoutParams(factory.createMatchParent());
        this.scrollView.setBackgroundColor(MoBendsTheme.BG_LIST);

        this.contentLayout = factory.createLinearLayout(IViewFactory.VERTICAL);
        this.contentLayout.setLayoutParams(factory.createLayoutParams(
                ILayoutParams.MATCH_PARENT,
                ILayoutParams.WRAP_CONTENT
        ));
        this.contentLayout.setPadding(0, MoBendsTheme.PADDING_SMALL, 0, MoBendsTheme.PADDING_SMALL);

        scrollView.addView(contentLayout, factory.createMatchParent());
    }

    public void setOnPackSelected(Consumer<IBendsPack> callback)
    {
        this.onPackSelected = callback;
    }

    public void populateFromManager()
    {
        clear();

        Collection<? extends IBendsPack> localPacks = PackManager.INSTANCE.getLocalPacks();
        Collection<IBendsPack> appliedPacks = PackManager.INSTANCE.getAppliedPacks();

        for (IBendsPack pack : localPacks)
        {
            boolean isApplied = appliedPacks.stream()
                    .anyMatch(p -> p.getKey().equals(pack.getKey()));
            addPack(pack, isApplied);
        }
    }

    public void addPack(IBendsPack pack, boolean applied)
    {
        PackItemInfo item = createPackItem(pack, applied);
        items.add(item);

        ILayoutParams params = factory.createLayoutParams(
                ILayoutParams.MATCH_PARENT,
                ITEM_HEIGHT
        );
        params.setMargins(MoBendsTheme.PADDING_SMALL, MoBendsTheme.PADDING_SMALL,
                         MoBendsTheme.PADDING_SMALL, 0);
        contentLayout.addView(item.rootLayout, params);
    }

    public void clear()
    {
        contentLayout.removeAllViews();
        items.clear();
        selectedItem = null;
    }

    public void filter(String query)
    {
        String lowerQuery = query.toLowerCase();

        for (PackItemInfo item : items)
        {
            boolean matches = query.isEmpty() ||
                    item.pack.getDisplayName().toLowerCase().contains(lowerQuery) ||
                    item.pack.getAuthor().toLowerCase().contains(lowerQuery);

            item.rootLayout.setVisibility(matches ? IMuiView.VISIBLE : IMuiView.GONE);
        }
    }

    public void selectPack(@Nullable IBendsPack pack)
    {
        if (selectedItem != null)
        {
            updateItemSelection(selectedItem, false);
        }

        selectedItem = null;
        if (pack != null)
        {
            for (PackItemInfo item : items)
            {
                if (item.pack == pack || item.pack.getKey().equals(pack.getKey()))
                {
                    selectedItem = item;
                    updateItemSelection(item, true);
                    break;
                }
            }
        }
    }

    @Nullable
    public IBendsPack getSelectedPack()
    {
        return selectedItem != null ? selectedItem.pack : null;
    }

    public IMuiView getView()
    {
        return scrollView;
    }

    // ==================== Private Methods ====================

    private PackItemInfo createPackItem(IBendsPack pack, boolean applied)
    {
        // Root layout (horizontal: accent | info | toggle)
        IMuiLinearLayout rootLayout = factory.createLinearLayout(IViewFactory.HORIZONTAL);
        rootLayout.setBackgroundColor(MoBendsTheme.BG_LIST_ITEM);
        rootLayout.setGravity(IMuiLinearLayout.GRAVITY_CENTER_VERTICAL);
        rootLayout.setPadding(0, 0, MoBendsTheme.PADDING, 0);

        // Accent bar (left edge, shows applied status)
        IMuiView accentBar = factory.createView();
        accentBar.setBackgroundColor(applied ? MoBendsTheme.TOGGLE_ON : MoBendsTheme.TOGGLE_OFF);
        accentBar.setLayoutParams(factory.createLayoutParams(3, ILayoutParams.MATCH_PARENT));

        // Info container (vertical: name, author/description)
        IMuiLinearLayout infoLayout = factory.createLinearLayout(IViewFactory.VERTICAL);
        infoLayout.setGravity(IMuiLinearLayout.GRAVITY_CENTER_VERTICAL);
        ILayoutParams infoParams = factory.createLayoutParams(0, ILayoutParams.MATCH_PARENT, 1.0f);
        infoParams.setMargins(MoBendsTheme.PADDING, 0, MoBendsTheme.PADDING, 0);

        // Pack name
        IMuiTextView nameView = factory.createTextView(pack.getDisplayName());
        nameView.setTextColor(MoBendsTheme.TEXT_PRIMARY);
        nameView.setTextSize(13);
        nameView.setBold(true);

        // Pack author
        IMuiTextView authorView = factory.createTextView("by " + pack.getAuthor());
        authorView.setTextColor(MoBendsTheme.TEXT_SECONDARY);
        authorView.setTextSize(10);

        infoLayout.addView(nameView, factory.createLayoutParams(
                ILayoutParams.WRAP_CONTENT, ILayoutParams.WRAP_CONTENT));
        infoLayout.addView(authorView, factory.createLayoutParams(
                ILayoutParams.WRAP_CONTENT, ILayoutParams.WRAP_CONTENT));

        // Toggle switch for apply/remove
        IMuiToggle toggle = factory.createToggle(applied);
        toggle.setOnCheckedChangeListener(checked -> onPackToggled(pack, checked, accentBar));

        // Assemble
        rootLayout.addView(accentBar, factory.createLayoutParams(3, ILayoutParams.MATCH_PARENT));
        rootLayout.addView(infoLayout, infoParams);
        rootLayout.addView(toggle, factory.createLayoutParams(
                ILayoutParams.WRAP_CONTENT, ILayoutParams.WRAP_CONTENT));

        PackItemInfo itemInfo = new PackItemInfo(pack, rootLayout, accentBar, nameView, authorView, toggle, applied);

        // Click to select (show details), toggle to activate/deactivate
        rootLayout.setOnClickListener(() -> onItemClicked(itemInfo));

        return itemInfo;
    }

    private void onPackToggled(IBendsPack pack, boolean apply, IMuiView accentBar)
    {
        // Update the accent bar color
        accentBar.setBackgroundColor(apply ? MoBendsTheme.TOGGLE_ON : MoBendsTheme.TOGGLE_OFF);

        // Update the item's applied state
        for (PackItemInfo item : items)
        {
            if (item.pack.getKey().equals(pack.getKey()))
            {
                item.applied = apply;
                break;
            }
        }

        // Collect all currently applied pack keys
        List<String> appliedKeys = items.stream()
                .filter(item -> item.applied)
                .map(item -> item.pack.getKey())
                .collect(Collectors.toList());

        // Apply to PackManager
        try
        {
            PackManager.INSTANCE.setAppliedPacks(appliedKeys, true);
            LOG.info("Pack '{}' {}", pack.getDisplayName(), apply ? "activated" : "deactivated");
        }
        catch (InvalidPackFormatException e)
        {
            LOG.error("Failed to apply pack '{}': {}", pack.getDisplayName(), e.getMessage());
            // Revert the toggle state on failure
            for (PackItemInfo item : items)
            {
                if (item.pack.getKey().equals(pack.getKey()))
                {
                    item.applied = !apply;
                    item.toggle.setChecked(!apply);
                    accentBar.setBackgroundColor(!apply ? MoBendsTheme.TOGGLE_ON : MoBendsTheme.TOGGLE_OFF);
                    break;
                }
            }
        }
    }

    private void onItemClicked(PackItemInfo item)
    {
        if (selectedItem != item)
        {
            if (selectedItem != null)
            {
                updateItemSelection(selectedItem, false);
            }
            selectedItem = item;
            updateItemSelection(item, true);

            if (onPackSelected != null)
            {
                onPackSelected.accept(item.pack);
            }
        }
    }

    private void updateItemSelection(PackItemInfo item, boolean selected)
    {
        item.rootLayout.setBackgroundColor(
                selected ? MoBendsTheme.BG_LIST_ITEM_SELECTED : MoBendsTheme.BG_LIST_ITEM
        );
    }

    // ==================== Inner Classes ====================

    private static class PackItemInfo
    {
        final IBendsPack pack;
        final IMuiLinearLayout rootLayout;
        final IMuiView accentBar;
        final IMuiTextView nameView;
        final IMuiTextView authorView;
        final IMuiToggle toggle;
        boolean applied;

        PackItemInfo(IBendsPack pack, IMuiLinearLayout rootLayout, IMuiView accentBar,
                    IMuiTextView nameView, IMuiTextView authorView, IMuiToggle toggle,
                    boolean applied)
        {
            this.pack = pack;
            this.rootLayout = rootLayout;
            this.accentBar = accentBar;
            this.nameView = nameView;
            this.authorView = authorView;
            this.toggle = toggle;
            this.applied = applied;
        }
    }
}
