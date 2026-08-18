package goblinbob.mobends.compat;

import net.minecraft.world.entity.LivingEntity;
import dev.architectury.platform.Platform;

public class BetterBloodOverlayCompat
{
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
}
