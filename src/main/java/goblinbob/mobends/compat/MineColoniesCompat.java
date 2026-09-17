package goblinbob.mobends.compat;

import dev.architectury.platform.Platform;
import goblinbob.mobends.api.addon.AddonAnimationRegistry;
import goblinbob.mobends.standard.client.renderer.entity.mutated.BipedRenderer;
import goblinbob.mobends.standard.data.HumanoidMobData;
import goblinbob.mobends.standard.data.MineColoniesCitizenData;
import goblinbob.mobends.standard.mutators.MineColoniesMutator;
import goblinbob.mobends.standard.previewer.BipedPreviewer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Method;

public final class MineColoniesCompat
{
    private static final String MOD_ID = "minecolonies";

    private static final String CITIZEN_CLASS = "com.minecolonies.api.entity.citizen.AbstractEntityCitizen";
    private static final String[][] RAIDER_FACTIONS = {
            {"minecolonies:barbarian", "entity.mobends.minecolonies_barbarian",
                    "com.minecolonies.api.entity.mobs.barbarians.AbstractEntityBarbarian",
                    "com.minecolonies.api.entity.mobs.barbarians.AbstractEntityBarbarianRaider"},
            {"minecolonies:pirate", "entity.mobends.minecolonies_pirate",
                    "com.minecolonies.api.entity.mobs.pirates.AbstractEntityPirate",
                    "com.minecolonies.api.entity.mobs.pirates.AbstractEntityPirateRaider"},
            {"minecolonies:mummy", "entity.mobends.minecolonies_egyptian",
                    "com.minecolonies.api.entity.mobs.egyptians.AbstractEntityEgyptian",
                    "com.minecolonies.api.entity.mobs.egyptians.AbstractEntityEgyptianRaider"},
            {"minecolonies:amazon", "entity.mobends.minecolonies_amazon",
                    "com.minecolonies.api.entity.mobs.amazons.AbstractEntityAmazon",
                    "com.minecolonies.api.entity.mobs.amazons.AbstractEntityAmazonRaider"},
            {"minecolonies:shieldmaiden", "entity.mobends.minecolonies_norsemen",
                    "com.minecolonies.api.entity.mobs.vikings.AbstractEntityNorsemen",
                    "com.minecolonies.api.entity.mobs.vikings.AbstractEntityNorsemenRaider"},
            {"minecolonies:drownedpirate", "entity.mobends.minecolonies_drowned_pirate",
                    "com.minecolonies.api.entity.mobs.drownedpirate.AbstractDrownedEntityPirate",
                    "com.minecolonies.api.entity.mobs.drownedpirate.AbstractDrownedEntityPirateRaider"}
    };
    private static final String MERCENARY_CLASS = "com.minecolonies.core.entity.mobs.EntityMercenary";
    private static final String ARMOR_LAYER_CLASS = "com.minecolonies.core.client.render.CitizenArmorLayer";

    private static boolean initialized = false;
    private static boolean isLoaded = false;

    private static Class<LivingEntity> citizenClass;
    private static Class<LivingEntity> mercenaryClass;
    private static Class<?> armorLayerClass;
    private static Method getCitizenDataViewMethod;
    private static Method getDisplayArmorMethod;
    private static Method getCustomTextureUuidMethod;

    private MineColoniesCompat()
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

        citizenClass = resolve(CITIZEN_CLASS);
        mercenaryClass = resolve(MERCENARY_CLASS);
        isLoaded = true;

        try
        {
            armorLayerClass = Class.forName(ARMOR_LAYER_CLASS);
        }
        catch (Throwable ignored)
        {
            armorLayerClass = null;
        }

        if (citizenClass == null)
        {
            return;
        }

        try
        {
            getCitizenDataViewMethod = citizenClass.getMethod("getCitizenDataView");
            final Class<?> viewClass = getCitizenDataViewMethod.getReturnType();
            getDisplayArmorMethod = viewClass.getMethod("getDisplayArmor", EquipmentSlot.class);
            getCustomTextureUuidMethod = viewClass.getMethod("getCustomTextureUUID");
        }
        catch (Throwable ignored)
        {
            getCitizenDataViewMethod = null;
            getDisplayArmorMethod = null;
            getCustomTextureUuidMethod = null;
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

    public static boolean isCitizen(LivingEntity entity)
    {
        return isModLoaded() && citizenClass != null && entity != null && citizenClass.isInstance(entity);
    }

    public static void register(AddonAnimationRegistry registry, String[] animations, String[] alterableParts)
    {
        if (!isModLoaded())
        {
            return;
        }

        if (citizenClass != null)
        {
            try
            {
                registry.registerNewEntity("minecolonies:citizen", "entity.minecolonies.citizen", citizenClass,
                        MineColoniesCitizenData::new, MineColoniesMutator::new,
                        new BipedRenderer<>(), new BipedPreviewer<>(), animations, alterableParts);
                markGroupBender(citizenClass);
            }
            catch (Throwable ignored)
            {
            }
        }

        for (final String[] faction : RAIDER_FACTIONS)
        {
            for (int i = 2; i < faction.length; ++i)
            {
                final Class<LivingEntity> raiderClass = resolve(faction[i]);
                if (raiderClass == null)
                {
                    continue;
                }

                try
                {
                    registry.registerNewEntity(faction[0], faction[1], raiderClass,
                            HumanoidMobData::new, MineColoniesMutator::new,
                            new BipedRenderer<>(), new BipedPreviewer<>(), animations, alterableParts);
                    markGroupBender(raiderClass);
                }
                catch (Throwable ignored)
                {
                }
            }
        }

        if (mercenaryClass != null)
        {
            try
            {
                registry.registerNewEntity("minecolonies:mercenary", "entity.minecolonies.mercenary", mercenaryClass,
                        HumanoidMobData::new, MineColoniesMutator::new,
                        new BipedRenderer<>(), new BipedPreviewer<>(), animations, alterableParts);
                markGroupBender(mercenaryClass);
            }
            catch (Throwable ignored)
            {
            }
        }
    }

    public static ItemStack displayArmor(LivingEntity entity, EquipmentSlot slot)
    {
        final Object view = citizenDataViewOf(entity);
        if (view == null || getDisplayArmorMethod == null)
        {
            return null;
        }

        try
        {
            return getDisplayArmorMethod.invoke(view, slot) instanceof ItemStack stack && !stack.isEmpty()
                    ? stack : null;
        }
        catch (Throwable e)
        {
            return null;
        }
    }

    public static boolean hasCustomTexture(LivingEntity entity)
    {
        final Object view = citizenDataViewOf(entity);
        if (view == null || getCustomTextureUuidMethod == null)
        {
            return false;
        }

        try
        {
            return getCustomTextureUuidMethod.invoke(view) != null;
        }
        catch (Throwable e)
        {
            return false;
        }
    }

    public static boolean shouldRenderVanillaArmorLayer(RenderLayer<?, ?> vanillaLayer, LivingEntity entity)
    {
        return vanillaLayer != null && armorLayerClass != null && armorLayerClass.isInstance(vanillaLayer)
                && hasCustomTexture(entity);
    }

    private static Object citizenDataViewOf(LivingEntity entity)
    {
        if (!isCitizen(entity) || getCitizenDataViewMethod == null)
        {
            return null;
        }

        try
        {
            return getCitizenDataViewMethod.invoke(entity);
        }
        catch (Throwable e)
        {
            return null;
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
