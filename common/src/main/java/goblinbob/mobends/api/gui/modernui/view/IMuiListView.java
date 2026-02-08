package goblinbob.mobends.api.gui.modernui.view;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Abstraction for Modern UI RecyclerView/ListView.
 * Displays a scrollable list of items with recycling.
 */
public interface IMuiListView extends IMuiViewGroup
{
    /**
     * Sets the adapter/data source for this list.
     *
     * @param adapter The native adapter object
     */
    void setAdapter(Object adapter);

    /**
     * Sets a simple string list adapter.
     *
     * @param items The list of string items
     * @param onItemClick Callback for item clicks (receives index and item)
     */
    void setSimpleAdapter(List<String> items, BiConsumer<Integer, String> onItemClick);

    /**
     * Sets a custom item adapter.
     *
     * @param items The list of items
     * @param viewBinder Function that creates/binds a view for each item
     * @param onItemClick Callback for item clicks
     * @param <T> The item type
     */
    <T> void setCustomAdapter(List<T> items,
                               Function<T, IMuiView> viewBinder,
                               BiConsumer<Integer, T> onItemClick);

    /**
     * Scrolls to show the item at the given position.
     *
     * @param position The item index to scroll to
     */
    void scrollToPosition(int position);

    /**
     * Smoothly scrolls to the item at the given position.
     *
     * @param position The item index to scroll to
     */
    void smoothScrollToPosition(int position);

    /**
     * Notifies that the data has changed and the list should refresh.
     */
    void notifyDataSetChanged();

    /**
     * Sets the spacing between list items.
     *
     * @param spacing Spacing in dp
     */
    void setItemSpacing(int spacing);

    /**
     * Sets whether dividers should be shown between items.
     *
     * @param show True to show dividers
     * @param color Divider color in ARGB format
     * @param height Divider height in dp
     */
    void setDividers(boolean show, int color, int height);
}
