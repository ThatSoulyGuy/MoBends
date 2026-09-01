package goblinbob.mobends.compat;

import dev.architectury.platform.Platform;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Method;

public class ParagliderCompat
{
    private static final String MOD_ID = "paraglider";

    private static boolean initialized = false;
    private static boolean isLoaded = false;

    private static Class<?> paragliderClass;
    private static Method isParaglidingMethod;

    public static void init()
    {
        if (initialized)
        {
            return;
        }
        initialized = true;

        isLoaded = Platform.isModLoaded(MOD_ID);

        if (isLoaded)
        {
            try
            {
                paragliderClass = Class.forName("tictim.paraglider.api.item.Paraglider");
                isParaglidingMethod = paragliderClass.getMethod("isParagliding", ItemStack.class);
            }
            catch (Exception e)
            {
                paragliderClass = null;
                isParaglidingMethod = null;
                isLoaded = false;
            }
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

    public static boolean isParagliding(LivingEntity entity)
    {
        if (entity == null || !isModLoaded())
        {
            return false;
        }

        final ItemStack heldItem = entity.getMainHandItem();
        if (heldItem.isEmpty() || !paragliderClass.isInstance(heldItem.getItem()))
        {
            return false;
        }

        try
        {
            Boolean paragliding = (Boolean) isParaglidingMethod.invoke(heldItem.getItem(), heldItem);
            return paragliding != null && paragliding;
        }
        catch (Exception e)
        {
            isLoaded = false;
            return false;
        }
    }
}
