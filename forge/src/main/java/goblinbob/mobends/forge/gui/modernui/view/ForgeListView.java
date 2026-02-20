package goblinbob.mobends.forge.gui.modernui.view;

import goblinbob.mobends.api.gui.view.IListView;
import goblinbob.mobends.api.gui.view.IView;
import icyllis.modernui.core.Context;
import icyllis.modernui.view.ViewGroup;
import icyllis.modernui.widget.LinearLayout;
import icyllis.modernui.widget.ScrollView;
import icyllis.modernui.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Forge list view implementation using ScrollView + LinearLayout.
 * Requires Context for Modern UI 3.9.x.
 */
public class ForgeListView extends ForgeViewGroup implements IListView
{
    private final Context context;
    private final ScrollView scrollView;
    private final LinearLayout contentLayout;
    private int itemSpacing = 0;
    private final List<IView> itemViews = new ArrayList<>();

    public ForgeListView(Context context)
    {
        super(new ScrollView(context));
        this.context = context;
        this.scrollView = (ScrollView) nativeViewGroup;
        this.contentLayout = new LinearLayout(context);
        this.contentLayout.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(contentLayout);
    }

    @Override
    public void setAdapter(Object adapter)
    {
        // RecyclerView adapter - not directly supported in this simple implementation
    }

    @Override
    public void setSimpleAdapter(List<String> items, BiConsumer<Integer, String> onItemClick)
    {
        contentLayout.removeAllViews();
        itemViews.clear();

        for (int i = 0; i < items.size(); i++)
        {
            final int index = i;
            final String item = items.get(i);

            TextView textView = new TextView(context);
            textView.setText(item);
            textView.setPadding(8, 8, 8, 8);
            textView.setOnClickListener(v -> onItemClick.accept(index, item));

            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );

            contentLayout.addView(textView, params);
            itemViews.add(new ForgeTextView(textView));
        }
    }

    @Override
    public <T> void setCustomAdapter(List<T> items, Function<T, IView> viewBinder,
                                     BiConsumer<Integer, T> onItemClick)
    {
        contentLayout.removeAllViews();
        itemViews.clear();

        for (int i = 0; i < items.size(); i++)
        {
            final int index = i;
            final T item = items.get(i);

            IView view = viewBinder.apply(item);
            view.setOnClickListener(() -> onItemClick.accept(index, item));

            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );

            contentLayout.addView((icyllis.modernui.view.View) view.getNativeView(), params);
            itemViews.add(view);
        }
    }

    @Override
    public void scrollToPosition(int position)
    {
        if (position >= 0 && position < itemViews.size())
        {
            icyllis.modernui.view.View view =
                    (icyllis.modernui.view.View) itemViews.get(position).getNativeView();
            scrollView.scrollTo(0, view.getTop());
        }
    }

    @Override
    public void smoothScrollToPosition(int position)
    {
        if (position >= 0 && position < itemViews.size())
        {
            icyllis.modernui.view.View view =
                    (icyllis.modernui.view.View) itemViews.get(position).getNativeView();
            // Modern UI 3.9.x may not have smoothScrollTo, use regular scrollTo
            scrollView.scrollTo(0, view.getTop());
        }
    }

    @Override
    public void notifyDataSetChanged()
    {
        contentLayout.requestLayout();
    }

    @Override
    public void setItemSpacing(int spacing)
    {
        this.itemSpacing = spacing;
        // Would need to update margins on existing children
    }

    @Override
    public void setDividers(boolean show, int color, int height)
    {
        // Would need to add divider views between items
        // This is a placeholder
    }
}
