package goblinbob.mobends.compat;

import dev.architectury.platform.Platform;
import goblinbob.mobends.api.addon.AddonAnimationRegistry;
import goblinbob.mobends.standard.client.renderer.entity.mutated.BipedRenderer;
import goblinbob.mobends.standard.client.renderer.entity.mutated.ZombieRenderer;
import goblinbob.mobends.standard.data.HumanoidMobData;
import goblinbob.mobends.standard.data.ZombieData;
import goblinbob.mobends.standard.mutators.VillagersRebornMutator;
import goblinbob.mobends.standard.mutators.VillagersRebornZombieMutator;
import goblinbob.mobends.standard.previewer.BipedPreviewer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Zombie;

public final class VillagersRebornCompat
{
    private static final String MOD_ID = "slimpatch";

    private static final String PACKAGE = "com.javic.slimpatch.";
    private static final String CONFIG_CLASS = PACKAGE + "Config";

    private static final String[][] VILLAGERS = {
            {"slimpatch:male_villager", "entity.mobends.villagersreborn_male_villager", "entity.MaleVillagerEntity"},
            {"slimpatch:female_villager", "entity.mobends.villagersreborn_female_villager", "entity.FemaleVillagerEntity"},
            {"slimpatch:human_trader", "entity.mobends.villagersreborn_wandering_trader", "entity.HumanWanderingTraderEntity"}
    };

    private static final String ZOMBIE_VILLAGER_CLASS = "entity.HumanZombieVillagerEntity";

    private VillagersRebornCompat()
    {
    }

    public static boolean isModLoaded()
    {
        return Platform.isModLoaded(MOD_ID);
    }

    public static boolean humanPillager()
    {
        return illagerModelEnabled("CUSTOM_PILLAGER_MODEL");
    }

    public static boolean humanVindicator()
    {
        return illagerModelEnabled("CUSTOM_VINDICATOR_MODEL");
    }

    public static boolean humanEvoker()
    {
        return illagerModelEnabled("CUSTOM_EVOKER_MODEL");
    }

    public static boolean humanWitch()
    {
        return isModLoaded() && configFlag("CUSTOM_WITCH_SKIN");
    }

    private static boolean illagerModelEnabled(String modelFlag)
    {
        return isModLoaded() && configFlag("CUSTOM_ILLAGER_SKINS") && configFlag(modelFlag);
    }

    private static boolean configFlag(String name)
    {
        try
        {
            final Object value = Class.forName(CONFIG_CLASS).getField(name).get(null);
            final Object flag = value.getClass().getMethod("get").invoke(value);
            return flag instanceof Boolean enabled ? enabled : true;
        }
        catch (Throwable t)
        {
            return true;
        }
    }

    public static void register(AddonAnimationRegistry registry, String[] villagerAnimations,
                                String[] zombieAnimations, String[] alterableParts)
    {
        if (!isModLoaded())
        {
            return;
        }

        for (final String[] villager : VILLAGERS)
        {
            final Class<LivingEntity> villagerClass = resolve(villager[2]);
            if (villagerClass == null)
            {
                continue;
            }

            try
            {
                registry.registerNewEntity(villager[0], villager[1], villagerClass,
                        HumanoidMobData::new, VillagersRebornMutator::new,
                        new BipedRenderer<>(), new BipedPreviewer<>(), villagerAnimations, alterableParts);
            }
            catch (Throwable ignored)
            {
            }
        }

        final Class<LivingEntity> zombieVillagerClass = resolve(ZOMBIE_VILLAGER_CLASS);
        if (zombieVillagerClass != null && Zombie.class.isAssignableFrom(zombieVillagerClass))
        {
            try
            {
                @SuppressWarnings("unchecked")
                final Class<Zombie> typed = (Class<Zombie>) (Class<?>) zombieVillagerClass;
                registry.registerNewEntity("slimpatch:human_zombie_villager",
                        "entity.mobends.villagersreborn_zombie_villager", typed,
                        ZombieData::new, VillagersRebornZombieMutator::new,
                        new ZombieRenderer<>(), new BipedPreviewer<>(), zombieAnimations, alterableParts);
            }
            catch (Throwable ignored)
            {
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static Class<LivingEntity> resolve(String className)
    {
        try
        {
            final Class<?> candidate = Class.forName(PACKAGE + className);
            return LivingEntity.class.isAssignableFrom(candidate) ? (Class<LivingEntity>) candidate : null;
        }
        catch (Throwable t)
        {
            return null;
        }
    }
}
