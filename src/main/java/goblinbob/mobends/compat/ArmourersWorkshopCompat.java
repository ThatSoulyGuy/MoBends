package goblinbob.mobends.compat;

import dev.architectury.platform.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArmourersWorkshopCompat
{
    private static final Logger LOGGER = LoggerFactory.getLogger("MoBends-ArmourersWorkshopCompat");
    private static final String MOD_ID = "armourers_workshop";

    private static boolean initialized = false;
    private static boolean isLoaded = false;

    public static void init()
    {
        if (initialized) return;
        initialized = true;

        isLoaded = Platform.isModLoaded(MOD_ID);

        if (isLoaded)
        {
            LOGGER.info("Armourer's Workshop detected, Mo'Bends armature will be used for skin rendering");
        }
    }

    public static boolean isModLoaded()
    {
        if (!initialized) init();
        return isLoaded;
    }

    public static String getCompatInfo()
    {
        if (!isModLoaded()) return "Armourer's Workshop: Not loaded";
        return "Armourer's Workshop: Loaded, compatibility active";
    }
}
