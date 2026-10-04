package goblinbob.mobends.core.client.gui.widget;

import goblinbob.mobends.core.client.gui.view.*;

import com.mojang.logging.LogUtils;
import goblinbob.mobends.core.client.gui.theme.MoBendsTheme;
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

public class PackListWidget
{
    private static final Logger LOG = LogUtils.getLogger();
    private static final int ITEM_HEIGHT = 34;

    private final ScrollView scrollView;
    private final LinearLayout contentLayout;
    private final List<PackItemInfo> items;

    @Nullable
    private PackItemInfo selectedItem;
    @Nullable
    private Consumer<IBendsPack> onPackSelected;

    public PackListWidget()
    {
        this.items = new ArrayList<>();

        this.scrollView = new ScrollView();
        this.scrollView.setLayoutParams(LayoutParams.matchParent());
        this.scrollView.setBackgroundColor(MoBendsTheme.BG_LIST);

        this.contentLayout = new LinearLayout(LinearLayout.VERTICAL);
        this.contentLayout.setLayoutParams(new LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
        ));
        this.contentLayout.setPadding(0, MoBendsTheme.PADDING_SMALL, 0, MoBendsTheme.PADDING_SMALL);

        scrollView.addView(contentLayout, LayoutParams.matchParent());
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

        LayoutParams params = new LayoutParams(
                LayoutParams.MATCH_PARENT,
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

            item.rootLayout.setVisibility(matches ? View.VISIBLE : View.GONE);
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

    public View getView()
    {
        return scrollView;
    }

    private PackItemInfo createPackItem(IBendsPack pack, boolean applied)
    {
        LinearLayout rootLayout = new LinearLayout(LinearLayout.HORIZONTAL);
        rootLayout.setBackgroundColor(MoBendsTheme.BG_LIST_ITEM);
        rootLayout.setGravity(LayoutParams.GRAVITY_CENTER_VERTICAL);
        rootLayout.setPadding(0, 0, MoBendsTheme.PADDING, 0);

        View accentBar = new View();
        accentBar.setBackgroundColor(applied ? MoBendsTheme.TOGGLE_ON : MoBendsTheme.TOGGLE_OFF);
        accentBar.setLayoutParams(new LayoutParams(3, LayoutParams.MATCH_PARENT));

        LinearLayout infoLayout = new LinearLayout(LinearLayout.VERTICAL);
        infoLayout.setGravity(LayoutParams.GRAVITY_CENTER_VERTICAL);
        LayoutParams infoParams = new LayoutParams(0, LayoutParams.MATCH_PARENT, 1.0f);
        infoParams.setMargins(MoBendsTheme.PADDING, 0, MoBendsTheme.PADDING, 0);

        TextView nameView = new TextView(pack.getDisplayName());
        nameView.setTextColor(MoBendsTheme.TEXT_PRIMARY);
        nameView.setTextSize(13);
        nameView.setBold(true);

        TextView authorView = new TextView("by " + pack.getAuthor());
        authorView.setTextColor(MoBendsTheme.TEXT_SECONDARY);
        authorView.setTextSize(10);

        infoLayout.addView(nameView, new LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        infoLayout.addView(authorView, new LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

        Toggle toggle = new Toggle(applied);
        toggle.setOnCheckedChangeListener(checked -> onPackToggled(pack, checked, accentBar));

        rootLayout.addView(accentBar, new LayoutParams(3, LayoutParams.MATCH_PARENT));
        rootLayout.addView(infoLayout, infoParams);
        rootLayout.addView(toggle, new LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

        PackItemInfo itemInfo = new PackItemInfo(pack, rootLayout, accentBar, nameView, authorView, toggle, applied);

        rootLayout.setOnClickListener(() -> onItemClicked(itemInfo));

        return itemInfo;
    }

    private void onPackToggled(IBendsPack pack, boolean apply, View accentBar)
    {
        accentBar.setBackgroundColor(apply ? MoBendsTheme.TOGGLE_ON : MoBendsTheme.TOGGLE_OFF);

        for (PackItemInfo item : items)
        {
            if (item.pack.getKey().equals(pack.getKey()))
            {
                item.applied = apply;
                break;
            }
        }

        List<String> appliedKeys = items.stream()
                .filter(item -> item.applied)
                .map(item -> item.pack.getKey())
                .collect(Collectors.toList());

        try
        {
            PackManager.INSTANCE.setAppliedPacks(appliedKeys, true);
        }
        catch (InvalidPackFormatException e)
        {
            LOG.error("Failed to apply pack '{}': {}", pack.getDisplayName(), e.getMessage());
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

    private static class PackItemInfo
    {
        final IBendsPack pack;
        final LinearLayout rootLayout;
        final View accentBar;
        final TextView nameView;
        final TextView authorView;
        final Toggle toggle;
        boolean applied;

        PackItemInfo(IBendsPack pack, LinearLayout rootLayout, View accentBar,
                    TextView nameView, TextView authorView, Toggle toggle,
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
