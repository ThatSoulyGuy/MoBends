package goblinbob.mobends.core.client.gui.vanilla;

import goblinbob.mobends.core.client.gui.theme.MoBendsTheme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class VanillaToggle extends VanillaView
{
    private boolean checked;
    private String text = "";
    @Nullable
    private Consumer<Boolean> checkedChangeListener;

    private static final int TOGGLE_WIDTH = 20;
    private static final int TOGGLE_HEIGHT = 10;

    public VanillaToggle(boolean initialState)
    {
        this.checked = initialState;
    }

    public void setChecked(boolean checked) { this.checked = checked; }

    public boolean isChecked() { return checked; }

    public void toggle()
    {
        setChecked(!checked);
        if (checkedChangeListener != null)
        {
            checkedChangeListener.accept(checked);
        }
    }

    public void setOnCheckedChangeListener(Consumer<Boolean> listener)
    {
        this.checkedChangeListener = listener;
    }

    public void setText(String text) { this.text = text; }

    public String getText() { return text; }

    public boolean handleClick(double mouseX, double mouseY, int button)
    {
        if (visibility != VISIBLE || !enabled) return false;
        if (!isInBounds(mouseX, mouseY)) return false;
        if (button == 0)
        {
            toggle();
            return true;
        }
        return false;
    }

    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick)
    {
        if (visibility != VISIBLE) return;

        if (backgroundColor != 0)
        {
            guiGraphics.fill(x, y, x + measuredWidth, y + measuredHeight, backgroundColor);
        }

        // Draw toggle track
        int toggleX = x + measuredWidth - TOGGLE_WIDTH - 2;
        int toggleY = y + (measuredHeight - TOGGLE_HEIGHT) / 2;
        int trackColor = checked ? MoBendsTheme.TOGGLE_ON : MoBendsTheme.TOGGLE_OFF;
        guiGraphics.fill(toggleX, toggleY, toggleX + TOGGLE_WIDTH, toggleY + TOGGLE_HEIGHT, trackColor);

        // Draw toggle thumb
        int thumbSize = TOGGLE_HEIGHT - 2;
        int thumbX = checked ? toggleX + TOGGLE_WIDTH - thumbSize - 1 : toggleX + 1;
        int thumbY = toggleY + 1;
        guiGraphics.fill(thumbX, thumbY, thumbX + thumbSize, thumbY + thumbSize, 0xFFFFFFFF);

        // Draw text
        if (text != null && !text.isEmpty())
        {
            var font = Minecraft.getInstance().font;
            int textX = x + paddingLeft;
            int textY = y + (measuredHeight - font.lineHeight) / 2;
            int color = enabled ? MoBendsTheme.TEXT_PRIMARY : MoBendsTheme.TEXT_DISABLED;
            guiGraphics.drawString(font, text, textX, textY, color, true);
        }
    }

    public void measure(int availableWidth, int availableHeight)
    {
        int lpW = layoutParams != null ? layoutParams.getWidth() : VanillaLayoutParams.WRAP_CONTENT;
        int lpH = layoutParams != null ? layoutParams.getHeight() : VanillaLayoutParams.WRAP_CONTENT;

        var font = Minecraft.getInstance().font;
        int contentW = TOGGLE_WIDTH + paddingLeft + paddingRight + 4;
        if (text != null && !text.isEmpty())
        {
            contentW += font.width(text) + 4;
        }
        int contentH = Math.max(TOGGLE_HEIGHT, font.lineHeight) + paddingTop + paddingBottom + 4;

        measuredWidth = resolveSize(lpW, availableWidth, Math.max(contentW, minWidth));
        measuredHeight = resolveSize(lpH, availableHeight, Math.max(contentH, minHeight));
    }
}