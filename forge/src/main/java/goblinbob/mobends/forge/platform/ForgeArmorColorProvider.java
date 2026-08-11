package goblinbob.mobends.forge.platform;

import goblinbob.mobends.api.rendering.IArmorColorProvider;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.ItemStack;

public class ForgeArmorColorProvider implements IArmorColorProvider
{
    @Override
    public int getDyedColor(Object itemStack)
    {
        if (!(itemStack instanceof ItemStack stack))
        {
            return -1;
        }

        if (stack.getItem() instanceof DyeableLeatherItem dyeable && dyeable.hasCustomColor(stack))
        {
            return dyeable.getColor(stack) & 0xFFFFFF;
        }

        return -1;
    }

    @Override
    public boolean hasDyedColor(Object itemStack)
    {
        return itemStack instanceof ItemStack stack
                && stack.getItem() instanceof DyeableLeatherItem dyeable
                && dyeable.hasCustomColor(stack);
    }

    @Override
    public boolean isDyeable(Object itemStack)
    {
        return itemStack instanceof ItemStack stack && stack.getItem() instanceof DyeableLeatherItem;
    }
}
