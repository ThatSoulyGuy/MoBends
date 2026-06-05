package goblinbob.mobends.core.util;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

import java.lang.reflect.Method;

/**
 * Helper for cross-version screen method calls.
 *
 * renderBackground:
 * - MC 1.21.1: renderBackground(GuiGraphics, int mouseX, int mouseY, float partialTicks)
 * - MC 1.20.1: renderBackground(GuiGraphics)
 *
 * mouseScrolled:
 * - MC 1.21.1: mouseScrolled(double, double, double scrollX, double scrollY) - 4 params
 * - MC 1.20.1: mouseScrolled(double, double, double scrollY) - 3 params
 */
public class ScreenHelper
{
    private static Boolean is1211 = null;
    private static Method renderBackgroundMethod = null;
    private static Method mouseScrolledMethod = null;

    /**
     * Calls renderBackground with version-appropriate parameters.
     */
    public static void renderBackground(Screen screen, GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks)
    {
        try
        {
            if (is1211 == null)
            {
                detectVersion();
            }

            if (is1211)
            {
                // 1.21.1: renderBackground(GuiGraphics, int, int, float)
                if (renderBackgroundMethod == null)
                {
                    renderBackgroundMethod = Screen.class.getDeclaredMethod("renderBackground",
                            GuiGraphics.class, int.class, int.class, float.class);
                    renderBackgroundMethod.setAccessible(true);
                }
                renderBackgroundMethod.invoke(screen, guiGraphics, mouseX, mouseY, partialTicks);
            }
            else
            {
                // 1.20.1: renderBackground(GuiGraphics)
                if (renderBackgroundMethod == null)
                {
                    renderBackgroundMethod = Screen.class.getDeclaredMethod("renderBackground", GuiGraphics.class);
                    renderBackgroundMethod.setAccessible(true);
                }
                renderBackgroundMethod.invoke(screen, guiGraphics);
            }
        }
        catch (Exception e)
        {
            // If reflection fails, just ignore - background won't render but it's not critical
            e.printStackTrace();
        }
    }

    /**
     * Calls super.mouseScrolled with version-appropriate parameters.
     * Note: This must be called from within the screen subclass to work properly.
     *
     * @return The result from mouseScrolled, or false if failed
     */
    public static boolean superMouseScrolled(Screen screen, double mouseX, double mouseY, double scrollX, double scrollY)
    {
        try
        {
            if (is1211 == null)
            {
                detectVersion();
            }

            if (is1211)
            {
                // 1.21.1: mouseScrolled(double, double, double, double)
                if (mouseScrolledMethod == null)
                {
                    mouseScrolledMethod = Screen.class.getDeclaredMethod("mouseScrolled",
                            double.class, double.class, double.class, double.class);
                    mouseScrolledMethod.setAccessible(true);
                }
                return (Boolean) mouseScrolledMethod.invoke(screen, mouseX, mouseY, scrollX, scrollY);
            }
            else
            {
                // 1.20.1: mouseScrolled(double, double, double)
                if (mouseScrolledMethod == null)
                {
                    mouseScrolledMethod = Screen.class.getDeclaredMethod("mouseScrolled",
                            double.class, double.class, double.class);
                    mouseScrolledMethod.setAccessible(true);
                }
                return (Boolean) mouseScrolledMethod.invoke(screen, mouseX, mouseY, scrollY);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    private static void detectVersion()
    {
        try
        {
            // Try to find the 1.21.1 version of renderBackground
            Screen.class.getDeclaredMethod("renderBackground",
                    GuiGraphics.class, int.class, int.class, float.class);
            is1211 = true;
        }
        catch (NoSuchMethodException e)
        {
            is1211 = false;
        }
    }
}
