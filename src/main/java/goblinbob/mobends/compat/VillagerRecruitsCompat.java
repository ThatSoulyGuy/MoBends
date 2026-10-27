package goblinbob.mobends.compat;

import dev.architectury.platform.Platform;
import goblinbob.mobends.api.addon.AddonAnimationRegistry;
import goblinbob.mobends.standard.client.renderer.entity.mutated.BipedRenderer;
import goblinbob.mobends.standard.data.HumanoidMobData;
import goblinbob.mobends.standard.mutators.HumanoidMobMutator;
import goblinbob.mobends.standard.previewer.BipedPreviewer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.lang.reflect.Method;

public final class VillagerRecruitsCompat
{
    private static final String MOD_ID = "recruits";
    private static final String ENTITY_CLASS = "com.talhanation.recruits.entities.AbstractRecruitEntity";
    private static final byte PLAINS_BIOME = 2;

    private static Method setBiomeMethod;

    private VillagerRecruitsCompat()
    {
    }

    public static void initializePreviewAppearance(Entity entity)
    {
        if (setBiomeMethod == null || !setBiomeMethod.getDeclaringClass().isInstance(entity))
        {
            return;
        }

        try
        {
            setBiomeMethod.invoke(entity, PLAINS_BIOME);
        }
        catch (Exception ignored)
        {
        }
    }

    public static void register(AddonAnimationRegistry registry, String[] animations, String[] alterableParts)
    {
        if (!Platform.isModLoaded(MOD_ID))
        {
            return;
        }

        final Class<LivingEntity> entityClass = ModCompatManager.livingEntityClass(ENTITY_CLASS);
        if (entityClass == null)
        {
            return;
        }

        try
        {
            setBiomeMethod = entityClass.getMethod("setBiome", byte.class);
        }
        catch (Exception ignored)
        {
        }

        try
        {
            registry.registerNewEntity(MOD_ID + ":recruit", "entity." + MOD_ID + ".recruit", entityClass,
                    HumanoidMobData::new, HumanoidMobMutator::new,
                    new BipedRenderer<>(), new BipedPreviewer<>(), animations, alterableParts);
            ModCompatManager.coverSubclasses(entityClass);
        }
        catch (Throwable ignored)
        {
        }
    }
}
