package goblinbob.mobends.compat;

import dev.architectury.platform.Platform;
import goblinbob.mobends.api.addon.AddonAnimationRegistry;
import goblinbob.mobends.standard.client.renderer.entity.mutated.BipedRenderer;
import goblinbob.mobends.standard.data.HumanoidMobData;
import goblinbob.mobends.standard.mutators.BipedMutator;
import goblinbob.mobends.standard.mutators.HumanoidMobMutator;
import goblinbob.mobends.standard.previewer.BipedPreviewer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.LivingEntity;

import java.lang.reflect.Field;

public final class ValarianConquestCompat
{
    private static final String MOD_ID = "valarian_conquest";
    private static final String ENTITY_PACKAGE = "net.mcreator.valarian_conquest.entity.";
    private static final String[][] ENTITIES = {
            {"SoldierEntity", "soldier"},
            {"ArcherEntity", "archer"},
            {"MaleCitizenEntity", "male_citizen"},
            {"FemaleCitizenEntity", "female_citizen"}
    };

    private static Field holdSpearAnimationField;

    private ValarianConquestCompat()
    {
    }

    public static void register(AddonAnimationRegistry registry, String[] animations, String[] alterableParts)
    {
        if (!Platform.isModLoaded(MOD_ID))
        {
            return;
        }

        for (String[] entity : ENTITIES)
        {
            final Class<LivingEntity> entityClass = ModCompatManager.livingEntityClass(ENTITY_PACKAGE + entity[0]);
            if (entityClass == null)
            {
                continue;
            }

            try
            {
                registry.registerNewEntity(MOD_ID + ":" + entity[1], "entity." + MOD_ID + "." + entity[1], entityClass,
                        HumanoidMobData::new, HumanoidMobMutator::new,
                        new BipedRenderer<>(), new BipedPreviewer<>(), animations, alterableParts);
            }
            catch (Throwable ignored)
            {
            }
        }

        try
        {
            holdSpearAnimationField = Class.forName(ENTITY_PACKAGE + "SoldierEntity").getField("animationState0");
        }
        catch (Throwable e)
        {
            holdSpearAnimationField = null;
        }
    }

    public static void applyPose(LivingEntity entity, BipedMutator<?, ?, ?> mutator, HumanoidModel<?> vanillaModel)
    {
        if (holdSpearAnimationField == null || mutator == null || vanillaModel == null
                || !holdSpearAnimationField.getDeclaringClass().isInstance(entity))
        {
            return;
        }

        try
        {
            if (((AnimationState) holdSpearAnimationField.get(entity)).isStarted())
            {
                mutator.adoptUpperBodyFromVanillaModel(vanillaModel, false, false, true);
            }
        }
        catch (Throwable e)
        {
            holdSpearAnimationField = null;
        }
    }
}
