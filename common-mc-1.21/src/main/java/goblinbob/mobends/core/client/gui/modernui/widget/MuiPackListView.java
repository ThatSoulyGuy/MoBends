package goblinbob.mobends.core.client.gui.modernui.widget;

import goblinbob.mobends.api.gui.modernui.ILayoutParams;
import goblinbob.mobends.api.gui.modernui.IViewFactory;
import goblinbob.mobends.api.gui.modernui.view.*;
import goblinbob.mobends.core.client.gui.modernui.theme.MoBendsTheme;
import goblinbob.mobends.core.pack.IBendsPack;
import goblinbob.mobends.core.pack.PackManager;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

/**
 * Modern UI pack list widget.
 * Displays a scrollable list of bends packs with selection support.
 */
public class MuiPackListView
{
    private static final int ITEM_HEIGHT = 40;
    private static final int THUMBNAIL_SIZE = 32;

    private final IViewFactory factory;
    private final IMuiScrollView scrollView;
    private final IMuiLinearLayout contentLayout;
    private final List<PackItemInfo> items;

    @Nullable
    private PackItemInfo selectedItem;
    @Nullable
    private Consumer<IBendsPack> onPackSelected;

    /**
     * Creates a new pack list view.
     *
     * @param factory The view factory
     */
    public MuiPackListView(IViewFactory factory)
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
     * Sets the callback for pack selection.
     *
     * @param callback Called when a pack is selected
     */
    public void setOnPackSelected(Consumer<IBendsPack> callback)
    {
        this.onPackSelected = callback;
    }

    /**
     * Populates the list with packs from the PackManager.
     */
    public void populateFromManager()
    {
        clear();

        Collection<? extends IBendsPack> packs = PackManager.INSTANCE.getLocalPacks();
        for (IBendsPack pack : packs)
        {
            addPack(pack);
        }
    }

    /**
     * Adds a pack to the list.
     *
     * @param pack The pack to add
     */
    public void addPack(IBendsPack pack)
    {
        PackItemInfo item = createPackItem(pack);
        items.add(item);

        ILayoutParams params = factory.createLayoutParams(
                ILayoutParams.MATCH_PARENT,
                ITEM_HEIGHT
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

        for (PackItemInfo item : items)
        {
            boolean matches = query.isEmpty() ||
                    item.pack.getDisplayName().toLowerCase().contains(lowerQuery) ||
                    item.pack.getAuthor().toLowerCase().contains(lowerQuery);

            item.rootLayout.setVisibility(matches ? IMuiView.VISIBLE : IMuiView.GONE);
        }
    }

    /**
     * Selects a pack by reference.
     *
     * @param pack The pack to select
     */
    public void selectPack(@Nullable IBendsPack pack)
    {
        // Deselect previous
        if (selectedItem != null)
        {
            updateItemSelection(selectedItem, false);
        }

        // Find and select new
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

    /**
     * @return The currently selected pack, or null
     */
    @Nullable
    public IBendsPack getSelectedPack()
    {
        return selectedItem != null ? selectedItem.pack : null;
    }

    /**
     * @return The root view to add to the layout
     */
    public IMuiView getView()
    {
        return scrollView;
    }

    // ==================== Private Methods ====================

    private PackItemInfo createPackItem(IBendsPack pack)
    {
        // Create root layout for item (horizontal: thumbnail | info)
        IMuiLinearLayout rootLayout = factory.createLinearLayout(IViewFactory.HORIZONTAL);
        rootLayout.setBackgroundColor(MoBendsTheme.BG_LIST_ITEM);
        rootLayout.setGravity(IMuiLinearLayout.GRAVITY_CENTER_VERTICAL);
        rootLayout.setPadding(MoBendsTheme.PADDING, MoBendsTheme.PADDING,
                             MoBendsTheme.PADDING, MoBendsTheme.PADDING);

        // Thumbnail placeholder (would need image loading support)
        IMuiView thumbnail = factory.createView();
        thumbnail.setBackgroundColor(MoBendsTheme.BG_CONTENT);
        thumbnail.setLayoutParams(factory.createLayoutParams(THUMBNAIL_SIZE, THUMBNAIL_SIZE));

        // Info container (vertical: name, author)
        IMuiLinearLayout infoLayout = factory.createLinearLayout(IViewFactory.VERTICAL);
        ILayoutParams infoParams = factory.createLayoutParams(
                ILayoutParams.MATCH_PARENT,
                ILayoutParams.WRAP_CONTENT
        );
        infoParams.setMargins(MoBendsTheme.PADDING, 0, 0, 0);

        // Pack name
        IMuiTextView nameView = factory.createTextView(pack.getDisplayName());
        nameView.setTextColor(MoBendsTheme.TEXT_PRIMARY);
        nameView.setTextSize(14);
        nameView.setBold(true);

        // Pack author
        IMuiTextView authorView = factory.createTextView("by " + pack.getAuthor());
        authorView.setTextColor(MoBendsTheme.TEXT_SECONDARY);
        authorView.setTextSize(11);

        // Assemble info layout
        infoLayout.addView(nameView, factory.createLayoutParams(
                ILayoutParams.WRAP_CONTENT,
                ILayoutParams.WRAP_CONTENT
        ));
        infoLayout.addView(authorView, factory.createLayoutParams(
                ILayoutParams.WRAP_CONTENT,
                ILayoutParams.WRAP_CONTENT
        ));

        // Assemble root layout
        rootLayout.addView(thumbnail, factory.createLayoutParams(THUMBNAIL_SIZE, THUMBNAIL_SIZE));
        rootLayout.addView(infoLayout, infoParams);

        // Create item info
        PackItemInfo itemInfo = new PackItemInfo(pack, rootLayout, nameView, authorView);

        // Set click handler
        rootLayout.setOnClickListener(() -> onItemClicked(itemInfo));

        return itemInfo;
    }

    private void onItemClicked(PackItemInfo item)
    {
        // Select this item
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
        final IMuiTextView nameView;
        final IMuiTextView authorView;

        PackItemInfo(IBendsPack pack, IMuiLinearLayout rootLayout,
                    IMuiTextView nameView, IMuiTextView authorView)
        {
            this.pack = pack;
            this.rootLayout = rootLayout;
            this.nameView = nameView;
            this.authorView = authorView;
        }
    }
}
