package goblinbob.mobends.compat;

import dev.architectury.platform.Platform;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.lang.reflect.Method;

public class QuarkColorRunesCompat
{
    private static final String MOD_ID = "quark";

    private static boolean initialized = false;
    private static boolean isLoaded = false;

    private static Method setTargetStackMethod;
    private static Method setTargetColorMethod;
    private static Method changeColorMethod;

    public static void init()
    {
        if (initialized)
        {
            return;
        }
        initialized = true;

        isLoaded = Platform.isModLoaded(MOD_ID);

        if (!isLoaded)
        {
            return;
        }

        try
        {
            Class<?> moduleClass = Class.forName("org.violetmoon.quark.content.tools.module.ColorRunesModule");
            Class<?> runeColorClass = Class.forName("org.violetmoon.quark.content.tools.base.RuneColor");

            setTargetStackMethod = moduleClass.getMethod("setTargetStack", ItemStack.class);
            setTargetColorMethod = moduleClass.getMethod("setTargetColor", runeColorClass);
            changeColorMethod = moduleClass.getMethod("changeColor");
        }
        catch (Throwable t)
        {
            setTargetStackMethod = null;
            setTargetColorMethod = null;
            changeColorMethod = null;
            isLoaded = false;
        }
    }

    public static boolean isModLoaded()
    {
        if (!initialized)
        {
            init();
        }
        return isLoaded;
    }

    @Nullable
    public static Object beginItem(ItemStack itemStack)
    {
        if (!isModLoaded())
        {
            return null;
        }

        try
        {
            Object previousColor = changeColorMethod.invoke(null);
            setTargetStackMethod.invoke(null, itemStack);
            return previousColor;
        }
        catch (Throwable t)
        {
            return null;
        }
    }

    public static void endItem(@Nullable Object previousColor)
    {
        if (!isModLoaded())
        {
            return;
        }

        try
        {
            setTargetColorMethod.invoke(null, previousColor);
        }
        catch (Throwable ignored)
        {
        }
    }
}
