package goblinbob.mobends.core.client.gui.vanilla;

import goblinbob.mobends.api.gui.ILayoutParams;
import goblinbob.mobends.api.gui.view.ILinearLayout;
import goblinbob.mobends.api.gui.view.IListView;
import goblinbob.mobends.api.gui.view.IView;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class VanillaListView extends VanillaScrollView implements IListView
{
    private final VanillaLinearLayout innerLayout;
    private final List<IView> itemViews = new ArrayList<>();

    public VanillaListView()
    {
        innerLayout = new VanillaLinearLayout();
        innerLayout.setOrientation(ILinearLayout.VERTICAL);
        VanillaLayoutParams params = new VanillaLayoutParams(ILayoutParams.MATCH_PARENT, ILayoutParams.WRAP_CONTENT);
        innerLayout.setLayoutParams(params);
        super.addView(innerLayout);
    }

    @Override
    public void setAdapter(Object adapter)
    {
        // Not supported
    }

    @Override
    public void setSimpleAdapter(List<String> items, BiConsumer<Integer, String> onItemClick)
    {
        innerLayout.removeAllViews();
        itemViews.clear();

        for (int i = 0; i < items.size(); i++)
        {
            final int index = i;
            final String item = items.get(i);
            VanillaTextView textView = new VanillaTextView(item);
            VanillaLayoutParams params = new VanillaLayoutParams(ILayoutParams.MATCH_PARENT, ILayoutParams.WRAP_CONTENT);
            textView.setLayoutParams(params);
            textView.setPadding(8, 4, 8, 4);
            textView.setOnClickListener(() -> onItemClick.accept(index, item));
            innerLayout.addView(textView);
            itemViews.add(textView);
        }
    }

    @Override
    public <T> void setCustomAdapter(List<T> items, Function<T, IView> viewBinder, BiConsumer<Integer, T> onItemClick)
    {
        innerLayout.removeAllViews();
        itemViews.clear();

        for (int i = 0; i < items.size(); i++)
        {
            final int index = i;
            final T item = items.get(i);
            IView view = viewBinder.apply(item);
            view.setOnClickListener(() -> onItemClick.accept(index, item));
            innerLayout.addView(view);
            itemViews.add(view);
        }
    }

    @Override
    public void scrollToPosition(int position)
    {
        if (position >= 0 && position < itemViews.size())
        {
            IView view = itemViews.get(position);
            if (view instanceof VanillaView vv)
            {
                scrollTo(vv.y - y);
            }
        }
    }

    @Override
    public void smoothScrollToPosition(int position)
    {
        scrollToPosition(position);
    }

    @Override
    public void notifyDataSetChanged()
    {
        // Will be re-measured/laid out on next render cycle
    }

    @Override
    public void setItemSpacing(int spacing)
    {
        innerLayout.setSpacing(spacing);
    }

    @Override
    public void setDividers(boolean show, int color, int height)
    {
        // Not implemented
    }
}
