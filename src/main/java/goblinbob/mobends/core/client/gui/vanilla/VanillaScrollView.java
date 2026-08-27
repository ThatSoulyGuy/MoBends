package goblinbob.mobends.core.client.gui.vanilla;

import goblinbob.mobends.core.client.gui.theme.MoBendsTheme;
import net.minecraft.client.gui.GuiGraphics;

public class VanillaScrollView extends VanillaViewGroup
{
    protected int scrollOffset = 0;
    protected int maxScroll = 0;
    private boolean verticalScrollBarEnabled = true;

    private int targetScroll = 0;
    private boolean animatingScroll = false;

    private static final int SCROLLBAR_WIDTH = 4;
    private static final int OUTSIDE_POINTER = Integer.MIN_VALUE / 2;
    private static final boolean SHOW_BORDER = true;

    public void scrollTo(int y)
    {
        this.animatingScroll = false;
        this.scrollOffset = Math.max(0, Math.min(y, maxScroll));
        relayoutChildren();
    }

    public void scrollBy(int dy)
    {
        scrollTo(scrollOffset + dy);
    }

    public void smoothScrollTo(int y)
    {
        this.targetScroll = y;
        this.animatingScroll = true;
    }

    public int getScrollY() { return scrollOffset; }

    public void setVerticalScrollBarEnabled(boolean visible)
    {
        this.verticalScrollBarEnabled = visible;
    }

    public void setOverScrollEnabled(boolean enabled)
    {
    }

    public void measure(int availableWidth, int availableHeight)
    {
        int lpW = layoutParams != null ? layoutParams.getWidth() : VanillaLayoutParams.WRAP_CONTENT;
        int lpH = layoutParams != null ? layoutParams.getHeight() : VanillaLayoutParams.WRAP_CONTENT;

        measuredWidth = resolveSize(lpW, availableWidth, availableWidth);
        measuredHeight = resolveSize(lpH, availableHeight, availableHeight);

        int contentW = measuredWidth - paddingLeft - paddingRight;
        if (verticalScrollBarEnabled) contentW -= SCROLLBAR_WIDTH;

        for (VanillaView child : children)
        {
            if (child.visibility == GONE) continue;
            VanillaLayoutParams clp = child.layoutParams;
            int ml = 0, mr = 0;
            if (clp != null)
            {
                ml = clp.getMarginLeft();
                mr = clp.getMarginRight();
            }
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
        if (verticalScrollBarEnabled) contentW -= SCROLLBAR_WIDTH;

        int totalContentHeight = 0;

        for (VanillaView child : children)
        {
            if (child.visibility == GONE) continue;
            VanillaLayoutParams clp = child.layoutParams;
            int ml = 0, mt = 0, mr = 0, mb = 0;
            if (clp != null)
            {
                ml = clp.getMarginLeft();
                mt = clp.getMarginTop();
                mr = clp.getMarginRight();
                mb = clp.getMarginBottom();
            }

            int childW = child.measuredWidth;
            if (clp != null && clp.getWidth() == VanillaLayoutParams.MATCH_PARENT)
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

        int clipRight = x + measuredWidth - paddingRight - (verticalScrollBarEnabled ? SCROLLBAR_WIDTH : 0);
        guiGraphics.enableScissor(x + paddingLeft, y + paddingTop, clipRight, y + measuredHeight - paddingBottom);

        final boolean pointerInside = isInViewport(mouseX, mouseY);
        final int childMouseX = pointerInside ? mouseX : OUTSIDE_POINTER;
        final int childMouseY = pointerInside ? mouseY : OUTSIDE_POINTER;

        for (VanillaView child : children)
        {
            child.render(guiGraphics, childMouseX, childMouseY, partialTick);
        }

        guiGraphics.disableScissor();

        if (verticalScrollBarEnabled && maxScroll > 0)
        {
            renderScrollbar(guiGraphics);
        }

        if (SHOW_BORDER)
        {
            int b = MoBendsTheme.BORDER;
            guiGraphics.fill(x, y, x + measuredWidth, y + 1, b);
            guiGraphics.fill(x, y + measuredHeight - 1, x + measuredWidth, y + measuredHeight, b);
            guiGraphics.fill(x, y, x + 1, y + measuredHeight, b);
            guiGraphics.fill(x + measuredWidth - 1, y, x + measuredWidth, y + measuredHeight, b);
        }
    }

    private void renderScrollbar(GuiGraphics guiGraphics)
    {
        int trackLeft = x + measuredWidth - SCROLLBAR_WIDTH;
        int trackTop = y + paddingTop;
        int trackHeight = getContentHeight();

        guiGraphics.fill(trackLeft, trackTop, trackLeft + SCROLLBAR_WIDTH, trackTop + trackHeight,
                MoBendsTheme.SCROLLBAR_TRACK);

        int totalContentHeight = maxScroll + trackHeight;
        int thumbHeight = Math.max(10, (int) ((float) trackHeight / totalContentHeight * trackHeight));
        int thumbTop = trackTop + (int) ((float) scrollOffset / maxScroll * (trackHeight - thumbHeight));

        guiGraphics.fill(trackLeft, thumbTop, trackLeft + SCROLLBAR_WIDTH, thumbTop + thumbHeight,
                MoBendsTheme.SCROLLBAR_THUMB);
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
        int clipRight = x + measuredWidth - paddingRight - (verticalScrollBarEnabled ? SCROLLBAR_WIDTH : 0);

        return pointerX >= x + paddingLeft && pointerX < clipRight
                && pointerY >= y + paddingTop && pointerY < y + measuredHeight - paddingBottom;
    }
}
