package goblinbob.mobends.core.client.gui.vanilla;

public class VanillaLinearLayout extends VanillaViewGroup
{
    public static final int HORIZONTAL = 0;
    public static final int VERTICAL = 1;

    private int orientation = VERTICAL;
    private int gravity = VanillaLayoutParams.GRAVITY_NO_GRAVITY;
    private int spacing = 0;

    public VanillaLinearLayout()
    {
    }

    public VanillaLinearLayout(int orientation)
    {
        this.orientation = orientation;
    }

    public void setOrientation(int orientation) { this.orientation = orientation; }

    public void setGravity(int gravity) { this.gravity = gravity; }

    public void setSpacing(int spacing) { this.spacing = spacing; }

    public void measure(int availableWidth, int availableHeight)
    {
        int lpW = layoutParams.getWidth();
        int lpH = layoutParams.getHeight();

        int effectiveW = (lpW > 0) ? Math.min(lpW, availableWidth) : availableWidth;
        int effectiveH = (lpH > 0) ? Math.min(lpH, availableHeight) : availableHeight;

        int contentW = effectiveW - paddingLeft - paddingRight;
        int contentH = effectiveH - paddingTop - paddingBottom;

        if (orientation == VERTICAL)
        {
            measureVertical(contentW, contentH, availableWidth, availableHeight);
        }
        else
        {
            measureHorizontal(contentW, contentH, availableWidth, availableHeight);
        }
    }

    private static float effectiveWeight(float weight, int mainAxisSpec)
    {
        if (weight > 0) return weight;
        return (mainAxisSpec == VanillaLayoutParams.MATCH_PARENT) ? 1.0f : 0;
    }

    private void measureVertical(int contentW, int contentH, int availableWidth, int availableHeight)
    {
        int totalFixedHeight = 0;
        float totalWeight = 0;
        int maxChildWidth = 0;
        int visibleCount = 0;

        for (VanillaView child : children)
        {
            if (child.visibility == GONE) continue;
            visibleCount++;

            VanillaLayoutParams clp = child.layoutParams;
            int ml = clp.getMarginLeft(), mt = clp.getMarginTop(), mr = clp.getMarginRight(), mb = clp.getMarginBottom();
            float weight = clp.getWeight();
            int childHeightSpec = clp.getHeight();

            float ew = effectiveWeight(weight, childHeightSpec);
            if (ew > 0)
            {
                totalWeight += ew;
                totalFixedHeight += mt + mb;
            }
            else
            {
                int childAvailW = contentW - ml - mr;
                child.measure(childAvailW, contentH);
                totalFixedHeight += child.measuredHeight + mt + mb;
                int childWidthSpec = clp.getWidth();
                if (childWidthSpec != VanillaLayoutParams.MATCH_PARENT)
                {
                    maxChildWidth = Math.max(maxChildWidth, child.measuredWidth + ml + mr);
                }
            }
        }

        if (visibleCount > 1)
        {
            totalFixedHeight += spacing * (visibleCount - 1);
        }

        int remainingHeight = Math.max(0, contentH - totalFixedHeight);
        if (totalWeight > 0)
        {
            for (VanillaView child : children)
            {
                if (child.visibility == GONE) continue;
                VanillaLayoutParams clp = child.layoutParams;
                float weight = clp.getWeight();
                int childHeightSpec = clp.getHeight();
                float ew = effectiveWeight(weight, childHeightSpec);

                if (ew > 0)
                {
                    int ml = clp.getMarginLeft();
                    int mr = clp.getMarginRight();
                    int childH = (int) (remainingHeight * ew / totalWeight);
                    int childAvailW = contentW - ml - mr;
                    child.measure(childAvailW, childH);
                    if (childH < 100000)
                    {
                        child.measuredHeight = childH;
                    }
                    int childWidthSpec = clp.getWidth();
                    if (childWidthSpec != VanillaLayoutParams.MATCH_PARENT)
                    {
                        maxChildWidth = Math.max(maxChildWidth, child.measuredWidth + ml + mr);
                    }
                }
            }
        }

        int lpW = layoutParams.getWidth();
        int lpH = layoutParams.getHeight();

        measuredWidth = resolveSize(lpW, availableWidth, maxChildWidth + paddingLeft + paddingRight);

        if (lpH == VanillaLayoutParams.WRAP_CONTENT)
        {
            measuredHeight = totalFixedHeight + paddingTop + paddingBottom;
            if (totalWeight > 0)
            {
                measuredHeight = availableHeight;
            }
        }
        else
        {
            measuredHeight = resolveSize(lpH, availableHeight, totalFixedHeight + paddingTop + paddingBottom);
        }
    }

    private void measureHorizontal(int contentW, int contentH, int availableWidth, int availableHeight)
    {
        int totalFixedWidth = 0;
        float totalWeight = 0;
        int maxChildHeight = 0;
        int visibleCount = 0;

        for (VanillaView child : children)
        {
            if (child.visibility == GONE) continue;
            visibleCount++;

            VanillaLayoutParams clp = child.layoutParams;
            int ml = clp.getMarginLeft(), mt = clp.getMarginTop(), mr = clp.getMarginRight(), mb = clp.getMarginBottom();
            float weight = clp.getWeight();
            int childWidthSpec = clp.getWidth();

            float ew = effectiveWeight(weight, childWidthSpec);
            if (ew > 0)
            {
                totalWeight += ew;
                totalFixedWidth += ml + mr;
            }
            else
            {
                int childAvailH = contentH - mt - mb;
                child.measure(contentW, childAvailH);
                totalFixedWidth += child.measuredWidth + ml + mr;
                int childHeightSpec = clp.getHeight();
                if (childHeightSpec != VanillaLayoutParams.MATCH_PARENT)
                {
                    maxChildHeight = Math.max(maxChildHeight, child.measuredHeight + mt + mb);
                }
            }
        }

        if (visibleCount > 1)
        {
            totalFixedWidth += spacing * (visibleCount - 1);
        }

        int remainingWidth = Math.max(0, contentW - totalFixedWidth);
        if (totalWeight > 0)
        {
            for (VanillaView child : children)
            {
                if (child.visibility == GONE) continue;
                VanillaLayoutParams clp = child.layoutParams;
                float weight = clp.getWeight();
                int childWidthSpec = clp.getWidth();
                float ew = effectiveWeight(weight, childWidthSpec);

                if (ew > 0)
                {
                    int mt = clp.getMarginTop();
                    int mb = clp.getMarginBottom();
                    int childW = (int) (remainingWidth * ew / totalWeight);
                    int childAvailH = contentH - mt - mb;
                    child.measure(childW, childAvailH);
                    if (childW < 100000)
                    {
                        child.measuredWidth = childW;
                    }
                    int childHeightSpec = clp.getHeight();
                    if (childHeightSpec != VanillaLayoutParams.MATCH_PARENT)
                    {
                        maxChildHeight = Math.max(maxChildHeight, child.measuredHeight + mt + mb);
                    }
                }
            }
        }

        int lpW = layoutParams.getWidth();
        int lpH = layoutParams.getHeight();

        if (lpW == VanillaLayoutParams.WRAP_CONTENT)
        {
            measuredWidth = totalFixedWidth + paddingLeft + paddingRight;
            if (totalWeight > 0)
            {
                measuredWidth = availableWidth;
            }
        }
        else
        {
            measuredWidth = resolveSize(lpW, availableWidth, totalFixedWidth + paddingLeft + paddingRight);
        }

        measuredHeight = resolveSize(lpH, availableHeight, maxChildHeight + paddingTop + paddingBottom);
    }

    public void layout(int left, int top, int right, int bottom)
    {
        super.layout(left, top, right, bottom);

        if (orientation == VERTICAL)
        {
            layoutVertical();
        }
        else
        {
            layoutHorizontal();
        }
    }

    private void layoutVertical()
    {
        int childTop = y + paddingTop;

        for (VanillaView child : children)
        {
            if (child.visibility == GONE) continue;

            VanillaLayoutParams clp = child.layoutParams;
            int ml = clp.getMarginLeft(), mt = clp.getMarginTop(), mr = clp.getMarginRight(), mb = clp.getMarginBottom();
            int childGravity = gravity;
            if (clp.getGravity() != VanillaLayoutParams.GRAVITY_NO_GRAVITY)
            {
                childGravity = clp.getGravity();
            }

            childTop += mt;

            int childW = child.measuredWidth;
            int availW = getContentWidth() - ml - mr;

            if (clp.getWidth() == VanillaLayoutParams.MATCH_PARENT)
            {
                childW = availW;
            }

            int childLeft = VanillaLayoutParams.alignH(childGravity, x + paddingLeft + ml, availW, childW);

            child.layout(childLeft, childTop, childLeft + childW, childTop + child.measuredHeight);
            childTop += child.measuredHeight + mb + spacing;
        }
    }

    private void layoutHorizontal()
    {
        int childLeft = x + paddingLeft;

        for (VanillaView child : children)
        {
            if (child.visibility == GONE) continue;

            VanillaLayoutParams clp = child.layoutParams;
            int ml = clp.getMarginLeft(), mt = clp.getMarginTop(), mr = clp.getMarginRight(), mb = clp.getMarginBottom();
            int childGravity = gravity;
            if (clp.getGravity() != VanillaLayoutParams.GRAVITY_NO_GRAVITY)
            {
                childGravity = clp.getGravity();
            }

            childLeft += ml;

            int childH = child.measuredHeight;
            int availH = getContentHeight() - mt - mb;

            if (clp.getHeight() == VanillaLayoutParams.MATCH_PARENT)
            {
                childH = availH;
            }

            int childTop = VanillaLayoutParams.alignV(childGravity, y + paddingTop + mt, availH, childH);

            child.layout(childLeft, childTop, childLeft + child.measuredWidth, childTop + childH);
            childLeft += child.measuredWidth + mr + spacing;
        }
    }
}
