package goblinbob.mobends.core.util;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

public final class ScreenHelper
{
    private ScreenHelper()
    {
    }

    public static void renderBackground(Screen screen, GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks)
    {
        //? if >=1.21 {
        /*screen.renderBackground(guiGraphics, mouseX, mouseY, partialTicks);
        *///?} else {
        screen.renderBackground(guiGraphics);
        //?}
    }
}
