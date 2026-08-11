package goblinbob.mobends.neoforge.compat;

import goblinbob.mobends.neoforge.PlayerAnimationLibCompat;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@OnlyIn(Dist.CLIENT)
public class ModCompatManager
{
    private static final Logger LOGGER = LoggerFactory.getLogger("MoBends-Compat");

    private static boolean initialized = false;

    public static void init()
    {
        if (initialized)
        {
            return;
        }
        initialized = true;

        LOGGER.info("Initializing Mo'Bends mod compatibility layers...");

        PlayerAnimationLibCompat.init();

        CuriosCompat.init();

        BetterBloodOverlayCompat.init();

        PhysicsModCompat.init();

        logCompatSummary();
    }

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

    public static boolean shouldDeferAnimation(net.minecraft.world.entity.LivingEntity entity)
    {
        if (PlayerAnimationLibCompat.hasActiveAnimation(entity))
            return true;

        if (PhysicsModCompat.hasActivePhysics(entity))
            return true;

        return false;
    }
}
