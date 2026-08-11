package goblinbob.mobends.core.util;

import net.minecraft.client.gui.Font;

import java.util.ArrayList;
import java.util.List;

public class GuiUtil
{
    public static String[] wrapText(Font font, String text, int maxWidth)
    {
        if (maxWidth <= 0) return new String[] {};
        if (!text.contains(" "))
            return new String[] { text };

        List<String> lines = new ArrayList<>();
        String leftover = text + "";
        String line = "";

        boolean endOfString = false;
        do
        {
            String leftoverToNextSpace;

            if (leftover.contains(" "))
            {
                leftoverToNextSpace = leftover.substring(0, leftover.indexOf(" "));
            }
            else
            {
                leftoverToNextSpace = leftover;
                endOfString = true;
            }

            int currentWidth = font.width(line + leftoverToNextSpace);
            if (currentWidth > maxWidth)
            {
                lines.add(line.trim());
                line = leftoverToNextSpace + " ";
            }
            else
            {
                line += leftoverToNextSpace + " ";
            }

            if (!endOfString)
                leftover = leftover.substring(leftover.indexOf(" ") + 1);
            else
                lines.add(line.trim());
        } while (!endOfString);

        return lines.toArray(new String[] {});
    }
}
