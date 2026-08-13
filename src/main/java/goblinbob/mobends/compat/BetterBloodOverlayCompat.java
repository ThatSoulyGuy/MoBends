package goblinbob.mobends.compat;

import net.minecraft.world.entity.LivingEntity;
import dev.architectury.platform.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BetterBloodOverlayCompat
{
    private static final Logger LOGGER = LoggerFactory.getLogger("MoBends-BloodOverlayCompat");
    private static final String MOD_ID = "betterbloodoverlay";
    private static final String ALT_MOD_ID = "better_blood_overlay";

    private static boolean initialized = false;
    private static boolean isLoaded = false;

    public static void init()
    {
        if (initialized)
        {
            return;
        }
        initialized = true;

        isLoaded = Platform.isModLoaded(MOD_ID) || Platform.isModLoaded(ALT_MOD_ID);

        if (isLoaded)
        {
            LOGGER.info("Better Blood Overlay detected, compatibility layer active");
            LOGGER.info("Blood overlay will follow Mo'Bends animations automatically");
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

    public static void beforeEntityRender(LivingEntity entity)
    {
        if (!isModLoaded())
        {
            return;
        }

    }

    public static String getCompatInfo()
    {
        if (!isModLoaded())
        {
            return "Better Blood Overlay: Not loaded";
        }
        return "Better Blood Overlay: Loaded, compatibility active";
    }
}
