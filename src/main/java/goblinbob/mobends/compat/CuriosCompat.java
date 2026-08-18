package goblinbob.mobends.compat;

import dev.architectury.platform.Platform;

public class CuriosCompat
{
    private static final String MOD_ID = "curios";

    private static boolean initialized = false;
    private static boolean isLoaded = false;

    public static void init()
    {
        if (initialized) return;
        initialized = true;

        isLoaded = Platform.isModLoaded(MOD_ID);
    }

    public static boolean isModLoaded()
    {
        if (!initialized) init();
        return isLoaded;
    }
}
