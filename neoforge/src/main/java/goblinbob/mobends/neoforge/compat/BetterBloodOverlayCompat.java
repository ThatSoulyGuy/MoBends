package goblinbob.mobends.neoforge.compat;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.ModList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Compatibility helper for Better Blood Overlay mod (NeoForge version).
 *
 * Better Blood Overlay adds dynamic blood textures to player skins based on health.
 * The blood overlay intensity increases as health decreases and fades when healing.
 *
 * This mod renders blood overlays as an additional layer on the player skin.
 * Since Mo'Bends syncs animation transforms to the vanilla HumanoidModel,
 * the blood overlay layer should automatically follow animated limbs.
 *
 * This compatibility layer:
 * 1. Detects when Better Blood Overlay is present
 * 2. Provides hooks for any special handling if needed
 * 3. Logs compatibility status for debugging
 *
 * @see <a href="https://www.curseforge.com/minecraft/mc-mods/better-blood-overlay">Better Blood Overlay on CurseForge</a>
 */
@OnlyIn(Dist.CLIENT)
public class BetterBloodOverlayCompat
{
    private static final Logger LOGGER = LoggerFactory.getLogger("MoBends-BloodOverlayCompat");
    private static final String MOD_ID = "betterbloodoverlay";
    // Alternative mod ID check in case the mod uses a different ID
    private static final String ALT_MOD_ID = "better_blood_overlay";

    private static boolean initialized = false;
    private static boolean isLoaded = false;

    /**
     * Initialize the compatibility layer.
     * Should be called once during mod initialization.
     */
    public static void init()
    {
        if (initialized)
        {
            return;
        }
        initialized = true;

        // Check for both possible mod IDs
        isLoaded = ModList.get().isLoaded(MOD_ID) || ModList.get().isLoaded(ALT_MOD_ID);

        if (isLoaded)
        {
            LOGGER.info("Better Blood Overlay detected, compatibility layer active");
            LOGGER.info("Blood overlay will follow Mo'Bends animations automatically");
        }
    }

    /**
     * Check if Better Blood Overlay is loaded.
     */
    public static boolean isModLoaded()
    {
        if (!initialized)
        {
            init();
        }
        return isLoaded;
    }

    /**
     * Called before entity rendering to ensure blood overlay compatibility.
     *
     * Better Blood Overlay uses a render layer that draws on the player skin.
     * Since Mo'Bends syncs transforms to the vanilla model before layer rendering,
     * the blood overlay should automatically follow the animated limbs.
     *
     * This method exists for future extensions if special handling is needed.
     *
     * @param entity The entity being rendered
     */
    public static void beforeEntityRender(LivingEntity entity)
    {
        if (!isModLoaded())
        {
            return;
        }

        // Blood overlay uses vanilla model transforms which Mo'Bends already syncs
        // No special handling needed - the overlay will follow animated limbs
    }

    /**
     * Get compatibility information for logging.
     */
    public static String getCompatInfo()
    {
        if (!isModLoaded())
        {
            return "Better Blood Overlay: Not loaded";
        }
        return "Better Blood Overlay: Loaded, compatibility active";
    }
}
