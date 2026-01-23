package goblinbob.mobends.core.compat;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.fml.ModList;

/**
 * Compatibility layer for PlayerAnimationLib (used by BetterCombat and other mods).
 * When PlayerAnimationLib has an active animation playing, MoBends should defer
 * to let it control the player model.
 */
public class PlayerAnimationLibCompat {

    private static final String MOD_ID = "playeranimator";
    private static Boolean isLoaded = null;
    private static PlayerAnimChecker checker = null;

    /**
     * Check if PlayerAnimationLib is loaded.
     */
    public static boolean isPlayerAnimationLibLoaded() {
        if (isLoaded == null) {
            isLoaded = ModList.get().isLoaded(MOD_ID);
            if (isLoaded) {
                try {
                    checker = new PlayerAnimCheckerImpl();
                    goblinbob.mobends.standard.main.MoBends.LOG.info(
                        "PlayerAnimationLib detected - MoBends will defer to its animations when active");
                } catch (Throwable t) {
                    goblinbob.mobends.standard.main.MoBends.LOG.warn(
                        "PlayerAnimationLib detected but failed to initialize compatibility: {}", t.getMessage());
                    checker = null;
                    isLoaded = false;
                }
            }
        }
        return isLoaded;
    }

    /**
     * Check if PlayerAnimationLib has an active animation for the given entity.
     * Returns false if the entity is not a player or if PlayerAnimationLib is not loaded.
     */
    public static boolean hasActiveAnimation(LivingEntity entity) {
        if (!isPlayerAnimationLibLoaded() || checker == null) {
            return false;
        }
        if (!(entity instanceof AbstractClientPlayer player)) {
            return false;
        }
        try {
            return checker.isAnimationActive(player);
        } catch (Throwable t) {
            // If something goes wrong, don't block MoBends
            return false;
        }
    }

    /**
     * Interface for checking animation state - allows us to isolate the
     * PlayerAnimationLib-dependent code.
     */
    private interface PlayerAnimChecker {
        boolean isAnimationActive(AbstractClientPlayer player);
    }

    /**
     * Implementation that actually uses PlayerAnimationLib API.
     * This class is only loaded if PlayerAnimationLib is present.
     */
    private static class PlayerAnimCheckerImpl implements PlayerAnimChecker {
        @Override
        public boolean isAnimationActive(AbstractClientPlayer player) {
            try {
                // Get the animation stack for the player
                var animStack = dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess
                    .getPlayerAnimLayer(player);

                // Check if any animation is currently active
                return animStack != null && animStack.isActive();
            } catch (IllegalArgumentException e) {
                // Player might not have animation data yet
                return false;
            }
        }
    }
}
