package goblinbob.mobends.neoforge.compat;

import goblinbob.mobends.neoforge.PlayerAnimationLibCompat;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Central manager for all mod compatibility layers.
 * Initializes and coordinates compatibility with other mods.
 */
@OnlyIn(Dist.CLIENT)
public class ModCompatManager
{
    private static final Logger LOGGER = LoggerFactory.getLogger("MoBends-Compat");

    private static boolean initialized = false;

    /**
     * Initialize all mod compatibility layers.
     * Should be called once during client setup.
     */
    public static void init()
    {
        if (initialized)
        {
            return;
        }
        initialized = true;

        LOGGER.info("Initializing Mo'Bends mod compatibility layers...");

        // Initialize compatibility for animation mods
        PlayerAnimationLibCompat.init();

        // Initialize compatibility for equipment/accessory mods
        CuriosCompat.init();

        // Initialize compatibility for visual effect mods
        BetterBloodOverlayCompat.init();

        // Initialize compatibility for physics mods
        PhysicsModCompat.init();

        // Log summary
        logCompatSummary();
    }

    /**
     * Log a summary of all compatibility layer statuses.
     */
    private static void logCompatSummary()
    {
        LOGGER.info("Mo'Bends compatibility summary:");
        LOGGER.info("  - {}", PlayerAnimationLibCompat.isModLoaded()
                ? "PlayerAnimationLib: Loaded"
                : "PlayerAnimationLib: Not loaded");
        LOGGER.info("  - {}", CuriosCompat.getCompatInfo());
        LOGGER.info("  - {}", BetterBloodOverlayCompat.getCompatInfo());
        LOGGER.info("  - {}", PhysicsModCompat.getCompatInfo());
    }

    /**
     * Check if any animation mod that should take priority is active.
     *
     * @return true if Mo'Bends should defer to another animation mod
     */
    public static boolean shouldDeferAnimation(net.minecraft.world.entity.LivingEntity entity)
    {
        // PlayerAnimationLib takes priority when it has an active animation
        if (PlayerAnimationLibCompat.hasActiveAnimation(entity))
            return true;

        // Physics Mod takes priority when ragdoll/physics are active
        if (PhysicsModCompat.hasActivePhysics(entity))
            return true;

        return false;
    }
}
