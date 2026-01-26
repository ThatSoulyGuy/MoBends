package goblinbob.mobends.core.client.gui.elements;

import goblinbob.mobends.core.util.Draw;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * A helper class for rendering tooltips in the MoBends GUI.
 */
public class GuiTooltip
{
    private static final int PADDING = 4;
    private static final int LINE_HEIGHT = 10;
    private static final int BG_COLOR = 0xEE1A1A1A;
    private static final int BORDER_COLOR = 0xFF444444;
    private static final int TEXT_COLOR = 0xFFFFFFFF;
    private static final int TEXT_COLOR_SECONDARY = 0xFFAAAAAA;

    @Nullable
    private static String pendingTooltip = null;
    @Nullable
    private static List<String> pendingTooltipLines = null;
    private static int tooltipX = 0;
    private static int tooltipY = 0;

    /**
     * Queues a simple tooltip to be rendered at the end of the frame.
     */
    public static void queueTooltip(String text, int x, int y)
    {
        pendingTooltip = text;
        pendingTooltipLines = null;
        tooltipX = x;
        tooltipY = y;
    }

    /**
     * Queues a multi-line tooltip to be rendered at the end of the frame.
     */
    public static void queueTooltip(List<String> lines, int x, int y)
    {
        pendingTooltip = null;
        pendingTooltipLines = new ArrayList<>(lines);
        tooltipX = x;
        tooltipY = y;
    }

    /**
     * Clears any pending tooltip.
     */
    public static void clearTooltip()
    {
        pendingTooltip = null;
        pendingTooltipLines = null;
    }

    /**
     * Renders the queued tooltip if any. Should be called at the end of the render pass.
     */
    public static void renderQueuedTooltip(GuiGraphics guiGraphics)
    {
        if (pendingTooltip != null)
        {
            renderTooltip(guiGraphics, pendingTooltip, tooltipX, tooltipY);
            pendingTooltip = null;
        }
        else if (pendingTooltipLines != null && !pendingTooltipLines.isEmpty())
        {
            renderTooltipMultiline(guiGraphics, pendingTooltipLines, tooltipX, tooltipY);
            pendingTooltipLines = null;
        }
    }

    /**
     * Renders a simple single-line tooltip.
     */
    public static void renderTooltip(GuiGraphics guiGraphics, String text, int x, int y)
    {
        Minecraft mc = Minecraft.getInstance();
        int textWidth = mc.font.width(text);

        int tooltipWidth = textWidth + PADDING * 2;
        int tooltipHeight = LINE_HEIGHT + PADDING * 2;

        // Adjust position to keep tooltip on screen
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        if (x + tooltipWidth > screenWidth)
        {
            x = screenWidth - tooltipWidth - 4;
        }
        if (y + tooltipHeight > screenHeight)
        {
            y = screenHeight - tooltipHeight - 4;
        }

        // Draw background
        Draw.rectangle(x - 1, y - 1, x + tooltipWidth + 1, y + tooltipHeight + 1, BORDER_COLOR);
        Draw.rectangle(x, y, x + tooltipWidth, y + tooltipHeight, BG_COLOR);

        // Draw text
        guiGraphics.drawString(mc.font, text, x + PADDING, y + PADDING, TEXT_COLOR, false);
    }

    /**
     * Renders a multi-line tooltip with optional title styling.
     */
    public static void renderTooltipMultiline(GuiGraphics guiGraphics, List<String> lines, int x, int y)
    {
        if (lines.isEmpty()) return;

        Minecraft mc = Minecraft.getInstance();

        // Calculate dimensions
        int maxWidth = 0;
        for (String line : lines)
        {
            int lineWidth = mc.font.width(line);
            if (lineWidth > maxWidth)
            {
                maxWidth = lineWidth;
            }
        }

        int tooltipWidth = maxWidth + PADDING * 2;
        int tooltipHeight = (LINE_HEIGHT * lines.size()) + PADDING * 2;

        // Adjust position to keep tooltip on screen
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        if (x + tooltipWidth > screenWidth)
        {
            x = screenWidth - tooltipWidth - 4;
        }
        if (y + tooltipHeight > screenHeight)
        {
            y = screenHeight - tooltipHeight - 4;
        }

        // Draw background
        Draw.rectangle(x - 1, y - 1, x + tooltipWidth + 1, y + tooltipHeight + 1, BORDER_COLOR);
        Draw.rectangle(x, y, x + tooltipWidth, y + tooltipHeight, BG_COLOR);

        // Draw lines
        int textY = y + PADDING;
        for (int i = 0; i < lines.size(); i++)
        {
            String line = lines.get(i);
            int color = (i == 0) ? TEXT_COLOR : TEXT_COLOR_SECONDARY;
            guiGraphics.drawString(mc.font, line, x + PADDING, textY, color, false);
            textY += LINE_HEIGHT;
        }
    }

    /**
     * Checks if a tooltip should be shown based on hover time.
     * Returns true if the hover delay has passed.
     */
    public static boolean shouldShowTooltip(long hoverStartTime, int delayMs)
    {
        if (hoverStartTime <= 0) return false;
        return System.currentTimeMillis() - hoverStartTime >= delayMs;
    }
}
