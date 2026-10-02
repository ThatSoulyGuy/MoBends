package goblinbob.mobends.compat;

import dev.architectury.platform.Platform;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.lang.reflect.Method;

public final class AntarchyCompat
{
    private static boolean initialized = false;
    private static Method hasFullSetMethod;
    private static Method getStateMethod;
    private static Method activeMethod;

    private AntarchyCompat()
    {
    }

    private static void init()
    {
        initialized = true;

        if (!Platform.isModLoaded("antarchy"))
        {
            return;
        }

        try
        {
            hasFullSetMethod = Class.forName("com.craisinlord.antarchy.content.item.TigerEyeArmorUtil")
                    .getMethod("hasFullSet", Player.class);
            getStateMethod = Class.forName("com.craisinlord.antarchy.content.client.TigerEyeCamouflageClientState")
                    .getMethod("get", int.class);
            activeMethod = Class.forName("com.craisinlord.antarchy.content.client.TigerEyeCamouflageClientState$CamouflageState")
                    .getMethod("active");
        }
        catch (Throwable e)
        {
            hasFullSetMethod = null;
        }
    }

    public static boolean isArmorHidden(LivingEntity entity)
    {
        if (!initialized)
        {
            init();
        }

        if (hasFullSetMethod == null || !(entity instanceof Player player))
        {
            return false;
        }

        try
        {
            if (!(Boolean) hasFullSetMethod.invoke(null, player))
            {
                return false;
            }

            final Object state = getStateMethod.invoke(null, player.getId());
            return state != null && (Boolean) activeMethod.invoke(state);
        }
        catch (Throwable e)
        {
            return false;
        }
    }
}
