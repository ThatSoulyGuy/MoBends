package goblinbob.mobends.compat;

import goblinbob.mobends.api.addon.AddonAnimationRegistry;
import goblinbob.mobends.standard.client.renderer.entity.mutated.BipedRenderer;
import goblinbob.mobends.standard.client.renderer.entity.mutated.VillagerRenderer;
import goblinbob.mobends.standard.data.PigZombieData;
import goblinbob.mobends.standard.data.PiglinData;
import goblinbob.mobends.standard.data.VillagerData;
import goblinbob.mobends.standard.data.ZombieLikeData;
import goblinbob.mobends.standard.mutators.AdaptiveHumanoidMutator;
import goblinbob.mobends.standard.mutators.CorruptedLunarianMutator;
import goblinbob.mobends.standard.mutators.LunarianMutator;
import goblinbob.mobends.standard.previewer.BipedPreviewer;
import goblinbob.mobends.standard.previewer.PiglinPreviewer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;

public final class AdAstraCompat
{
    private static final String MOBS = "earth.terrarium.adastra.common.entities.mob.";

    private AdAstraCompat()
    {
    }

    @SuppressWarnings("unchecked")
    private static <T extends LivingEntity> Class<T> entityClass(String name, Class<? super T> base)
    {
        final Class<LivingEntity> found = ModCompatManager.livingEntityClass(MOBS + name);
        return found != null && base.isAssignableFrom(found) ? (Class<T>) (Class<?>) found : null;
    }

    private static void tryRegister(Runnable registration)
    {
        try
        {
            registration.run();
        }
        catch (Throwable ignored)
        {
        }
    }

    public static void register(AddonAnimationRegistry registry, String[] sprintingAnimations,
                                String[] bipedAnimations, String[] alterableParts)
    {
        final Class<LivingEntity> lunarian = entityClass("Lunarian", LivingEntity.class);
        if (lunarian == null)
        {
            return;
        }

        tryRegister(() -> registry.registerNewEntity("ad_astra:lunarian", "entity.ad_astra.lunarian", lunarian,
                VillagerData::new, LunarianMutator::new, new VillagerRenderer<>(),
                new BipedPreviewer<>(), sprintingAnimations, alterableParts));

        final Class<LivingEntity> trader = entityClass("LunarianWanderingTrader", LivingEntity.class);
        if (trader != null)
        {
            tryRegister(() -> registry.registerNewEntity("ad_astra:lunarian_wandering_trader",
                    "entity.ad_astra.lunarian_wandering_trader", trader,
                    VillagerData::new, LunarianMutator::new, new BipedRenderer<>(),
                    new BipedPreviewer<>(), sprintingAnimations, alterableParts));
        }

        final Class<Mob> corrupted = entityClass("CorruptedLunarian", Mob.class);
        if (corrupted != null)
        {
            tryRegister(() -> registry.registerNewEntity("ad_astra:corrupted_lunarian",
                    "entity.ad_astra.corrupted_lunarian", corrupted,
                    ZombieLikeData::new, CorruptedLunarianMutator::new, new BipedRenderer<>(),
                    new BipedPreviewer<>(), bipedAnimations, alterableParts));
        }

        for (String[] pygro : new String[][] {{"Pygro", "pygro"}, {"PygroBrute", "pygro_brute"}})
        {
            final Class<AbstractPiglin> pygroClass = entityClass(pygro[0], AbstractPiglin.class);
            if (pygroClass != null)
            {
                tryRegister(() -> registry.registerNewEntity("ad_astra:" + pygro[1], "entity.ad_astra." + pygro[1],
                        pygroClass, PiglinData::new, AdaptiveHumanoidMutator::new, new BipedRenderer<>(),
                        new PiglinPreviewer<>(), sprintingAnimations, alterableParts));
            }
        }

        final Class<ZombifiedPiglin> zombifiedPygro = entityClass("ZombifiedPygro", ZombifiedPiglin.class);
        if (zombifiedPygro != null)
        {
            tryRegister(() -> registry.registerNewEntity("ad_astra:zombified_pygro",
                    "entity.ad_astra.zombified_pygro", zombifiedPygro,
                    PigZombieData::new, AdaptiveHumanoidMutator::new, new BipedRenderer<>(),
                    new BipedPreviewer<>(), bipedAnimations, alterableParts));
        }
    }
}
