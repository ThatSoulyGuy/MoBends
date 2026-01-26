package goblinbob.mobends.standard.main;

import goblinbob.mobends.core.util.ResourceLocationFactory;
import net.minecraft.resources.ResourceLocation;

/**
 * Static constants for the mod.
 * Platform-specific initialization code sets the MOD_ID.
 */
public class ModStatics
{
    public static String MOD_ID = "mobends";
    /**
     * Alias for MOD_ID for compatibility.
     */
    public static String MODID = MOD_ID;
    public static String MOD_NAME = "Mo' Bends";
    public static String VERSION = "5.0.0";

    public static ResourceLocation getResource(String path)
    {
        return ResourceLocationFactory.create(MOD_ID, path);
    }
}
