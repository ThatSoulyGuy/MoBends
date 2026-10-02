package goblinbob.mobends.compat;

import dev.architectury.platform.Platform;
import net.minecraft.client.model.Model;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.lang.reflect.Field;

public final class CrysisCompat
{
    private static boolean initialized = false;
    private static Class<?> nanosuitModelClass;
    private static Field colorField;
    private static Field overlayTextureField;

    private CrysisCompat()
    {
    }

    private static void init()
    {
        initialized = true;

        if (!Platform.isModLoaded("crysismod"))
        {
            return;
        }

        try
        {
            nanosuitModelClass = Class.forName("com.blaster.crysismod.client.model.NanosuitModel");
            colorField = nanosuitModelClass.getField("color");
            overlayTextureField = nanosuitModelClass.getField("overlayTexture");
        }
        catch (Throwable e)
        {
            nanosuitModelClass = null;
        }
    }

    @Nullable
    public static ResourceLocation getGlowTexture(Model model)
    {
        if (!initialized)
        {
            init();
        }

        if (nanosuitModelClass == null || !nanosuitModelClass.isInstance(model))
        {
            return null;
        }

        try
        {
            final java.awt.Color color = (java.awt.Color) colorField.get(model);
            if (color == null || color.getAlpha() == 0)
            {
                return null;
            }
            return (ResourceLocation) overlayTextureField.get(model);
        }
        catch (Throwable e)
        {
            return null;
        }
    }

    public static int getGlowColor(Model model)
    {
        try
        {
            return ((java.awt.Color) colorField.get(model)).getRGB();
        }
        catch (Throwable e)
        {
            return 0xFFFFFFFF;
        }
    }
}
