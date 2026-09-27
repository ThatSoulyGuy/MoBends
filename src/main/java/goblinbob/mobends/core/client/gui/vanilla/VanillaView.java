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

    protected int visibility = VISIBLE;
    protected boolean enabled = true;
    protected float alpha = 1.0f;

    private float alphaFrom = 1.0f;
    private float alphaTo = 1.0f;
    private long alphaStartMs;
    private int alphaDurationMs;
    private boolean alphaAnimating;

    protected int backgroundColor;
    protected int minWidth, minHeight;
    protected int paddingLeft, paddingTop, paddingRight, paddingBottom;

    protected VanillaLayoutParams layoutParams = new VanillaLayoutParams(VanillaLayoutParams.WRAP_CONTENT, VanillaLayoutParams.WRAP_CONTENT);
    @Nullable
    protected Runnable clickListener;

    public void setLayoutParams(VanillaLayoutParams params)
    {
        this.layoutParams = params;
    }

    public void setPadding(int left, int top, int right, int bottom)
    {
        this.paddingLeft = left;
        this.paddingTop = top;
        this.paddingRight = right;
        this.paddingBottom = bottom;
    }

    public int getWidth() { return measuredWidth; }

    public int getHeight() { return measuredHeight; }

    public int getTop() { return y; }

    public void setMinimumWidth(int minWidth) { this.minWidth = minWidth; }

    public void setVisibility(int visibility) { this.visibility = visibility; }

    public int getVisibility() { return visibility; }

    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public void setAlpha(float alpha)
    {
        this.alpha = alpha;
        this.alphaAnimating = false;
    }

    public void setBackgroundColor(int color) { this.backgroundColor = color; }

    public void setOnClickListener(@Nullable Runnable listener) { this.clickListener = listener; }

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

    public void measure(int availableWidth, int availableHeight)
    {
        int w = resolveSize(layoutParams.getWidth(),
                availableWidth, minWidth + paddingLeft + paddingRight);
        int h = resolveSize(layoutParams.getHeight(),
                availableHeight, minHeight + paddingTop + paddingBottom);
        measuredWidth = w;
        measuredHeight = h;
    }

    protected int resolveSize(int spec, int available, int contentSize)
    {
        if (spec == VanillaLayoutParams.MATCH_PARENT)
        {
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
            GuiSound.playClick();
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

    protected int getContentWidth() { return measuredWidth - paddingLeft - paddingRight; }

    protected int getContentHeight() { return measuredHeight - paddingTop - paddingBottom; }
}
