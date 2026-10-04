package goblinbob.mobends.core.client.gui.view;

import goblinbob.mobends.core.client.gui.theme.MoBendsTheme;
import net.minecraft.client.gui.GuiGraphics;

public class ScrollView extends ViewGroup
{
    protected int scrollOffset = 0;
    protected int maxScroll = 0;

    private int targetScroll = 0;
    private boolean animatingScroll = false;

    private boolean draggingThumb = false;
    private double thumbGrabOffset = 0;

    private static final int SCROLLBAR_WIDTH = 4;
    private static final int OUTSIDE_POINTER = Integer.MIN_VALUE / 2;

    public void scrollTo(int y)
    {
        this.animatingScroll = false;
        this.scrollOffset = Math.max(0, Math.min(y, maxScroll));
        relayoutChildren();
    }

    public void smoothScrollTo(int y)
    {
        this.targetScroll = y;
        this.animatingScroll = true;
    }

    public void measure(int availableWidth, int availableHeight)
    {
        int lpW = layoutParams.getWidth();
        int lpH = layoutParams.getHeight();

        measuredWidth = resolveSize(lpW, availableWidth, availableWidth);
        measuredHeight = resolveSize(lpH, availableHeight, availableHeight);

        int contentW = measuredWidth - paddingLeft - paddingRight;
        contentW -= SCROLLBAR_WIDTH;

        for (View child : children)
        {
            if (child.visibility == GONE) continue;
            LayoutParams clp = child.layoutParams;
            int ml = clp.getMarginLeft(), mr = clp.getMarginRight();
            child.measure(contentW - ml - mr, Integer.MAX_VALUE / 2);
        }
    }

    public void layout(int left, int top, int right, int bottom)
    {
        super.layout(left, top, right, bottom);
        if (animatingScroll)
        {
            advanceSmoothScroll();
        }
        relayoutChildren();
    }

    private void advanceSmoothScroll()
    {
        int target = Math.max(0, Math.min(targetScroll, maxScroll));
        int diff = target - scrollOffset;
        if (Math.abs(diff) <= 1)
        {
            scrollOffset = target;
            animatingScroll = false;
        }
        else
        {
            scrollOffset += diff > 0 ? Math.max(1, (int) (diff * 0.30f)) : Math.min(-1, (int) (diff * 0.30f));
        }
    }

    protected void relayoutChildren()
    {
        int contentW = measuredWidth - paddingLeft - paddingRight;
        contentW -= SCROLLBAR_WIDTH;

        int totalContentHeight = 0;

        for (View child : children)
        {
            if (child.visibility == GONE) continue;
            LayoutParams clp = child.layoutParams;
            int ml = clp.getMarginLeft(), mt = clp.getMarginTop(), mr = clp.getMarginRight(), mb = clp.getMarginBottom();

            int childW = child.measuredWidth;
            if (clp.getWidth() == LayoutParams.MATCH_PARENT)
            {
                childW = contentW - ml - mr;
            }

            int childLeft = x + paddingLeft + ml;
            int childTop = y + paddingTop + mt - scrollOffset + totalContentHeight;

            child.layout(childLeft, childTop, childLeft + childW, childTop + child.measuredHeight);
            totalContentHeight += child.measuredHeight + mt + mb;
        }

        int viewportHeight = getContentHeight();
        maxScroll = Math.max(0, totalContentHeight - viewportHeight);

        if (scrollOffset > maxScroll) scrollOffset = maxScroll;
    }

    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick)
    {
        if (visibility != VISIBLE) return;

        if (backgroundColor != 0)
        {
            int a = (int) (((backgroundColor >>> 24) & 0xFF) * alpha);
            int color = (a << 24) | (backgroundColor & 0x00FFFFFF);
            guiGraphics.fill(x, y, x + measuredWidth, y + measuredHeight, color);
        }

        int clipRight = x + measuredWidth - paddingRight - SCROLLBAR_WIDTH;
        guiGraphics.enableScissor(x + paddingLeft, y + paddingTop, clipRight, y + measuredHeight - paddingBottom);

        final boolean pointerInside = isInViewport(mouseX, mouseY);
        final int childMouseX = pointerInside ? mouseX : OUTSIDE_POINTER;
        final int childMouseY = pointerInside ? mouseY : OUTSIDE_POINTER;

        for (View child : children)
        {
            child.render(guiGraphics, childMouseX, childMouseY, partialTick);
        }

        guiGraphics.disableScissor();

        if (maxScroll > 0)
        {
            renderScrollbar(guiGraphics);
        }

        int b = MoBendsTheme.BORDER;
        guiGraphics.fill(x, y, x + measuredWidth, y + 1, b);
        guiGraphics.fill(x, y + measuredHeight - 1, x + measuredWidth, y + measuredHeight, b);
        guiGraphics.fill(x, y, x + 1, y + measuredHeight, b);
        guiGraphics.fill(x + measuredWidth - 1, y, x + measuredWidth, y + measuredHeight, b);
    }

    private void renderScrollbar(GuiGraphics guiGraphics)
    {
        int trackLeft = x + measuredWidth - SCROLLBAR_WIDTH;
        int trackTop = y + paddingTop;
        int trackHeight = getContentHeight();

        guiGraphics.fill(trackLeft, trackTop, trackLeft + SCROLLBAR_WIDTH, trackTop + trackHeight,
                MoBendsTheme.SCROLLBAR_TRACK);

        int thumbTop = thumbTop();

        guiGraphics.fill(trackLeft, thumbTop, trackLeft + SCROLLBAR_WIDTH, thumbTop + thumbHeight(),
                MoBendsTheme.SCROLLBAR_THUMB);
    }

    private int thumbHeight()
    {
        int trackHeight = getContentHeight();
        return Math.max(10, (int) ((float) trackHeight / (maxScroll + trackHeight) * trackHeight));
    }

    private int thumbTop()
    {
        return y + paddingTop + (int) ((float) scrollOffset / maxScroll * (getContentHeight() - thumbHeight()));
    }

    private void dragThumbTo(double mouseY)
    {
        int range = getContentHeight() - thumbHeight();
        if (range <= 0) return;

        scrollTo((int) Math.round((mouseY - thumbGrabOffset - y - paddingTop) / range * maxScroll));
    }

    public boolean handleMouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY)
    {
        if (draggingThumb)
        {
            dragThumbTo(mouseY);
            return true;
        }

        return super.handleMouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    public void handleMouseReleased(double mouseX, double mouseY, int button)
    {
        draggingThumb = false;
        super.handleMouseReleased(mouseX, mouseY, button);
    }

    public boolean handleMouseScrolled(double mouseX, double mouseY, double scrollY)
    {
        if (visibility != VISIBLE) return false;
        if (!isInBounds(mouseX, mouseY)) return false;

        for (int i = children.size() - 1; i >= 0; i--)
        {
            if (children.get(i).handleMouseScrolled(mouseX, mouseY, scrollY)) return true;
        }

        if (maxScroll > 0)
        {
            scrollTo(scrollOffset - (int) (scrollY * 10));
            return true;
        }

        return false;
    }

    public boolean handleClick(double mouseX, double mouseY, int button)
    {
        if (visibility != VISIBLE || !enabled) return false;
        if (!isInBounds(mouseX, mouseY)) return false;

        if (maxScroll > 0 && button == 0 && mouseX >= x + measuredWidth - SCROLLBAR_WIDTH)
        {
            int thumbTop = thumbTop();
            int thumbHeight = thumbHeight();

            thumbGrabOffset = mouseY >= thumbTop && mouseY < thumbTop + thumbHeight
                    ? mouseY - thumbTop
                    : thumbHeight / 2.0;
            draggingThumb = true;
            dragThumbTo(mouseY);
            return true;
        }

        if (isInViewport(mouseX, mouseY))
        {
            for (int i = children.size() - 1; i >= 0; i--)
            {
                if (children.get(i).handleClick(mouseX, mouseY, button)) return true;
            }
        }

        return super.handleClick(mouseX, mouseY, button);
    }

    private boolean isInViewport(double pointerX, double pointerY)
    {
        int clipRight = x + measuredWidth - paddingRight - SCROLLBAR_WIDTH;

        return pointerX >= x + paddingLeft && pointerX < clipRight
                && pointerY >= y + paddingTop && pointerY < y + measuredHeight - paddingBottom;
    }
}
