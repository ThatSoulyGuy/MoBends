package goblinbob.mobends.core.util;

import goblinbob.mobends.compat.TinkersConstructCompat;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TridentItem;

public final class HeldItemHelper
{
    private HeldItemHelper()
    {
    }

    public static boolean isSword(Item item)
    {
        return item instanceof SwordItem || TinkersConstructCompat.isMeleeWeapon(item);
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
}
