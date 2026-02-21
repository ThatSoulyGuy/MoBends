package goblinbob.mobends.core.client.gui.vanilla;

import goblinbob.mobends.api.gui.ILayoutParams;
import goblinbob.mobends.api.gui.view.IView;
import net.minecraft.client.gui.GuiGraphics;

import javax.annotation.Nullable;

public class VanillaView implements IView
{
    protected int x, y;
    protected int measuredWidth, measuredHeight;

    protected int id;
    protected int visibility = VISIBLE;
    protected boolean enabled = true;
    protected float alpha = 1.0f;
    protected int backgroundColor;
    protected int minWidth, minHeight;
    protected int paddingLeft, paddingTop, paddingRight, paddingBottom;

    @Nullable
    protected VanillaLayoutParams layoutParams;
    @Nullable
    protected Runnable clickListener;
    @Nullable
    protected Object background;

    @Override
    public void setId(int id) { this.id = id; }

    @Override
    public int getId() { return id; }

    @Override
    public void setLayoutParams(ILayoutParams params)
    {
        if (params instanceof VanillaLayoutParams vlp)
        {
            this.layoutParams = vlp;
        }
    }

    @Nullable
    @Override
    public ILayoutParams getLayoutParams() { return layoutParams; }

    @Override
    public void setPadding(int left, int top, int right, int bottom)
    {
        this.paddingLeft = left;
        this.paddingTop = top;
        this.paddingRight = right;
        this.paddingBottom = bottom;
    }

    @Override
    public int getWidth() { return measuredWidth; }

    @Override
    public int getHeight() { return measuredHeight; }

    @Override
    public void setMinimumWidth(int minWidth) { this.minWidth = minWidth; }

    @Override
    public void setMinimumHeight(int minHeight) { this.minHeight = minHeight; }

    @Override
    public void setVisibility(int visibility) { this.visibility = visibility; }

    @Override
    public int getVisibility() { return visibility; }

    @Override
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    @Override
    public boolean isEnabled() { return enabled; }

    @Override
    public void setAlpha(float alpha) { this.alpha = alpha; }

    @Override
    public float getAlpha() { return alpha; }

    @Override
    public void setBackground(@Nullable Object drawable)
    {
        this.background = drawable;
        if (drawable instanceof Integer color)
        {
            this.backgroundColor = color;
        }
    }

    @Override
    public void setBackgroundColor(int color) { this.backgroundColor = color; }

    @Override
    public void setOnClickListener(@Nullable Runnable listener) { this.clickListener = listener; }

    @Override
    public Object getNativeView() { return this; }

    // --- Layout engine ---

    public void measure(int availableWidth, int availableHeight)
    {
        int w = resolveSize(layoutParams != null ? layoutParams.getWidth() : ILayoutParams.WRAP_CONTENT,
                availableWidth, minWidth + paddingLeft + paddingRight);
        int h = resolveSize(layoutParams != null ? layoutParams.getHeight() : ILayoutParams.WRAP_CONTENT,
                availableHeight, minHeight + paddingTop + paddingBottom);
        measuredWidth = w;
        measuredHeight = h;
    }

    protected int resolveSize(int spec, int available, int contentSize)
    {
        if (spec == ILayoutParams.MATCH_PARENT)
        {
            // Inside a ScrollView, available can be Integer.MAX_VALUE/2.
            // MATCH_PARENT should fall back to content size in that case.
            return (available > 100000) ? Math.max(contentSize, 0) : available;
        }
        if (spec == ILayoutParams.WRAP_CONTENT) return Math.max(contentSize, 0);
        return spec;
    }

    public void layout(int left, int top, int right, int bottom)
    {
        this.x = left;
        this.y = top;
        this.measuredWidth = right - left;
        this.measuredHeight = bottom - top;
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
    }

    public boolean handleClick(double mouseX, double mouseY, int button)
    {
        if (visibility != VISIBLE || !enabled) return false;
        if (!isInBounds(mouseX, mouseY)) return false;
        if (clickListener != null && button == 0)
        {
            clickListener.run();
            return true;
        }
        return false;
    }

    public boolean handleMouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY)
    {
        return false;
    }

    public boolean handleMouseScrolled(double mouseX, double mouseY, double scrollY)
    {
        return false;
    }

    public void handleMouseReleased(double mouseX, double mouseY, int button)
    {
        // Override in subclasses
    }

    public boolean handleKeyPressed(int keyCode, int scanCode, int modifiers)
    {
        return false;
    }

    public boolean handleCharTyped(char ch, int modifiers)
    {
        return false;
    }

    public boolean isInBounds(double mx, double my)
    {
        return mx >= x && mx < x + measuredWidth && my >= y && my < y + measuredHeight;
    }

    protected int getContentLeft() { return x + paddingLeft; }

    protected int getContentTop() { return y + paddingTop; }

    protected int getContentWidth() { return measuredWidth - paddingLeft - paddingRight; }

    protected int getContentHeight() { return measuredHeight - paddingTop - paddingBottom; }
}
