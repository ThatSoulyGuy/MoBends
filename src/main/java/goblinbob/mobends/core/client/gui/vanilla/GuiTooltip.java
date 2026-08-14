package goblinbob.mobends.core.client.gui.vanilla;

import goblinbob.mobends.core.client.gui.theme.MoBendsTheme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import javax.annotation.Nullable;
import java.util.List;

public final class GuiTooltip
{
    private static final int MAX_WIDTH = 200;
    private static final int PADDING_X = 4;
    private static final int PADDING_Y = 4;
    private static final int LINE_HEIGHT = 10;
    private static final int CURSOR_OFFSET_X = 10;
    private static final int CURSOR_OFFSET_Y = 10;
    private static final int SCREEN_MARGIN = 4;
    private static final int Z_OFFSET = 400;

    @Nullable
    private static String pending;

    private GuiTooltip()
    {
    }

    public static void request(@Nullable String text)
    {
        pending = text;
    }

    public static void clear()
    {
        pending = null;
    }

    public static void renderPending(GuiGraphics guiGraphics, int mouseX, int mouseY)
    {
        if (pending == null || pending.isEmpty()) return;

        final Font font = Minecraft.getInstance().font;
        final List<FormattedCharSequence> lines = font.split(Component.literal(pending), MAX_WIDTH);
        pending = null;

        if (lines.isEmpty()) return;

        int textWidth = 0;
        for (FormattedCharSequence line : lines)
        {
            textWidth = Math.max(textWidth, font.width(line));
        }

        final int boxWidth = textWidth + PADDING_X * 2;
        final int boxHeight = lines.size() * LINE_HEIGHT - (LINE_HEIGHT - font.lineHeight) + PADDING_Y * 2;

        final int screenWidth = guiGraphics.guiWidth();
        final int screenHeight = guiGraphics.guiHeight();

        int boxX = mouseX + CURSOR_OFFSET_X;
        if (boxX + boxWidth + SCREEN_MARGIN > screenWidth)
        {
            boxX = Math.max(SCREEN_MARGIN, mouseX - CURSOR_OFFSET_X - boxWidth);
        }

        int boxY = mouseY - CURSOR_OFFSET_Y;
        if (boxY + boxHeight + SCREEN_MARGIN > screenHeight)
        {
            boxY = screenHeight - boxHeight - SCREEN_MARGIN;
        }
        boxY = Math.max(SCREEN_MARGIN, boxY);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0.0F, 0.0F, Z_OFFSET);

        guiGraphics.fill(boxX, boxY, boxX + boxWidth, boxY + boxHeight, MoBendsTheme.BG_PANEL);
        guiGraphics.fill(boxX, boxY, boxX + boxWidth, boxY + 1, MoBendsTheme.BORDER);
        guiGraphics.fill(boxX, boxY + boxHeight - 1, boxX + boxWidth, boxY + boxHeight, MoBendsTheme.BORDER);
        guiGraphics.fill(boxX, boxY, boxX + 1, boxY + boxHeight, MoBendsTheme.BORDER);
        guiGraphics.fill(boxX + boxWidth - 1, boxY, boxX + boxWidth, boxY + boxHeight, MoBendsTheme.BORDER);

        int lineY = boxY + PADDING_Y;
        for (FormattedCharSequence line : lines)
        {
            guiGraphics.drawString(font, line, boxX + PADDING_X, lineY, MoBendsTheme.TEXT_PRIMARY, true);
            lineY += LINE_HEIGHT;
        }

        guiGraphics.pose().popPose();
    }
}
