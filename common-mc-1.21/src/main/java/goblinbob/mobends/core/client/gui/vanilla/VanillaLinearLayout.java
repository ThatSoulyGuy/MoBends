package goblinbob.mobends.core.client.gui.vanilla;

import goblinbob.mobends.api.gui.ILayoutParams;
import goblinbob.mobends.api.gui.view.ILinearLayout;

public class VanillaLinearLayout extends VanillaViewGroup implements ILinearLayout
{
    private int orientation = VERTICAL;
    private int gravity = ILayoutParams.GRAVITY_NO_GRAVITY;
    private int spacing = 0;

    @Override
    public void setOrientation(int orientation) { this.orientation = orientation; }

    @Override
    public int getOrientation() { return orientation; }

    @Override
    public void setGravity(int gravity) { this.gravity = gravity; }

    @Override
    public void setSpacing(int spacing) { this.spacing = spacing; }

    @Override
    public void measure(int availableWidth, int availableHeight)
    {
        int lpW = layoutParams != null ? layoutParams.getWidth() : ILayoutParams.WRAP_CONTENT;
        int lpH = layoutParams != null ? layoutParams.getHeight() : ILayoutParams.WRAP_CONTENT;

        // Constrain available space to own fixed size when specified,
        // so children are measured within this layout's actual bounds
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

    /**
     * Returns the effective weight for a child in the main axis.
     * MATCH_PARENT in the main axis is treated as weight=1 (fill remaining space).
     */
    private static float effectiveWeight(float weight, int mainAxisSpec)
    {
        if (weight > 0) return weight;
        return (mainAxisSpec == ILayoutParams.MATCH_PARENT) ? 1.0f : 0;
    }

    private void measureVertical(int contentW, int contentH, int availableWidth, int availableHeight)
    {
        int totalFixedHeight = 0;
        float totalWeight = 0;
        int maxChildWidth = 0;
        int visibleCount = 0;

        // First pass: measure fixed/wrap children, accumulate weights
        for (VanillaView child : children)
        {
            if (child.visibility == GONE) continue;
            visibleCount++;

            VanillaLayoutParams clp = child.layoutParams;
            int ml = 0, mt = 0, mr = 0, mb = 0;
            float weight = 0;
            int childHeightSpec = ILayoutParams.WRAP_CONTENT;
            if (clp != null)
            {
                ml = clp.getMarginLeft();
                mt = clp.getMarginTop();
                mr = clp.getMarginRight();
                mb = clp.getMarginBottom();
                weight = clp.getWeight();
                childHeightSpec = clp.getHeight();
            }

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
                // Cross-axis MATCH_PARENT children will be stretched during layout,
                // so don't let them inflate a WRAP_CONTENT parent's width.
                int childWidthSpec = clp != null ? clp.getWidth() : ILayoutParams.WRAP_CONTENT;
                if (childWidthSpec != ILayoutParams.MATCH_PARENT)
                {
                    maxChildWidth = Math.max(maxChildWidth, child.measuredWidth + ml + mr);
                }
            }
        }

        if (visibleCount > 1)
        {
            totalFixedHeight += spacing * (visibleCount - 1);
        }

        // Second pass: distribute remaining space to weighted / MATCH_PARENT children
        int remainingHeight = Math.max(0, contentH - totalFixedHeight);
        if (totalWeight > 0)
        {
            for (VanillaView child : children)
            {
                if (child.visibility == GONE) continue;
                VanillaLayoutParams clp = child.layoutParams;
                float weight = clp != null ? clp.getWeight() : 0;
                int childHeightSpec = clp != null ? clp.getHeight() : ILayoutParams.WRAP_CONTENT;
                float ew = effectiveWeight(weight, childHeightSpec);

                if (ew > 0)
                {
                    int ml = clp != null ? clp.getMarginLeft() : 0;
                    int mr = clp != null ? clp.getMarginRight() : 0;
                    int childH = (int) (remainingHeight * ew / totalWeight);
                    int childAvailW = contentW - ml - mr;
                    child.measure(childAvailW, childH);
                    // Only override measured height when the allocation is reasonable
                    if (childH < 100000)
                    {
                        child.measuredHeight = childH;
                    }
                    int childWidthSpec = clp != null ? clp.getWidth() : ILayoutParams.WRAP_CONTENT;
                    if (childWidthSpec != ILayoutParams.MATCH_PARENT)
                    {
                        maxChildWidth = Math.max(maxChildWidth, child.measuredWidth + ml + mr);
                    }
                }
            }
        }

        int lpW = layoutParams != null ? layoutParams.getWidth() : ILayoutParams.WRAP_CONTENT;
        int lpH = layoutParams != null ? layoutParams.getHeight() : ILayoutParams.WRAP_CONTENT;

        measuredWidth = resolveSize(lpW, availableWidth, maxChildWidth + paddingLeft + paddingRight);

        if (lpH == ILayoutParams.WRAP_CONTENT)
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

        // First pass: measure fixed/wrap children, accumulate weights
        for (VanillaView child : children)
        {
            if (child.visibility == GONE) continue;
            visibleCount++;

            VanillaLayoutParams clp = child.layoutParams;
            int ml = 0, mt = 0, mr = 0, mb = 0;
            float weight = 0;
            int childWidthSpec = ILayoutParams.WRAP_CONTENT;
            if (clp != null)
            {
                ml = clp.getMarginLeft();
                mt = clp.getMarginTop();
                mr = clp.getMarginRight();
                mb = clp.getMarginBottom();
                weight = clp.getWeight();
                childWidthSpec = clp.getWidth();
            }

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
                // Cross-axis MATCH_PARENT children will be stretched during layout,
                // so don't let them inflate a WRAP_CONTENT parent's height.
                int childHeightSpec = clp != null ? clp.getHeight() : ILayoutParams.WRAP_CONTENT;
                if (childHeightSpec != ILayoutParams.MATCH_PARENT)
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
                float weight = clp != null ? clp.getWeight() : 0;
                int childWidthSpec = clp != null ? clp.getWidth() : ILayoutParams.WRAP_CONTENT;
                float ew = effectiveWeight(weight, childWidthSpec);

                if (ew > 0)
                {
                    int mt = clp != null ? clp.getMarginTop() : 0;
                    int mb = clp != null ? clp.getMarginBottom() : 0;
                    int childW = (int) (remainingWidth * ew / totalWeight);
                    int childAvailH = contentH - mt - mb;
                    child.measure(childW, childAvailH);
                    // Only override measured width when the allocation is reasonable
                    if (childW < 100000)
                    {
                        child.measuredWidth = childW;
                    }
                    int childHeightSpec = clp != null ? clp.getHeight() : ILayoutParams.WRAP_CONTENT;
                    if (childHeightSpec != ILayoutParams.MATCH_PARENT)
                    {
                        maxChildHeight = Math.max(maxChildHeight, child.measuredHeight + mt + mb);
                    }
                }
            }
        }

        int lpW = layoutParams != null ? layoutParams.getWidth() : ILayoutParams.WRAP_CONTENT;
        int lpH = layoutParams != null ? layoutParams.getHeight() : ILayoutParams.WRAP_CONTENT;

        if (lpW == ILayoutParams.WRAP_CONTENT)
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

    @Override
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
            int ml = 0, mt = 0, mr = 0, mb = 0;
            int childGravity = gravity;
            if (clp != null)
            {
                ml = clp.getMarginLeft();
                mt = clp.getMarginTop();
                mr = clp.getMarginRight();
                mb = clp.getMarginBottom();
                if (clp.getGravity() != ILayoutParams.GRAVITY_NO_GRAVITY)
                {
                    childGravity = clp.getGravity();
                }
            }

            childTop += mt;

            int childW = child.measuredWidth;
            int availW = getContentWidth() - ml - mr;

            if (clp != null && clp.getWidth() == ILayoutParams.MATCH_PARENT)
            {
                childW = availW;
            }

            int childLeft;
            int horizontalGravity = childGravity & 0x07;
            if (horizontalGravity == 0x05)
            {
                childLeft = x + paddingLeft + availW - childW + ml;
            }
            else if (horizontalGravity == 0x01)
            {
                childLeft = x + paddingLeft + ml + (availW - childW) / 2;
            }
            else
            {
                childLeft = x + paddingLeft + ml;
            }

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
            int ml = 0, mt = 0, mr = 0, mb = 0;
            int childGravity = gravity;
            if (clp != null)
            {
                ml = clp.getMarginLeft();
                mt = clp.getMarginTop();
                mr = clp.getMarginRight();
                mb = clp.getMarginBottom();
                if (clp.getGravity() != ILayoutParams.GRAVITY_NO_GRAVITY)
                {
                    childGravity = clp.getGravity();
                }
            }

            childLeft += ml;

            int childH = child.measuredHeight;
            int availH = getContentHeight() - mt - mb;

            if (clp != null && clp.getHeight() == ILayoutParams.MATCH_PARENT)
            {
                childH = availH;
            }

            int childTop;
            int verticalGravity = childGravity & 0x70;
            if (verticalGravity == 0x50)
            {
                childTop = y + paddingTop + availH - childH + mt;
            }
            else if (verticalGravity == 0x10)
            {
                childTop = y + paddingTop + mt + (availH - childH) / 2;
            }
            else
            {
                childTop = y + paddingTop + mt;
            }

            child.layout(childLeft, childTop, childLeft + child.measuredWidth, childTop + childH);
            childLeft += child.measuredWidth + mr + spacing;
        }
    }
}
