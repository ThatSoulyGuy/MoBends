package goblinbob.mobends.core.client.gui.view;

import goblinbob.mobends.core.client.gui.theme.MoBendsTheme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class TextField extends View
{
    private String text = "";
    private String hint;
    private int textColor = MoBendsTheme.TEXT_PRIMARY;
    @Nullable
    private Consumer<String> textChangedListener;
    @Nullable
    private EditBox editBox;
    private int maxLength = 256;
    private Runnable submitListener;

    private static final int KEY_ENTER = 257;
    private static final int KEY_KP_ENTER = 335;

    public TextField(String hint)
    {
        this.hint = hint;
    }

    private void ensureEditBox()
    {
        if (editBox == null && measuredWidth > 0 && measuredHeight > 0)
        {
            var font = Minecraft.getInstance().font;
            editBox = new EditBox(font, x + 4, y + (measuredHeight - font.lineHeight) / 2,
                    measuredWidth - 8, font.lineHeight, Component.literal(hint));
            editBox.setMaxLength(maxLength);
            editBox.setValue(text);
            editBox.setTextColor(textColor);
            editBox.setBordered(false);
            if (hint != null && !hint.isEmpty())
            {
                editBox.setHint(Component.literal(hint));
            }
            editBox.setResponder(newText -> {
                text = newText;
                if (textChangedListener != null)
                {
                    textChangedListener.accept(newText);
                }
            });
        }
    }

    public void setText(String text)
    {
        this.text = text;
        if (editBox != null)
        {
            editBox.setValue(text);
        }
    }

    public String getText() { return text; }

    public void setTextColor(int color)
    {
        this.textColor = color;
        if (editBox != null)
        {
            editBox.setTextColor(color);
        }
    }

    public void setTextSize(float sizeSp) {  }

    public void setOnTextChangedListener(Consumer<String> listener)
    {
        this.textChangedListener = listener;
    }

    public void setOnSubmitListener(Runnable listener)
    {
        this.submitListener = listener;
    }

    public void setMaxLength(int maxLength)
    {
        this.maxLength = maxLength;
        if (editBox != null)
        {
            editBox.setMaxLength(maxLength);
        }
    }

    public void layout(int left, int top, int right, int bottom)
    {
        super.layout(left, top, right, bottom);
        if (editBox != null)
        {
            var font = Minecraft.getInstance().font;
            editBox.setX(x + 4);
            editBox.setY(y + (measuredHeight - font.lineHeight) / 2);
            editBox.setWidth(measuredWidth - 8);
        }
    }

    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick)
    {
        if (visibility != VISIBLE) return;

        int bgColor = backgroundColor != 0 ? backgroundColor : MoBendsTheme.BG_LIST;
        guiGraphics.fill(x, y, x + measuredWidth, y + measuredHeight, bgColor);

        boolean isFocused = editBox != null && editBox.isFocused();
        int borderColor = isFocused ? MoBendsTheme.BORDER_FOCUSED : MoBendsTheme.BORDER;
        guiGraphics.fill(x, y, x + measuredWidth, y + 1, borderColor);
        guiGraphics.fill(x, y + measuredHeight - 1, x + measuredWidth, y + measuredHeight, borderColor);
        guiGraphics.fill(x, y, x + 1, y + measuredHeight, borderColor);
        guiGraphics.fill(x + measuredWidth - 1, y, x + measuredWidth, y + measuredHeight, borderColor);

        ensureEditBox();
        if (editBox != null)
        {
            editBox.render(guiGraphics, mouseX, mouseY, partialTick);
        }
    }

    public boolean handleClick(double mouseX, double mouseY, int button)
    {
        if (visibility != VISIBLE || !enabled) return false;

        ensureEditBox();
        if (editBox == null) return false;

        boolean inBounds = isInBounds(mouseX, mouseY);
        if (inBounds)
        {
            editBox.mouseClicked(mouseX, mouseY, button);
            editBox.setFocused(true);
            return true;
        }

        editBox.setFocused(false);
        return false;
    }

    public boolean handleKeyPressed(int keyCode, int scanCode, int modifiers)
    {
        if (editBox != null && editBox.isFocused())
        {
            if (submitListener != null && (keyCode == KEY_ENTER || keyCode == KEY_KP_ENTER))
            {
                submitListener.run();
                return true;
            }
            return editBox.keyPressed(keyCode, scanCode, modifiers);
        }
        return false;
    }

    public boolean handleCharTyped(char ch, int modifiers)
    {
        if (editBox != null && editBox.isFocused())
        {
            return editBox.charTyped(ch, modifiers);
        }
        return false;
    }

    public void measure(int availableWidth, int availableHeight)
    {
        int lpW = layoutParams.getWidth();
        int lpH = layoutParams.getHeight();

        var font = Minecraft.getInstance().font;
        int contentW = 100 + paddingLeft + paddingRight;
        int contentH = font.lineHeight + paddingTop + paddingBottom + 8;

        measuredWidth = resolveSize(lpW, availableWidth, Math.max(contentW, minWidth));
        measuredHeight = resolveSize(lpH, availableHeight, Math.max(contentH, minHeight));
    }
}
