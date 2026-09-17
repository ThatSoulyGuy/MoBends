package goblinbob.mobends.core.util;

import goblinbob.mobends.compat.TinkersConstructCompat;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.TridentItem;

public final class HeldItemHelper
{
    private static final String MACE_CLASS = "net.minecraft.world.item.MaceItem";

    private HeldItemHelper()
    {
    }

    public static boolean isSword(Item item)
    {
        return item instanceof SwordItem
                || isTagged(item, ItemTags.SWORDS)
                || isUntaggedMeleeWeapon(item)
                || TinkersConstructCompat.isMeleeWeapon(item)
                || CustomWeapons.matches(item);
    }

    public static boolean isSword(ItemStack itemStack)
    {
        return isSword(itemStack.getItem());
    }

    public static boolean isTool(Item item)
    {
        return item instanceof SwordItem
                || item instanceof AxeItem
                || item instanceof PickaxeItem
                || item instanceof ShovelItem
                || item instanceof HoeItem
                || item instanceof TridentItem
                || item instanceof TieredItem
                || isTagged(item, ItemTags.SWORDS)
                || isTagged(item, ItemTags.AXES)
                || isTagged(item, ItemTags.PICKAXES)
                || isTagged(item, ItemTags.SHOVELS)
                || isTagged(item, ItemTags.HOES)
                || TinkersConstructCompat.isTool(item);
    }

    public static boolean isBow(Item item)
    {
        return item instanceof BowItem || TinkersConstructCompat.isBow(item);
    }

    public static boolean isCrossbow(Item item)
    {
        return item instanceof CrossbowItem || TinkersConstructCompat.isCrossbow(item);
    }

    public static boolean isLoadedCrossbow(ItemStack itemStack)
    {
        if (itemStack.getItem() instanceof CrossbowItem)
        {
            return CrossbowItem.isCharged(itemStack);
        }

        return TinkersConstructCompat.isLoadedCrossbow(itemStack);
    }

    public static boolean isShield(Item item)
    {
        return item instanceof ShieldItem || TinkersConstructCompat.isShield(item);
    }

    private static boolean isUntaggedMeleeWeapon(Item item)
    {
        if (item == null)
        {
            return false;
        }

        if (item instanceof TieredItem && !(item instanceof DiggerItem))
        {
            return true;
        }

        return MACE_CLASS.equals(item.getClass().getName());
    }

    private static boolean isTagged(Item item, TagKey<Item> tag)
    {
        return item != null && item.builtInRegistryHolder().is(tag);
    }
}
