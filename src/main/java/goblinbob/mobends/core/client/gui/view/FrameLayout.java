package goblinbob.mobends.core.client.gui.view;

public class FrameLayout extends ViewGroup
{
    public void measure(int availableWidth, int availableHeight)
    {
        int lpW = layoutParams.getWidth();
        int lpH = layoutParams.getHeight();

        int effectiveW = (lpW > 0) ? Math.min(lpW, availableWidth) : availableWidth;
        int effectiveH = (lpH > 0) ? Math.min(lpH, availableHeight) : availableHeight;

        int contentW = effectiveW - paddingLeft - paddingRight;
        int contentH = effectiveH - paddingTop - paddingBottom;

        int maxChildW = 0;
        int maxChildH = 0;

        for (View child : children)
        {
            if (child.visibility == GONE) continue;

            LayoutParams clp = child.layoutParams;
            int ml = clp.getMarginLeft(), mt = clp.getMarginTop(), mr = clp.getMarginRight(), mb = clp.getMarginBottom();

            child.measure(contentW - ml - mr, contentH - mt - mb);
            maxChildW = Math.max(maxChildW, child.measuredWidth + ml + mr);
            maxChildH = Math.max(maxChildH, child.measuredHeight + mt + mb);
        }

        measuredWidth = resolveSize(lpW, availableWidth, maxChildW + paddingLeft + paddingRight);
        measuredHeight = resolveSize(lpH, availableHeight, maxChildH + paddingTop + paddingBottom);
    }

    public void layout(int left, int top, int right, int bottom)
    {
        super.layout(left, top, right, bottom);

        for (View child : children)
        {
            if (child.visibility == GONE) continue;

            LayoutParams clp = child.layoutParams;
            int ml = clp.getMarginLeft(), mt = clp.getMarginTop(), mr = clp.getMarginRight(), mb = clp.getMarginBottom();
            int childGravity = clp.getGravity();

            int childW = child.measuredWidth;
            int childH = child.measuredHeight;
            int availW = getContentWidth() - ml - mr;
            int availH = getContentHeight() - mt - mb;

            if (clp.getWidth() == LayoutParams.MATCH_PARENT) childW = availW;
            if (clp.getHeight() == LayoutParams.MATCH_PARENT) childH = availH;

            int childLeft = LayoutParams.alignH(childGravity, x + paddingLeft + ml, availW, childW);
            int childTop = LayoutParams.alignV(childGravity, y + paddingTop + mt, availH, childH);

            child.layout(childLeft, childTop, childLeft + childW, childTop + childH);
        }
    }
}
