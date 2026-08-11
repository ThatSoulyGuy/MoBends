package goblinbob.mobends.forge.compat;

import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.ModList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@OnlyIn(Dist.CLIENT)
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

        isLoaded = ModList.get().isLoaded(MOD_ID) || ModList.get().isLoaded(ALT_MOD_ID);

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
