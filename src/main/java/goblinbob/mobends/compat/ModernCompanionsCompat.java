package goblinbob.mobends.compat;

import dev.architectury.platform.Platform;
import goblinbob.mobends.api.addon.AddonAnimationRegistry;
import goblinbob.mobends.standard.client.renderer.entity.mutated.BipedRenderer;
import goblinbob.mobends.standard.data.ModernCompanionData;
import goblinbob.mobends.standard.mutators.HumanoidMobMutator;
import goblinbob.mobends.standard.previewer.BipedPreviewer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class ModernCompanionsCompat
{
    private static final String MOD_ID = "modern_companions";

    private static final String ENTITY_CLASS =
            "com.majorbonghits.moderncompanions.entity.AbstractHumanCompanionEntity";
    private static final String ARMOR_LAYER_CLASS =
            "com.majorbonghits.moderncompanions.client.renderer.CompanionRenderer$CompanionArmorLayer";

    private static final String PREVIEW_KEY = "modern_companions:knight";
    private static final String UNLOCALIZED_NAME = "entity.modern_companions.companion";

    private static boolean initialized = false;
    private static boolean isLoaded = false;

    private static Class<LivingEntity> entityClass;
    private static Class<?> armorLayerClass;
    private static Method usesAlexModelMethod;
    private static Field alexField;

    private ModernCompanionsCompat()
    {
    }

    public static void init()
    {
        if (initialized)
        {
            return;
        }
        initialized = true;

        if (!Platform.isModLoaded(MOD_ID))
        {
            return;
        }

        entityClass = resolve(ENTITY_CLASS);
        if (entityClass == null)
        {
            return;
        }

        isLoaded = true;

        try
        {
            usesAlexModelMethod = entityClass.getMethod("usesAlexModel");
        }
        catch (Throwable ignored)
        {
            usesAlexModelMethod = null;
        }

        try
        {
            armorLayerClass = Class.forName(ARMOR_LAYER_CLASS);
            alexField = armorLayerClass.getDeclaredField("alex");
            alexField.setAccessible(true);
        }
        catch (Throwable ignored)
        {
            armorLayerClass = null;
            alexField = null;
        }
    }

    public static boolean isModLoaded()
    {
        if (!initialized)
        {
            init();
        }
        return isLoaded;
    }

    public static boolean isCompanion(LivingEntity entity)
    {
        return isModLoaded() && entity != null && entityClass.isInstance(entity);
    }

    public static void register(AddonAnimationRegistry registry, String[] animations, String[] alterableParts)
    {
        if (!isModLoaded())
        {
            return;
        }

        try
        {
            registry.registerNewEntity(PREVIEW_KEY, UNLOCALIZED_NAME, entityClass,
                    ModernCompanionData::new, HumanoidMobMutator::new,
                    new BipedRenderer<>(), new BipedPreviewer<>(), animations, alterableParts);
            markGroupBender(entityClass);
        }
        catch (Throwable ignored)
        {
        }
    }

    public static boolean isSittingOnGround(LivingEntity entity)
    {
        return entity instanceof TamableAnimal tamable
                && tamable.isInSittingPose()
                && !entity.isPassenger();
    }

    public static boolean shouldSkipArmorLayer(RenderLayer<?, ?> vanillaLayer, LivingEntity entity)
    {
        if (!isModLoaded() || vanillaLayer == null || entity == null
                || armorLayerClass == null || alexField == null || usesAlexModelMethod == null
                || !armorLayerClass.isInstance(vanillaLayer) || !entityClass.isInstance(entity))
        {
            return false;
        }

        try
        {
            final boolean layerIsAlex = alexField.getBoolean(vanillaLayer);
            final Object entityIsAlex = usesAlexModelMethod.invoke(entity);
            return entityIsAlex instanceof Boolean alex && alex != layerIsAlex;
        }
        catch (Throwable e)
        {
            return false;
        }
    }

    private static void markGroupBender(Class<LivingEntity> entityClass)
    {
        final goblinbob.mobends.core.bender.EntityBender<LivingEntity> bender =
                goblinbob.mobends.core.bender.EntityBenderRegistry.instance.getForEntityClass(entityClass);
        if (bender != null)
        {
            bender.setCoversSubclasses(true);
        }
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
