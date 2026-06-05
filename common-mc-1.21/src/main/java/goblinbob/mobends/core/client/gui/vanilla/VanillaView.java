package goblinbob.mobends.core.client.gui.vanilla;

import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;

import javax.annotation.Nullable;

public class VanillaView
{
    public static final int VISIBLE = 0;
    public static final int INVISIBLE = 4;
    public static final int GONE = 8;

    protected int x, y;
    protected int measuredWidth, measuredHeight;

    protected int id;
    protected int visibility = VISIBLE;
    protected boolean enabled = true;
    protected float alpha = 1.0f;

    // Alpha tween state
    private float alphaFrom = 1.0f;
    private float alphaTo = 1.0f;
    private long alphaStartMs;
    private int alphaDurationMs;
    private boolean alphaAnimating;

    protected int backgroundColor;
    protected int minWidth, minHeight;
    protected int paddingLeft, paddingTop, paddingRight, paddingBottom;

    @Nullable
    protected VanillaLayoutParams layoutParams;
    @Nullable
    protected Runnable clickListener;
    @Nullable
    protected Object background;

    public void setId(int id) { this.id = id; }

    public int getId() { return id; }

    public void setLayoutParams(VanillaLayoutParams params)
    {
        this.layoutParams = params;
    }

    @Nullable
    public VanillaLayoutParams getLayoutParams() { return layoutParams; }

    public void setPadding(int left, int top, int right, int bottom)
    {
        this.paddingLeft = left;
        this.paddingTop = top;
        this.paddingRight = right;
        this.paddingBottom = bottom;
    }

    public int getWidth() { return measuredWidth; }

    public int getHeight() { return measuredHeight; }

    public void setMinimumWidth(int minWidth) { this.minWidth = minWidth; }

    public void setMinimumHeight(int minHeight) { this.minHeight = minHeight; }

    public void setVisibility(int visibility) { this.visibility = visibility; }

    public int getVisibility() { return visibility; }

    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public boolean isEnabled() { return enabled; }

    public void setAlpha(float alpha)
    {
        this.alpha = alpha;
        this.alphaAnimating = false;
    }

    public float getAlpha() { return alpha; }

    public void setBackground(@Nullable Object drawable)
    {
        this.background = drawable;
        if (drawable instanceof Integer color)
        {
            this.backgroundColor = color;
        }
    }

    public void setBackgroundColor(int color) { this.backgroundColor = color; }

    public void setOnClickListener(@Nullable Runnable listener) { this.clickListener = listener; }

    /**
     * Tweens the view's alpha to the target value over the given duration (linear).
     * Advanced each frame from {@link #layout}.
     */
    public void animateAlpha(float targetAlpha, int durationMs)
    {
        if (durationMs <= 0)
        {
            setAlpha(targetAlpha);
            return;
        }
        this.alphaFrom = this.alpha;
        this.alphaTo = targetAlpha;
        this.alphaStartMs = Util.getMillis();
        this.alphaDurationMs = durationMs;
        this.alphaAnimating = true;
    }

    /**
     * Advances time-based animations. Called once per frame from {@link #layout}.
     */
    protected void tickAnimations()
    {
        if (alphaAnimating)
        {
            long elapsed = Util.getMillis() - alphaStartMs;
            if (elapsed >= alphaDurationMs)
            {
                this.alpha = alphaTo;
                this.alphaAnimating = false;
            }
            else
            {
                float t = (float) elapsed / alphaDurationMs;
                this.alpha = alphaFrom + (alphaTo - alphaFrom) * t;
            }
        }
    }

    public Object getNativeView() { return this; }

    // --- Layout engine ---

    public void measure(int availableWidth, int availableHeight)
    {
        int w = resolveSize(layoutParams != null ? layoutParams.getWidth() : VanillaLayoutParams.WRAP_CONTENT,
                availableWidth, minWidth + paddingLeft + paddingRight);
        int h = resolveSize(layoutParams != null ? layoutParams.getHeight() : VanillaLayoutParams.WRAP_CONTENT,
                availableHeight, minHeight + paddingTop + paddingBottom);
        measuredWidth = w;
        measuredHeight = h;
    }

    protected int resolveSize(int spec, int available, int contentSize)
    {
        if (spec == VanillaLayoutParams.MATCH_PARENT)
        {
            // Inside a ScrollView, available can be Integer.MAX_VALUE/2.
            // MATCH_PARENT should fall back to content size in that case.
            return (available > 100000) ? Math.max(contentSize, 0) : available;
        }
        if (spec == VanillaLayoutParams.WRAP_CONTENT) return Math.max(contentSize, 0);
        return spec;
    }

    public void layout(int left, int top, int right, int bottom)
    {
        tickAnimations();
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