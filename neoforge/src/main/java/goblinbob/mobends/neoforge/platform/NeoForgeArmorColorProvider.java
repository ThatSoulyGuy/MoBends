package goblinbob.mobends.neoforge.platform;

import goblinbob.mobends.api.rendering.IArmorColorProvider;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;

public class NeoForgeArmorColorProvider implements IArmorColorProvider
{
    @Override
    public int getDyedColor(Object itemStack)
    {
        if (!(itemStack instanceof ItemStack stack))
        {
            return -1;
        }

        if (!stack.is(ItemTags.DYEABLE))
        {
            return -1;
        }

        return DyedItemColor.getOrDefault(stack, 0xA06540) & 0xFFFFFF;
    }

    @Override
    public boolean hasDyedColor(Object itemStack)
    {
        return itemStack instanceof ItemStack stack && stack.has(DataComponents.DYED_COLOR);
    }

    @Override
    public boolean isDyeable(Object itemStack)
    {
        return itemStack instanceof ItemStack stack && stack.is(ItemTags.DYEABLE);
    }
}
