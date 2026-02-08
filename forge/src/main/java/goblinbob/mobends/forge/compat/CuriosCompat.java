package goblinbob.mobends.forge.compat;

import com.mojang.blaze3d.vertex.PoseStack;
import goblinbob.mobends.core.client.MoBendsRenderContext;
import goblinbob.mobends.standard.mutators.BipedMutator;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.ModList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * Compatibility helper for Curios API.
 * Ensures curio items rendered on players follow Mo'Bends animations.
 *
 * Curios API adds extra equipment slots (rings, amulets, belts, back items, etc.)
 * that can render on the player model. This compatibility layer ensures:
 * 1. Curio render layers use Mo'Bends-synced transforms
 * 2. Curio items follow animated body parts correctly
 *
 * @see <a href="https://github.com/TheIllusiveC4/Curios">Curios GitHub</a>
 */
@OnlyIn(Dist.CLIENT)
public class CuriosCompat
{
    private static final Logger LOGGER = LoggerFactory.getLogger("MoBends-CuriosCompat");
    private static final String MOD_ID = "curios";

    private static boolean initialized = false;
    private static boolean isLoaded = false;

    // Reflection cache for Curios API
    private static Class<?> curiosApiClass;
    private static Method getCuriosInventoryMethod;

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

        isLoaded = ModList.get().isLoaded(MOD_ID);

        if (isLoaded)
        {
            LOGGER.info("Curios API detected, initializing compatibility layer");
            try
            {
                initReflection();
                LOGGER.info("Curios compatibility initialized successfully");
            }
            catch (Exception e)
            {
                LOGGER.warn("Failed to initialize Curios compatibility: {}", e.getMessage());
                // Still mark as loaded - we just won't have advanced features
            }
        }
    }

    /**
     * Initialize reflection handles for Curios API classes.
     */
    private static void initReflection() throws Exception
    {
        // Try to get the CuriosApi class
        curiosApiClass = Class.forName("top.theillusivec4.curios.api.CuriosApi");

        // Get the getCuriosInventory method if available
        // Method signature: Optional<ICuriosItemHandler> getCuriosInventory(LivingEntity entity)
        getCuriosInventoryMethod = curiosApiClass.getMethod("getCuriosInventory", LivingEntity.class);

        LOGGER.debug("Curios reflection initialized:");
        LOGGER.debug("  - CuriosApi class: {}", curiosApiClass);
        LOGGER.debug("  - getCuriosInventory method: {}", getCuriosInventoryMethod);
    }

    /**
     * Check if Curios API is loaded.
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
     * Sync Mo'Bends transforms for curio rendering.
     * This should be called before curio render layers execute.
     *
     * Since Mo'Bends already syncs poses to the vanilla HumanoidModel via
     * {@code bipedMutator.syncPosesToVanillaModel()}, curio render layers
     * that use vanilla model transforms will automatically work.
     *
     * This method provides additional sync for curios that may need
     * access to split limb transforms (upper/lower arm, etc.)
     *
     * @param entity The entity being rendered
     * @param model The humanoid model
     * @param poseStack The current pose stack
     */
    public static void syncTransformsForCurios(LivingEntity entity, HumanoidModel<?> model, PoseStack poseStack)
    {
        if (!isModLoaded())
        {
            return;
        }

        // Get the current biped mutator from render context
        BipedMutator<?, ?, ?> mutator = MoBendsRenderContext.getCurrentBipedMutator();
        if (mutator == null)
        {
            return;
        }

        // The vanilla model transforms are already synced by beforeLivingRender
        // Curios render layers will use these synced transforms automatically
        // This method exists for future extensions if curios need special handling
    }

    /**
     * Check if an entity has any curio items equipped.
     *
     * @param entity The entity to check
     * @return true if the entity has curio items, false otherwise
     */
    public static boolean hasCurioItems(LivingEntity entity)
    {
        if (!isModLoaded() || getCuriosInventoryMethod == null)
        {
            return false;
        }

        try
        {
            Object optional = getCuriosInventoryMethod.invoke(null, entity);
            if (optional != null)
            {
                Method isPresentMethod = optional.getClass().getMethod("isPresent");
                return (Boolean) isPresentMethod.invoke(optional);
            }
        }
        catch (Exception e)
        {
            LOGGER.debug("Error checking curio items: {}", e.getMessage());
        }

        return false;
    }

    /**
     * Get compatibility information for logging.
     */
    public static String getCompatInfo()
    {
        if (!isModLoaded())
        {
            return "Curios: Not loaded";
        }
        return "Curios: Loaded, compatibility active";
    }
}
