package goblinbob.mobends.compat;

import dev.architectury.platform.Platform;
import goblinbob.mobends.core.util.ResourceLocationFactory;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class TinkersConstructCompat
{
    private static final String MOD_ID = "tconstruct";

    private static final String PERSISTENT_DATA_TAG = "tic_persistent";
    private static final String CROSSBOW_AMMO_KEY = "tconstruct:crossbow_ammo";

    private static boolean initialized = false;
    private static boolean isLoaded = false;

    private static TagKey<Item> meleePrimaryTag;
    private static TagKey<Item> harvestTag;
    private static TagKey<Item> harvestPrimaryTag;
    private static TagKey<Item> longbowTag;
    private static TagKey<Item> crossbowTag;
    private static TagKey<Item> shieldTag;

    public static void init()
    {
        if (initialized)
        {
            return;
        }
        initialized = true;

        isLoaded = Platform.isModLoaded(MOD_ID);

        if (isLoaded)
        {
            meleePrimaryTag = tag("modifiable/melee/primary");
            harvestTag = tag("modifiable/harvest");
            harvestPrimaryTag = tag("modifiable/harvest/primary");
            longbowTag = tag("modifiable/ranged/longbows");
            crossbowTag = tag("modifiable/ranged/crossbows");
            shieldTag = tag("modifiable/shields");
        }
    }

    private static TagKey<Item> tag(String path)
    {
        return TagKey.create(Registries.ITEM, ResourceLocationFactory.create(MOD_ID, path));
    }

    public static boolean isModLoaded()
    {
        if (!initialized)
        {
            init();
        }
        return isLoaded;
    }

    public static boolean isMeleeWeapon(Item item)
    {
        return isModLoaded() && is(item, meleePrimaryTag) && !is(item, harvestPrimaryTag);
    }

    public static boolean isTool(Item item)
    {
        return isModLoaded() && (is(item, harvestTag) || is(item, meleePrimaryTag));
    }

    public static boolean isBow(Item item)
    {
        return isModLoaded() && is(item, longbowTag);
    }

    public static boolean isCrossbow(Item item)
    {
        return isModLoaded() && is(item, crossbowTag);
    }

    public static boolean isShield(Item item)
    {
        return isModLoaded() && is(item, shieldTag);
    }

    public static boolean isLoadedCrossbow(ItemStack itemStack)
    {
        if (!isCrossbow(itemStack.getItem()))
        {
            return false;
        }

        final CompoundTag tag = rawTagOf(itemStack);

        return tag != null && !tag.getCompound(PERSISTENT_DATA_TAG).getCompound(CROSSBOW_AMMO_KEY).isEmpty();
    }

    private static CompoundTag rawTagOf(ItemStack itemStack)
    {
        //? if >=1.21 {
        /*net.minecraft.world.item.component.CustomData customData =
                itemStack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
        return customData != null ? customData.copyTag() : null;
        *///?} else {
        return itemStack.getTag();
        //?}
    }

    private static boolean is(Item item, TagKey<Item> tag)
    {
        return tag != null && item.builtInRegistryHolder().is(tag);
    }
}
