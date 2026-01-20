package goblinbob.mobends.standard.client.model.armor;

import goblinbob.mobends.standard.main.MoBends;
import net.minecraft.client.model.HumanoidModel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.HashMap;
import java.util.Map;

/**
 * Factory for creating and caching armor wrappers.
 * Maintains separate caches for different inflation levels (inner/outer layer).
 */
@OnlyIn(Dist.CLIENT)
public class ArmorModelFactory
{
    /**
     * Cache for outer layer armor wrappers (inflation = 1.0F).
     */
    protected static Map<HumanoidModel<?>, ArmorWrapper> outerLayerCache = new HashMap<>();

    /**
     * Cache for inner layer armor wrappers (inflation = 0.5F).
     */
    protected static Map<HumanoidModel<?>, ArmorWrapper> innerLayerCache = new HashMap<>();

    /**
     * Get or create an armor wrapper for the given model.
     *
     * @param model The HumanoidModel to wrap
     * @param shouldBeMutated Whether the armor should use Mo' Bends animation
     * @param inflation The inflation amount (0.5F for leggings inner layer, 1.0F for outer layer)
     * @return The armor wrapper, or null if shouldBeMutated is false and no wrapper exists
     */
    public static ArmorWrapper getArmorWrapper(HumanoidModel<?> model, boolean shouldBeMutated, float inflation)
    {
        Map<HumanoidModel<?>, ArmorWrapper> cache = (inflation < 0.75F) ? innerLayerCache : outerLayerCache;

        ArmorWrapper wrapper = cache.get(model);

        if (shouldBeMutated)
        {
            if (wrapper == null)
            {
                wrapper = ArmorWrapper.createFor(model, inflation);
                cache.put(model, wrapper);

                MoBends.LOG.info("Creating armor wrapper for " + model + " with inflation " + inflation);
            }

            return wrapper;
        }

        if (wrapper != null)
        {
            wrapper.deapply();
        }

        return wrapper;
    }

    /**
     * Get or create an armor wrapper with default (outer layer) inflation.
     */
    public static ArmorWrapper getArmorWrapper(HumanoidModel<?> model, boolean shouldBeMutated)
    {
        return getArmorWrapper(model, shouldBeMutated, 1.0F);
    }

    /**
     * Refresh all cached wrappers.
     */
    public static void refresh()
    {
        for (ArmorWrapper wrapper : outerLayerCache.values())
        {
            wrapper.demutate();
        }
        outerLayerCache.clear();

        for (ArmorWrapper wrapper : innerLayerCache.values())
        {
            wrapper.demutate();
        }
        innerLayerCache.clear();
    }
}
