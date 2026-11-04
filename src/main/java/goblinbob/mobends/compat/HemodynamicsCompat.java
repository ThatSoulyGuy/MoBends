package goblinbob.mobends.compat;

import dev.architectury.platform.Platform;
import net.minecraft.world.entity.LivingEntity;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class HemodynamicsCompat
{
    private static final String MOD_ID = "hemodynamics";

    private static boolean initialized = false;
    private static Method currentMethod;
    private static Field frameField;
    private static Field fittedField;

    public static void init()
    {
        if (initialized)
        {
            return;
        }
        initialized = true;

        if (!Platform.isModLoaded(MOD_ID))
        {
            return;
        }

        try
        {
            final Class<?> drawClass = Class.forName("breenihilation.hemodynamics.client.body.BloodBodyDraw");
            frameField = drawClass.getDeclaredField("frame");
            frameField.setAccessible(true);
            fittedField = drawClass.getDeclaredField("fitted");
            fittedField.setAccessible(true);
            currentMethod = drawClass.getMethod("current", LivingEntity.class);
        }
        catch (Throwable e)
        {
            currentMethod = null;
        }
    }

    public static void renderOnSkin(LivingEntity entity, Runnable render)
    {
        if (!initialized)
        {
            init();
        }

        Object draw = null;
        Object frame = null;
        if (currentMethod != null && entity != null)
        {
            try
            {
                draw = currentMethod.invoke(null, entity);
                if (draw != null)
                {
                    frame = frameField.get(draw);
                    frameField.set(draw, null);
                    fittedField.setBoolean(draw, false);
                }
            }
            catch (Throwable e)
            {
                currentMethod = null;
                draw = null;
            }
        }

        try
        {
            render.run();
        }
        finally
        {
            if (draw != null)
            {
                try
                {
                    frameField.set(draw, frame);
                    fittedField.setBoolean(draw, false);
                }
                catch (Throwable ignored)
                {
                }
            }
        }
    }
}
