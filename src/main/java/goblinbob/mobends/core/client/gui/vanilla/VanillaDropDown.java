package goblinbob.mobends.core.client.gui.vanilla;

import goblinbob.mobends.core.client.gui.theme.MoBendsTheme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;

public class VanillaDropDown extends VanillaView implements GuiOverlay.Layer
{
    private static final int OPTION_HEIGHT = 14;
    private static final int TEXT_INSET = 4;
    private static final int ARROW_WIDTH = 8;
    private static final int MIN_POPUP_WIDTH = 60;

    private String label;
    private final List<String> options = new ArrayList<>();
    private final List<String> descriptions = new ArrayList<>();
    private int selectedIndex;
    private boolean expanded;

    @Nullable
    private IntConsumer selectionListener;

    public VanillaDropDown(String label)
    {
        this.label = label;
        this.backgroundColor = MoBendsTheme.BG_BUTTON;
    }

    public void setLabel(String label) { this.label = label; }

    public void addOption(String option) { addOption(option, null); }

    public void addOption(String option, @Nullable String description)
    {
        options.add(option);
        descriptions.add(description);
    }

    public void setSelectedIndex(int selectedIndex)
    {
        if (options.isEmpty()) return;
        this.selectedIndex = Math.max(0, Math.min(options.size() - 1, selectedIndex));
    }

    public int getSelectedIndex() { return selectedIndex; }

    public void setOnSelectionChanged(IntConsumer listener) { this.selectionListener = listener; }

    private int getPopupWidth()
    {
        Font font = Minecraft.getInstance().font;
        int widest = MIN_POPUP_WIDTH;
        for (String option : options)
        {
            widest = Math.max(widest, font.width(option) + TEXT_INSET * 2);
        }
        return Math.max(widest, measuredWidth);
    }

    private int getPopupHeight()
    {
        return options.size() * OPTION_HEIGHT + 2;
    }

    private boolean isInPopup(double mx, double my)
    {
        if (!expanded) return false;

        int popupTop = y + measuredHeight;
        return mx >= x && mx < x + getPopupWidth()
                && my >= popupTop && my < popupTop + getPopupHeight();
    }

    private int optionAt(double my)
    {
        int popupTop = y + measuredHeight + 1;
        int index = (int) ((my - popupTop) / OPTION_HEIGHT);
        return (index >= 0 && index < options.size()) ? index : -1;
    }

    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick)
    {
        if (visibility != VISIBLE)
        {
            expanded = false;
            GuiOverlay.release(this);
            return;
        }

        boolean hoveredButton = enabled && isInBounds(mouseX, mouseY);
        expanded = enabled && (hoveredButton || isInPopup(mouseX, mouseY));

        int bgColor = hoveredButton || expanded ? MoBendsTheme.BG_BUTTON_HOVER : backgroundColor;
        guiGraphics.fill(x, y, x + measuredWidth, y + measuredHeight, bgColor);

        int borderColor = expanded ? MoBendsTheme.BORDER_FOCUSED : MoBendsTheme.BORDER;
        guiGraphics.fill(x, y, x + measuredWidth, y + 1, borderColor);
        guiGraphics.fill(x, y + measuredHeight - 1, x + measuredWidth, y + measuredHeight, borderColor);
        guiGraphics.fill(x, y, x + 1, y + measuredHeight, borderColor);
        guiGraphics.fill(x + measuredWidth - 1, y, x + measuredWidth, y + measuredHeight, borderColor);

        Font font = Minecraft.getInstance().font;
        int textY = y + (measuredHeight - font.lineHeight) / 2;
        guiGraphics.drawString(font, label, x + TEXT_INSET, textY, MoBendsTheme.TEXT_PRIMARY, true);
        guiGraphics.drawString(font, expanded ? "▲" : "▼",
                x + measuredWidth - ARROW_WIDTH, textY, MoBendsTheme.TEXT_HINT, true);

        if (expanded)
        {
            GuiOverlay.request(this);
        }
        else
        {
            GuiOverlay.release(this);
        }
    }

    public void renderOverlay(GuiGraphics guiGraphics, int mouseX, int mouseY)
    {
        Font font = Minecraft.getInstance().font;

        int popupLeft = x;
        int popupTop = y + measuredHeight;
        int popupWidth = getPopupWidth();
        int popupHeight = getPopupHeight();

        guiGraphics.fill(popupLeft, popupTop, popupLeft + popupWidth, popupTop + popupHeight,
                MoBendsTheme.BG_PANEL);
        guiGraphics.fill(popupLeft, popupTop, popupLeft + popupWidth, popupTop + 1, MoBendsTheme.BORDER);
        guiGraphics.fill(popupLeft, popupTop + popupHeight - 1, popupLeft + popupWidth,
                popupTop + popupHeight, MoBendsTheme.BORDER);
        guiGraphics.fill(popupLeft, popupTop, popupLeft + 1, popupTop + popupHeight, MoBendsTheme.BORDER);
        guiGraphics.fill(popupLeft + popupWidth - 1, popupTop, popupLeft + popupWidth,
                popupTop + popupHeight, MoBendsTheme.BORDER);

        for (int i = 0; i < options.size(); i++)
        {
            int optionTop = popupTop + 1 + i * OPTION_HEIGHT;
            boolean hovered = mouseX >= popupLeft && mouseX < popupLeft + popupWidth
                    && mouseY >= optionTop && mouseY < optionTop + OPTION_HEIGHT;

            if (hovered)
            {
                guiGraphics.fill(popupLeft + 1, optionTop, popupLeft + popupWidth - 1,
                        optionTop + OPTION_HEIGHT, MoBendsTheme.BG_LIST_ITEM_HOVER);

                final String description = i < descriptions.size() ? descriptions.get(i) : null;
                if (description != null)
                {
                    GuiTooltip.request(description);
                }
            }

            int color = i == selectedIndex ? MoBendsTheme.TOGGLE_ON
                    : (hovered ? MoBendsTheme.TEXT_PRIMARY : MoBendsTheme.TEXT_SECONDARY);

            guiGraphics.drawString(font, options.get(i), popupLeft + TEXT_INSET,
                    optionTop + (OPTION_HEIGHT - font.lineHeight) / 2, color, true);
        }
    }

    public boolean handleOverlayClick(double mouseX, double mouseY, int button)
    {
        if (button != 0 || !isInPopup(mouseX, mouseY)) return false;

        int index = optionAt(mouseY);
        if (index < 0) return true;

        GuiSound.playClick();
        selectedIndex = index;

        if (selectionListener != null)
        {
            selectionListener.accept(index);
        }
        return true;
    }

    public boolean handleClick(double mouseX, double mouseY, int button)
    {
        if (visibility != VISIBLE || !enabled) return false;
        return isInBounds(mouseX, mouseY) && button == 0;
    }

    public void measure(int availableWidth, int availableHeight)
    {
        int lpW = layoutParams != null ? layoutParams.getWidth() : VanillaLayoutParams.WRAP_CONTENT;
        int lpH = layoutParams != null ? layoutParams.getHeight() : VanillaLayoutParams.WRAP_CONTENT;

        Font font = Minecraft.getInstance().font;
        int contentW = font.width(label) + TEXT_INSET * 2 + ARROW_WIDTH + 4;
        int contentH = font.lineHeight + paddingTop + paddingBottom + 8;

        measuredWidth = resolveSize(lpW, availableWidth, Math.max(contentW, minWidth));
        measuredHeight = resolveSize(lpH, availableHeight, Math.max(contentH, minHeight));
    }
}
