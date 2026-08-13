package goblinbob.mobends.compat;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.LivingEntity;
import dev.architectury.platform.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

public class PlayerAnimationLibCompat
{
    private static final Logger LOGGER = LoggerFactory.getLogger("MoBends-PlayerAnimCompat");
    private static final String MOD_ID = "playeranimator";

    private static boolean initialized = false;
    private static boolean isLoaded = false;

    private static Class<?> playerAnimationAccessClass;
    private static Method getPlayerAnimLayerMethod;
    private static Method isActiveMethod;

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
            LOGGER.info("PlayerAnimationLib detected, initializing compatibility layer");
            try
            {
                initReflection();
                LOGGER.info("PlayerAnimationLib compatibility initialized successfully");
                debugReflection();
            }
            catch (Exception e)
            {
                LOGGER.warn("Failed to initialize PlayerAnimationLib compatibility: {}", e.getMessage(), e);
                isLoaded = false;
            }
        }
    }

    private static void initReflection() throws Exception
    {
        playerAnimationAccessClass = Class.forName("dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess");

        getPlayerAnimLayerMethod = playerAnimationAccessClass.getMethod("getPlayerAnimLayer", AbstractClientPlayer.class);

        Class<?> animationStackClass = Class.forName("dev.kosmx.playerAnim.api.layered.AnimationStack");
        isActiveMethod = animationStackClass.getMethod("isActive");
    }

    public static boolean isModLoaded()
    {
        if (!initialized)
        {
            init();
        }
        return isLoaded;
    }

    public static boolean hasActiveAnimation(LivingEntity entity)
    {
        if (!isModLoaded())
        {
            return false;
        }

        if (!(entity instanceof AbstractClientPlayer player))
        {
            return false;
        }

        try
        {
            Object animationStack = getPlayerAnimLayerMethod.invoke(null, player);

            if (animationStack == null)
            {
                return false;
            }

            Boolean isActive = (Boolean) isActiveMethod.invoke(animationStack);
            boolean result = isActive != null && isActive;

            if (result)
            {
                LOGGER.debug("PlayerAnimationLib animation active for player: {}", player.getName().getString());
            }

            return result;
        }
        catch (Exception e)
        {
            LOGGER.warn("Error checking PlayerAnimationLib animation state: {}", e.getMessage());
            return false;
        }
    }

    public static void debugReflection()
    {
        LOGGER.info("PlayerAnimationLib compat debug:");
        LOGGER.info("  - Mod loaded: {}", isLoaded);
        LOGGER.info("  - PlayerAnimationAccess class: {}", playerAnimationAccessClass);
        LOGGER.info("  - getPlayerAnimLayer method: {}", getPlayerAnimLayerMethod);
        LOGGER.info("  - isActive method: {}", isActiveMethod);
    }
}
