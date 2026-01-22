package goblinbob.mobends.core.compat;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.ModList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * Compatibility helper for PlayerAnimationLib (player-animation-lib).
 * Uses reflection to avoid hard dependency on the mod.
 *
 * When PlayerAnimationLib has an animation playing on a player,
 * Mo'Bends should not override those animations.
 */
@OnlyIn(Dist.CLIENT)
public class PlayerAnimationLibCompat
{
    private static final Logger LOGGER = LoggerFactory.getLogger("MoBends-PlayerAnimCompat");
    private static final String MOD_ID = "playeranimator";

    private static boolean initialized = false;
    private static boolean isLoaded = false;

    // Reflection cache
    private static Class<?> playerAnimationAccessClass;
    private static Method getPlayerAnimLayerMethod;
    private static Method isActiveMethod;

    /**
     * Initialize the compatibility layer.
     * Should be called once during mod initialization.
     */
    public static void init()
    {
        if (initialized)
        {
            return;
        }
        initialized = true;

        isLoaded = ModList.get().isLoaded(MOD_ID);

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

    /**
     * Initialize reflection handles for PlayerAnimationLib classes.
     */
    private static void initReflection() throws Exception
    {
        // Get PlayerAnimationAccess class
        playerAnimationAccessClass = Class.forName("dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess");

        // Get the getPlayerAnimLayer method
        // Method signature: public static AnimationStack getPlayerAnimLayer(AbstractClientPlayer player)
        getPlayerAnimLayerMethod = playerAnimationAccessClass.getMethod("getPlayerAnimLayer", AbstractClientPlayer.class);

        // Get AnimationStack class and isActive method
        Class<?> animationStackClass = Class.forName("dev.kosmx.playerAnim.api.layered.AnimationStack");
        isActiveMethod = animationStackClass.getMethod("isActive");
    }

    /**
     * Check if PlayerAnimationLib is loaded.
     */
    public static boolean isModLoaded()
    {
        if (!initialized)
        {
            init();
        }
        return isLoaded;
    }

    /**
     * Check if PlayerAnimationLib has an active animation on the given entity.
     * Returns false if PlayerAnimationLib is not loaded or the entity is not a player.
     *
     * @param entity The entity to check
     * @return true if an animation is active, false otherwise
     */
    public static boolean hasActiveAnimation(LivingEntity entity)
    {
        if (!isModLoaded())
        {
            return false;
        }

        // PlayerAnimationLib only works with players
        if (!(entity instanceof AbstractClientPlayer player))
        {
            return false;
        }

        try
        {
            // Get the animation stack for the player
            Object animationStack = getPlayerAnimLayerMethod.invoke(null, player);

            if (animationStack == null)
            {
                return false;
            }

            // Check if any animation is active
            Boolean isActive = (Boolean) isActiveMethod.invoke(animationStack);
            boolean result = isActive != null && isActive;

            // Debug logging (only log when animation becomes active/inactive to reduce spam)
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

    /**
     * Force check if reflection is working correctly.
     * Call this during init to verify the compatibility layer.
     */
    public static void debugReflection()
    {
        LOGGER.info("PlayerAnimationLib compat debug:");
        LOGGER.info("  - Mod loaded: {}", isLoaded);
        LOGGER.info("  - PlayerAnimationAccess class: {}", playerAnimationAccessClass);
        LOGGER.info("  - getPlayerAnimLayer method: {}", getPlayerAnimLayerMethod);
        LOGGER.info("  - isActive method: {}", isActiveMethod);
    }
}
