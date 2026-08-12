package goblinbob.mobends.core.util;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

import java.lang.reflect.Method;

public class ScreenHelper
{
    private static Boolean is1211 = null;
    private static Method renderBackgroundMethod = null;
    private static Method mouseScrolledMethod = null;

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
            e.printStackTrace();
        }
    }

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
