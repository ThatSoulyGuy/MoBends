package goblinbob.mobends.compat;

import dev.architectury.platform.Platform;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;

import java.lang.reflect.Method;

public class ArtifactsCompat
{
    private static final String MOD_ID = "artifacts";

    private static boolean initialized = false;
    private static boolean isLoaded = false;

    private static Class<?> umbrellaClass;
    private static Method isHoldingUmbrellaUprightMethod;

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
                umbrellaClass = Class.forName("artifacts.item.UmbrellaItem");
            }
            catch (Exception e)
            {
                isLoaded = false;
                return;
            }

            try
            {
                isHoldingUmbrellaUprightMethod = umbrellaClass.getMethod(
                        "isHoldingUmbrellaUpright", LivingEntity.class, InteractionHand.class);
            }
            catch (Exception e)
            {
                isHoldingUmbrellaUprightMethod = null;
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

    public static boolean isHoldingUmbrellaUpright(LivingEntity entity, InteractionHand hand)
    {
        if (entity == null || !isModLoaded())
        {
            return false;
        }

        if (isHoldingUmbrellaUprightMethod != null)
        {
            try
            {
                Boolean upright = (Boolean) isHoldingUmbrellaUprightMethod.invoke(null, entity, hand);
                return upright != null && upright;
            }
            catch (Exception e)
            {
                isHoldingUmbrellaUprightMethod = null;
            }
        }

        return umbrellaClass.isInstance(entity.getItemInHand(hand).getItem())
                && (!entity.isUsingItem() || entity.getUsedItemHand() != hand);
    }

    public static boolean isHoldingUmbrellaUpright(LivingEntity entity)
    {
        return isHoldingUmbrellaUpright(entity, InteractionHand.MAIN_HAND)
                || isHoldingUmbrellaUpright(entity, InteractionHand.OFF_HAND);
    }
}
