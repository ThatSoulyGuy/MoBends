package goblinbob.mobends.core.client.gui.vanilla;

import goblinbob.mobends.api.gui.ILayoutParams;
import goblinbob.mobends.api.gui.view.IScrollView;
import goblinbob.mobends.core.client.gui.theme.MoBendsTheme;
import net.minecraft.client.gui.GuiGraphics;

public class VanillaScrollView extends VanillaViewGroup implements IScrollView
{
    protected int scrollOffset = 0;
    protected int maxScroll = 0;
    private boolean verticalScrollBarEnabled = true;

    private static final int SCROLLBAR_WIDTH = 4;

    @Override
    public void scrollTo(int y)
    {
        this.scrollOffset = Math.max(0, Math.min(y, maxScroll));
        relayoutChildren();
    }

    @Override
    public void scrollBy(int dy)
    {
        scrollTo(scrollOffset + dy);
    }

    @Override
    public void smoothScrollTo(int y)
    {
        scrollTo(y);
    }

    @Override
    public int getScrollY() { return scrollOffset; }

    @Override
    public void setVerticalScrollBarEnabled(boolean visible)
    {
        this.verticalScrollBarEnabled = visible;
    }

    @Override
    public void setOverScrollEnabled(boolean enabled)
    {
        // No-op
    }

    @Override
    public void measure(int availableWidth, int availableHeight)
    {
        int lpW = layoutParams != null ? layoutParams.getWidth() : ILayoutParams.WRAP_CONTENT;
        int lpH = layoutParams != null ? layoutParams.getHeight() : ILayoutParams.WRAP_CONTENT;

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

    @Override
    public void layout(int left, int top, int right, int bottom)
    {
        super.layout(left, top, right, bottom);
        relayoutChildren();
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
            if (clp != null && clp.getWidth() == ILayoutParams.MATCH_PARENT)
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

    @Override
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

        for (VanillaView child : children)
        {
            child.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        guiGraphics.disableScissor();

        if (verticalScrollBarEnabled && maxScroll > 0)
        {
            renderScrollbar(guiGraphics);
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

    @Override
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

    @Override
    public boolean handleClick(double mouseX, double mouseY, int button)
    {
        if (visibility != VISIBLE || !enabled) return false;
        if (!isInBounds(mouseX, mouseY)) return false;

        for (int i = children.size() - 1; i >= 0; i--)
        {
            if (children.get(i).handleClick(mouseX, mouseY, button)) return true;
        }

        return super.handleClick(mouseX, mouseY, button);
    }
}
