package goblinbob.mobends.standard.client.model.armor;

import net.minecraft.client.model.HumanoidModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class ArmorModelFactory
{
    private static final Logger LOG = LoggerFactory.getLogger(ArmorModelFactory.class);

    protected static Map<HumanoidModel<?>, ArmorWrapper> outerLayerCache = new HashMap<>();

    protected static Map<HumanoidModel<?>, ArmorWrapper> innerLayerCache = new HashMap<>();

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

                LOG.info("Creating armor wrapper for {} with inflation {}", model, inflation);
            }

            return wrapper;
        }

        if (wrapper != null)
        {
            wrapper.deapply();
        }

        return wrapper;
    }

    public static ArmorWrapper getArmorWrapper(HumanoidModel<?> model, boolean shouldBeMutated)
    {
        return getArmorWrapper(model, shouldBeMutated, 1.0F);
    }

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
