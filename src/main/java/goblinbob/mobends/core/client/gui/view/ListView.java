package goblinbob.mobends.core.client.gui.view;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class ListView extends ScrollView
{
    private final LinearLayout innerLayout;
    private final List<View> itemViews = new ArrayList<>();

    private boolean dividersShown = false;
    private int dividerColor = 0xFF2A2E3C;
    private int dividerHeight = 1;

    public ListView()
    {
        innerLayout = new LinearLayout();
        innerLayout.setOrientation(LinearLayout.VERTICAL);
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        innerLayout.setLayoutParams(params);
        super.addView(innerLayout);
    }

    public void setSimpleAdapter(List<String> items, BiConsumer<Integer, String> onItemClick)
    {
        itemViews.clear();
        for (int i = 0; i < items.size(); i++)
        {
            final int index = i;
            final String item = items.get(i);
            TextView textView = new TextView(item);
            LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            textView.setLayoutParams(params);
            textView.setPadding(8, 4, 8, 4);
            textView.setOnClickListener(() -> onItemClick.accept(index, item));
            itemViews.add(textView);
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

    private View makeDivider()
    {
        View divider = new View();
        divider.setBackgroundColor(dividerColor);
        divider.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, Math.max(1, dividerHeight)));
        return divider;
    }

    public void setDividers(boolean show, int color, int height)
    {
        this.dividersShown = show;
        this.dividerColor = color;
        this.dividerHeight = height;
        rebuildItems();
    }
}
