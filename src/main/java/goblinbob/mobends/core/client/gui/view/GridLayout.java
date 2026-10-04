package goblinbob.mobends.core.client.gui.view;

public class GridLayout extends ViewGroup
{
    private int cellWidth = 80;
    private int cellHeight = 80;
    private int horizontalSpacing = 4;
    private int verticalSpacing = 4;

    private int columnCount = 1;
    private int resolvedCellWidth = 80;

    public void setCellSize(int cellWidth, int cellHeight)
    {
        this.cellWidth = Math.max(1, cellWidth);
        this.cellHeight = Math.max(1, cellHeight);
    }

    public void setSpacing(int horizontalSpacing, int verticalSpacing)
    {
        this.horizontalSpacing = Math.max(0, horizontalSpacing);
        this.verticalSpacing = Math.max(0, verticalSpacing);
    }

    public void measure(int availableWidth, int availableHeight)
    {
        int lpW = layoutParams.getWidth();
        int lpH = layoutParams.getHeight();

        measuredWidth = resolveSize(lpW, availableWidth, availableWidth);

        int contentW = Math.max(1, measuredWidth - paddingLeft - paddingRight);
        columnCount = Math.max(1, (contentW + horizontalSpacing) / (cellWidth + horizontalSpacing));
        resolvedCellWidth = Math.max(1,
                (contentW - horizontalSpacing * (columnCount - 1)) / columnCount);

        int visibleCount = 0;
        for (View child : children)
        {
            if (child.visibility == GONE) continue;

            visibleCount++;
            child.measure(resolvedCellWidth, cellHeight);
            child.measuredWidth = resolvedCellWidth;
            child.measuredHeight = cellHeight;
        }

        int rowCount = (visibleCount + columnCount - 1) / columnCount;
        int contentH = rowCount * cellHeight + Math.max(0, rowCount - 1) * verticalSpacing;

        measuredHeight = resolveSize(lpH, availableHeight, contentH + paddingTop + paddingBottom);
    }

    public void layout(int left, int top, int right, int bottom)
    {
        super.layout(left, top, right, bottom);

        int column = 0;
        int rowTop = y + paddingTop;

        for (View child : children)
        {
            if (child.visibility == GONE) continue;

            int childLeft = x + paddingLeft + column * (resolvedCellWidth + horizontalSpacing);
            child.layout(childLeft, rowTop, childLeft + resolvedCellWidth, rowTop + cellHeight);

            if (++column >= columnCount)
            {
                column = 0;
                rowTop += cellHeight + verticalSpacing;
            }
        }
    }
}
