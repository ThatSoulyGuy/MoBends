package goblinbob.mobends.core.client.gui.vanilla;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class VanillaListView extends VanillaScrollView
{
    private final VanillaLinearLayout innerLayout;
    private final List<VanillaView> itemViews = new ArrayList<>();

    private boolean dividersShown = false;
    private int dividerColor = 0xFF2A2E3C;
    private int dividerHeight = 1;

    public VanillaListView()
    {
        innerLayout = new VanillaLinearLayout();
        innerLayout.setOrientation(VanillaLinearLayout.VERTICAL);
        VanillaLayoutParams params = new VanillaLayoutParams(VanillaLayoutParams.MATCH_PARENT, VanillaLayoutParams.WRAP_CONTENT);
        innerLayout.setLayoutParams(params);
        super.addView(innerLayout);
    }

    public void setAdapter(Object adapter)
    {
    }

    public void setSimpleAdapter(List<String> items, BiConsumer<Integer, String> onItemClick)
    {
        itemViews.clear();
        for (int i = 0; i < items.size(); i++)
        {
            final int index = i;
            final String item = items.get(i);
            VanillaTextView textView = new VanillaTextView(item);
            VanillaLayoutParams params = new VanillaLayoutParams(VanillaLayoutParams.MATCH_PARENT, VanillaLayoutParams.WRAP_CONTENT);
            textView.setLayoutParams(params);
            textView.setPadding(8, 4, 8, 4);
            textView.setOnClickListener(() -> onItemClick.accept(index, item));
            itemViews.add(textView);
        }
        rebuildItems();
    }

    public <T> void setCustomAdapter(List<T> items, Function<T, VanillaView> viewBinder, BiConsumer<Integer, T> onItemClick)
    {
        itemViews.clear();
        for (int i = 0; i < items.size(); i++)
        {
            final int index = i;
            final T item = items.get(i);
            VanillaView view = viewBinder.apply(item);
            view.setOnClickListener(() -> onItemClick.accept(index, item));
            itemViews.add(view);
        }
        rebuildItems();
    }

    private void rebuildItems()
    {
        innerLayout.removeAllViews();
        for (int i = 0; i < itemViews.size(); i++)
        {
            innerLayout.addView(itemViews.get(i));
            if (dividersShown && i < itemViews.size() - 1)
            {
                innerLayout.addView(makeDivider());
            }
        }
    }

    private VanillaView makeDivider()
    {
        VanillaView divider = new VanillaView();
        divider.setBackgroundColor(dividerColor);
        divider.setLayoutParams(new VanillaLayoutParams(VanillaLayoutParams.MATCH_PARENT, Math.max(1, dividerHeight)));
        return divider;
    }

    public void scrollToPosition(int position)
    {
        if (position >= 0 && position < itemViews.size())
        {
            VanillaView view = itemViews.get(position);
            scrollTo(scrollOffset + (view.y - y));
        }
    }

    public void smoothScrollToPosition(int position)
    {
        if (position >= 0 && position < itemViews.size())
        {
            VanillaView view = itemViews.get(position);
            smoothScrollTo(scrollOffset + (view.y - y));
        }
    }

    public void notifyDataSetChanged()
    {
    }

    public void setItemSpacing(int spacing)
    {
        innerLayout.setSpacing(spacing);
    }

    public void setDividers(boolean show, int color, int height)
    {
        this.dividersShown = show;
        this.dividerColor = color;
        this.dividerHeight = height;
        rebuildItems();
    }
}
