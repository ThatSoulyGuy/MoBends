package goblinbob.mobends.compat;

import dev.architectury.platform.Platform;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class OffHandCombatCompat
{
    private static final String MOD_ID = "offhandcombat";

    private static boolean initialized = false;
    private static boolean isLoaded = false;

    public static final int NOT_SWINGING = -1;

    private static Method getDataMethod;
    private static Field swingingField;
    private static Field swingingArmField;
    private static Field swingTimeField;

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
            getDataMethod = Class.forName("cinnamon.ofc.Mod").getMethod("get", Player.class);

            final Class<?> dataClass = Class.forName("cinnamon.ofc.Mod$Data");
            swingingField = dataClass.getField("swinging");
            swingingArmField = dataClass.getField("swingingArm");
            swingTimeField = dataClass.getField("swingTime");

            isLoaded = true;
        }
        catch (Throwable e)
        {
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

    public static boolean isSwinging(LivingEntity entity, InteractionHand hand)
    {
        if (entity == null)
        {
            return false;
        }

        if (entity.swinging && entity.swingingArm == hand)
        {
            return true;
        }

        if (!isModLoaded() || !(entity instanceof Player player))
        {
            return false;
        }

        try
        {
            final Object data = getDataMethod.invoke(null, player);
            if (data == null)
            {
                return false;
            }

            return swingingField.getBoolean(data) && swingingArmField.get(data) == hand;
        }
        catch (Exception e)
        {
            isLoaded = false;
            return false;
        }
    }

    public static int getSwingTime(LivingEntity entity, InteractionHand hand)
    {
        if (entity == null)
        {
            return NOT_SWINGING;
        }

        if (entity.swinging && entity.swingingArm == hand)
        {
            return Math.max(entity.swingTime, 0);
        }

        if (!isModLoaded() || !(entity instanceof Player player))
        {
            return NOT_SWINGING;
        }

        try
        {
            final Object data = getDataMethod.invoke(null, player);
            if (data == null || !swingingField.getBoolean(data) || swingingArmField.get(data) != hand)
            {
                return NOT_SWINGING;
            }

            return Math.max(swingTimeField.getInt(data), 0);
        }
        catch (Exception e)
        {
            isLoaded = false;
            return NOT_SWINGING;
        }
    }
}
