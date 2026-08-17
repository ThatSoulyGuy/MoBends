package goblinbob.mobends.compat;

import dev.architectury.platform.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CuriosCompat
{
    private static final Logger LOGGER = LoggerFactory.getLogger("MoBends-CuriosCompat");
    private static final String MOD_ID = "curios";

    private static boolean initialized = false;
    private static boolean isLoaded = false;

    public static void init()
    {
        if (initialized) return;
        initialized = true;

        isLoaded = Platform.isModLoaded(MOD_ID);

        if (isLoaded)
        {
            LOGGER.info("Curios API detected, accessory models will follow the animated skeleton");
        }
    }

    public static boolean isModLoaded()
    {
        if (!initialized) init();
        return isLoaded;
    }

    public static String getCompatInfo()
    {
        if (!isModLoaded()) return "Curios: Not loaded";
        return "Curios: Loaded, compatibility active";
    }
}
