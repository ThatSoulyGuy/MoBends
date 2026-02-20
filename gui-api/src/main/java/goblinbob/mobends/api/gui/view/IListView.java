package goblinbob.mobends.api.gui.view;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Abstraction for a scrollable list of items.
 */
public interface IListView extends IViewGroup
{
    void setAdapter(Object adapter);
    void setSimpleAdapter(List<String> items, BiConsumer<Integer, String> onItemClick);
    <T> void setCustomAdapter(List<T> items, Function<T, IView> viewBinder, BiConsumer<Integer, T> onItemClick);
    void scrollToPosition(int position);
    void smoothScrollToPosition(int position);
    void notifyDataSetChanged();
    void setItemSpacing(int spacing);
    void setDividers(boolean show, int color, int height);
}
