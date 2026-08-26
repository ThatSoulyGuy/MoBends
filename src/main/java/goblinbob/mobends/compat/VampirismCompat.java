package goblinbob.mobends.compat;

import dev.architectury.platform.Platform;
import goblinbob.mobends.api.addon.AddonAnimationRegistry;
import goblinbob.mobends.standard.client.renderer.entity.mutated.BipedRenderer;
import goblinbob.mobends.standard.data.HumanoidMobData;
import goblinbob.mobends.standard.data.VillagerData;
import goblinbob.mobends.standard.mutators.HumanoidMobMutator;
import goblinbob.mobends.standard.mutators.VillagerMutator;
import goblinbob.mobends.standard.previewer.BipedPreviewer;
import net.minecraft.world.entity.LivingEntity;

public final class VampirismCompat
{
    private static final String MOD_ID = "vampirism";

    private static final String[][] HUMANOID_ENTITIES = {
            {"vampirism:vampire", "de.teamlapen.vampirism.entity.vampire.BasicVampireEntity"},
            {"vampirism:vampire", "de.teamlapen.vampirism.entity.vampire.BasicVampireEntity$IMob"},
            {"vampirism:advanced_vampire", "de.teamlapen.vampirism.entity.vampire.AdvancedVampireEntity"},
            {"vampirism:advanced_vampire", "de.teamlapen.vampirism.entity.vampire.AdvancedVampireEntity$IMob"},
            {"vampirism:vampire_minion", "de.teamlapen.vampirism.entity.minion.VampireMinionEntity"},
            {"vampirism:hunter", "de.teamlapen.vampirism.entity.hunter.BasicHunterEntity"},
            {"vampirism:hunter", "de.teamlapen.vampirism.entity.hunter.BasicHunterEntity$IMob"},
            {"vampirism:advanced_hunter", "de.teamlapen.vampirism.entity.hunter.AdvancedHunterEntity"},
            {"vampirism:advanced_hunter", "de.teamlapen.vampirism.entity.hunter.AdvancedHunterEntity$IMob"},
            {"vampirism:hunter_trainer", "de.teamlapen.vampirism.entity.hunter.HunterTrainerEntity"},
            {"vampirism:hunter_trainer_dummy", "de.teamlapen.vampirism.entity.hunter.DummyHunterTrainerEntity"},
            {"vampirism:hunter_minion", "de.teamlapen.vampirism.entity.minion.HunterMinionEntity"}
    };

    private static final String[][] VILLAGER_ENTITIES = {
            {"vampirism:task_master_vampire", "de.teamlapen.vampirism.entity.vampire.VampireTaskMasterEntity"},
            {"vampirism:task_master_hunter", "de.teamlapen.vampirism.entity.hunter.HunterTaskMasterEntity"},
            {"vampirism:villager_angry", "de.teamlapen.vampirism.entity.hunter.AggressiveVillagerEntity"},
            {"vampirism:villager_converted", "de.teamlapen.vampirism.entity.converted.ConvertedVillagerEntity"}
    };

    private VampirismCompat()
    {
    }

    public static void register(AddonAnimationRegistry registry, String[] animations, String[] alterableParts)
    {
        if (!Platform.isModLoaded(MOD_ID))
        {
            return;
        }

        for (final String[] entry : HUMANOID_ENTITIES)
        {
            final Class<LivingEntity> entityClass = resolve(entry[1]);
            if (entityClass == null)
            {
                continue;
            }

            try
            {
                registry.registerNewEntity(entry[0], unlocalizedNameOf(entry[0]), entityClass,
                        HumanoidMobData::new, HumanoidMobMutator::new,
                        new BipedRenderer<>(), new BipedPreviewer<>(), animations, alterableParts);
            }
            catch (Throwable ignored)
            {
            }
        }

        for (final String[] entry : VILLAGER_ENTITIES)
        {
            final Class<LivingEntity> entityClass = resolve(entry[1]);
            if (entityClass == null)
            {
                continue;
            }

            try
            {
                registry.registerNewEntity(entry[0], unlocalizedNameOf(entry[0]), entityClass,
                        VillagerData::new, VillagerMutator::new,
                        new BipedRenderer<>(), new BipedPreviewer<>(), animations, alterableParts);
            }
            catch (Throwable ignored)
            {
            }
        }
    }

    private static String unlocalizedNameOf(String key)
    {
        return "entity." + key.replace(':', '.');
    }

    @SuppressWarnings("unchecked")
    private static Class<LivingEntity> resolve(String className)
    {
        try
        {
            final Class<?> candidate = Class.forName(className);
            if (LivingEntity.class.isAssignableFrom(candidate))
            {
                return (Class<LivingEntity>) candidate;
            }
        }
        catch (Throwable ignored)
        {
        }

        return null;
    }
}
