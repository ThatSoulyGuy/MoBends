package goblinbob.mobends.compat;

import dev.architectury.platform.Platform;
import goblinbob.mobends.api.addon.AddonAnimationRegistry;
import goblinbob.mobends.standard.client.renderer.entity.mutated.BipedRenderer;
import goblinbob.mobends.standard.data.HumanoidMobData;
import goblinbob.mobends.standard.mutators.MillenaireMutator;
import goblinbob.mobends.standard.previewer.BipedPreviewer;
import net.minecraft.world.entity.LivingEntity;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public final class MillenaireCompat
{
    private static final String MOD_ID = "millenaire";

    private static final String VILLAGER_CLASS = "org.millenaire.entity.MillVillager";
    private static final String VILLAGER_TYPE_CLASS = "org.millenaire.culture.VillagerType";
    private static final String CULTURES_CLASS = "org.millenaire.culture.ModCultures";
    private static final String APPEARANCE_FACTORY_CLASS = "org.millenaire.entity.VillagerAppearanceFactory";

    private MillenaireCompat()
    {
    }

    public static void register(AddonAnimationRegistry registry, String[] animations, String[] alterableParts)
    {
        if (!Platform.isModLoaded(MOD_ID))
        {
            return;
        }

        final Class<LivingEntity> villagerClass = resolve(VILLAGER_CLASS);
        if (villagerClass == null)
        {
            return;
        }

        try
        {
            registry.registerNewEntity("millenaire:villager", "entity.mobends.millenaire_villager", villagerClass,
                    HumanoidMobData::new, MillenaireMutator::new,
                    new BipedRenderer<>(), new BipedPreviewer<>(), animations, alterableParts);
        }
        catch (Throwable ignored)
        {
        }
    }

    public static void initializePreviewAppearance(Object entity)
    {
        if (entity == null || !VILLAGER_CLASS.equals(entity.getClass().getName()))
        {
            return;
        }

        try
        {
            final Object villagerType = previewVillagerType();
            if (villagerType == null)
            {
                return;
            }

            final Class<?> typeClass = Class.forName(VILLAGER_TYPE_CLASS);
            Class.forName(APPEARANCE_FACTORY_CLASS)
                    .getMethod("randomizeAppearance", entity.getClass(), typeClass)
                    .invoke(null, entity, villagerType);
        }
        catch (Throwable ignored)
        {
        }
    }

    private static Object previewVillagerType() throws ReflectiveOperationException
    {
        final Map<?, ?> villagerTypes = (Map<?, ?>) Class.forName(CULTURES_CLASS)
                .getMethod("getAllVillagerTypes").invoke(null);
        if (villagerTypes == null || villagerTypes.isEmpty())
        {
            return null;
        }

        final Class<?> typeClass = Class.forName(VILLAGER_TYPE_CLASS);
        final Method isChild = typeClass.getMethod("isChild");
        final Method textures = typeClass.getMethod("textures");

        final List<Object> candidates = new ArrayList<>();
        for (Object type : villagerTypes.values())
        {
            if (type == null || (Boolean) isChild.invoke(type))
            {
                continue;
            }
            if (textures.invoke(type) instanceof List<?> list && !list.isEmpty())
            {
                candidates.add(type);
            }
        }

        return candidates.isEmpty()
                ? null
                : candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
    }

    @SuppressWarnings("unchecked")
    private static Class<LivingEntity> resolve(String className)
    {
        try
        {
            final Class<?> candidate = Class.forName(className);
            return LivingEntity.class.isAssignableFrom(candidate) ? (Class<LivingEntity>) candidate : null;
        }
        catch (Throwable t)
        {
            return null;
        }
    }
}
