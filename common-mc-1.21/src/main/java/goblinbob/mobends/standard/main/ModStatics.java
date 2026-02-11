package goblinbob.mobends.standard.main;

import goblinbob.mobends.core.util.ResourceLocationFactory;
import net.minecraft.resources.ResourceLocation;

import java.io.InputStream;
import java.util.Properties;

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
    public static final String VERSION = loadVersion();

    private static String loadVersion()
    {
        try (InputStream is = ModStatics.class.getResourceAsStream("/mobends_version.properties"))
        {
            if (is != null)
            {
                Properties props = new Properties();
                props.load(is);
                return props.getProperty("version", "unknown");
            }
        }
        catch (Exception ignored) {}
        return "unknown";
    }

    public static ResourceLocation getResource(String path)
    {
        return ResourceLocationFactory.create(MOD_ID, path);
    }
}
