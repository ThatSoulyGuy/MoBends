package goblinbob.mobends.core.client.gui;

import net.minecraft.client.Minecraft;

public class GuiHelper
{

    public static int[] getDeScaledCoords(int x, int y)
    {
        Minecraft mc = Minecraft.getInstance();
        int scaledWidth = mc.getWindow().getGuiScaledWidth();
        int scaledHeight = mc.getWindow().getGuiScaledHeight();
        int screenWidth = mc.getWindow().getScreenWidth();
        int screenHeight = mc.getWindow().getScreenHeight();
        int k1 = x * screenWidth / scaledWidth;
        int l1 = (scaledHeight - y) * screenHeight / scaledHeight + 1;
        return new int[]{k1, l1};
    }

    public static int[] getDeScaledVector(int x, int y)
    {
        Minecraft mc = Minecraft.getInstance();
        int scaledWidth = mc.getWindow().getGuiScaledWidth();
        int scaledHeight = mc.getWindow().getGuiScaledHeight();
        int screenWidth = mc.getWindow().getScreenWidth();
        int screenHeight = mc.getWindow().getScreenHeight();
        int k1 = x * screenWidth / scaledWidth;
        int l1 = y * screenHeight / scaledHeight;
        return new int[]{k1, l1};
    }

}
