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

    private int toggleWidth = 20;
    private int toggleHeight = 10;

    @Nullable
    private String tooltip;

    public VanillaToggle(boolean initialState)
    {
        this.checked = initialState;
    }

    public void setToggleSize(int toggleWidth, int toggleHeight)
    {
        this.toggleWidth = toggleWidth;
        this.toggleHeight = toggleHeight;
    }

    public void setTooltip(@Nullable String tooltip)
    {
        this.tooltip = tooltip;
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
            GuiSound.playClick();
            toggle();
            return true;
        }
        return false;
    }

    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick)
    {
        if (visibility != VISIBLE) return;

        boolean hovered = enabled && isInBounds(mouseX, mouseY);

        guiGraphics.pose().pushPose();

        if (hovered)
        {
            float centerX = x + measuredWidth / 2.0F;
            float centerY = y + measuredHeight / 2.0F;

            guiGraphics.pose().translate(centerX, centerY, 0.0F);
            guiGraphics.pose().scale(1.03F, 1.03F, 1.0F);
            guiGraphics.pose().translate(-centerX, -centerY, 0.0F);
        }

        if (backgroundColor != 0)
        {
            guiGraphics.fill(x, y, x + measuredWidth, y + measuredHeight,
                    hovered ? MoBendsTheme.BG_LIST_ITEM_SELECTED : backgroundColor);
        }

        if (hovered && tooltip != null)
        {
            GuiTooltip.request(tooltip);
        }

        int toggleX = x + measuredWidth - toggleWidth - paddingRight - 2;
        int toggleY = y + (measuredHeight - toggleHeight) / 2;
        int trackColor = checked ? MoBendsTheme.TOGGLE_ON : MoBendsTheme.TOGGLE_OFF;
        guiGraphics.fill(toggleX, toggleY, toggleX + toggleWidth, toggleY + toggleHeight, trackColor);

        int thumbSize = toggleHeight - 2;
        int thumbX = checked ? toggleX + toggleWidth - thumbSize - 1 : toggleX + 1;
        int thumbY = toggleY + 1;
        guiGraphics.fill(thumbX, thumbY, thumbX + thumbSize, thumbY + thumbSize, 0xFFFFFFFF);

        if (text != null && !text.isEmpty())
        {
            var font = Minecraft.getInstance().font;
            int textX = x + paddingLeft;
            int textY = y + (measuredHeight - font.lineHeight) / 2;
            int color = enabled ? MoBendsTheme.TEXT_PRIMARY : MoBendsTheme.TEXT_DISABLED;
            guiGraphics.drawString(font, text, textX, textY, color, true);
        }

        guiGraphics.pose().popPose();
    }

    public void measure(int availableWidth, int availableHeight)
    {
        int lpW = layoutParams != null ? layoutParams.getWidth() : VanillaLayoutParams.WRAP_CONTENT;
        int lpH = layoutParams != null ? layoutParams.getHeight() : VanillaLayoutParams.WRAP_CONTENT;

        var font = Minecraft.getInstance().font;
        int contentW = toggleWidth + paddingLeft + paddingRight + 4;
        if (text != null && !text.isEmpty())
        {
            contentW += font.width(text) + 4;
        }
        int contentH = Math.max(toggleHeight, font.lineHeight) + paddingTop + paddingBottom + 4;

        measuredWidth = resolveSize(lpW, availableWidth, Math.max(contentW, minWidth));
        measuredHeight = resolveSize(lpH, availableHeight, Math.max(contentH, minHeight));
    }
}
