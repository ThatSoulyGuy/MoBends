package goblinbob.mobends.core.client.gui.vanilla;

import goblinbob.mobends.api.gui.ILayoutParams;
import goblinbob.mobends.api.gui.view.IFrameLayout;

public class VanillaFrameLayout extends VanillaViewGroup implements IFrameLayout
{
    private boolean measureAllChildren = false;

    @Override
    public void setMeasureAllChildren(boolean measureAll)
    {
        this.measureAllChildren = measureAll;
    }

    @Override
    public void measure(int availableWidth, int availableHeight)
    {
        int lpW = layoutParams != null ? layoutParams.getWidth() : ILayoutParams.WRAP_CONTENT;
        int lpH = layoutParams != null ? layoutParams.getHeight() : ILayoutParams.WRAP_CONTENT;

        // Constrain available space to own fixed size when specified
        int effectiveW = (lpW > 0) ? Math.min(lpW, availableWidth) : availableWidth;
        int effectiveH = (lpH > 0) ? Math.min(lpH, availableHeight) : availableHeight;

        int contentW = effectiveW - paddingLeft - paddingRight;
        int contentH = effectiveH - paddingTop - paddingBottom;

        int maxChildW = 0;
        int maxChildH = 0;

        for (VanillaView child : children)
        {
            if (child.visibility == GONE && !measureAllChildren) continue;

            VanillaLayoutParams clp = child.layoutParams;
            int ml = 0, mt = 0, mr = 0, mb = 0;
            if (clp != null)
            {
                ml = clp.getMarginLeft();
                mt = clp.getMarginTop();
                mr = clp.getMarginRight();
                mb = clp.getMarginBottom();
            }

            child.measure(contentW - ml - mr, contentH - mt - mb);
            maxChildW = Math.max(maxChildW, child.measuredWidth + ml + mr);
            maxChildH = Math.max(maxChildH, child.measuredHeight + mt + mb);
        }

        measuredWidth = resolveSize(lpW, availableWidth, maxChildW + paddingLeft + paddingRight);
        measuredHeight = resolveSize(lpH, availableHeight, maxChildH + paddingTop + paddingBottom);
    }

    @Override
    public void layout(int left, int top, int right, int bottom)
    {
        super.layout(left, top, right, bottom);

        for (VanillaView child : children)
        {
            if (child.visibility == GONE) continue;

            VanillaLayoutParams clp = child.layoutParams;
            int ml = 0, mt = 0, mr = 0, mb = 0;
            int childGravity = ILayoutParams.GRAVITY_NO_GRAVITY;
            if (clp != null)
            {
                ml = clp.getMarginLeft();
                mt = clp.getMarginTop();
                mr = clp.getMarginRight();
                mb = clp.getMarginBottom();
                childGravity = clp.getGravity();
            }

            int childW = child.measuredWidth;
            int childH = child.measuredHeight;
            int availW = getContentWidth() - ml - mr;
            int availH = getContentHeight() - mt - mb;

            if (clp != null && clp.getWidth() == ILayoutParams.MATCH_PARENT) childW = availW;
            if (clp != null && clp.getHeight() == ILayoutParams.MATCH_PARENT) childH = availH;

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

            child.layout(childLeft, childTop, childLeft + childW, childTop + childH);
        }
    }
}
