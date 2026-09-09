package goblinbob.mobends.platform;

import goblinbob.mobends.api.entity.IItemStack;
import goblinbob.mobends.api.resource.IResourcePath;
import goblinbob.mobends.core.util.HeldItemHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;

import javax.annotation.Nullable;

public class McItemStack implements IItemStack
{
    private final ItemStack itemStack;

    public McItemStack(ItemStack itemStack)
    {
        this.itemStack = itemStack;
    }

    @Override
    public boolean isEmpty()
    {
        return itemStack.isEmpty();
    }

    @Override
    public int getCount()
    {
        return itemStack.getCount();
    }

    @Override
    @Nullable
    public IResourcePath getItemId()
    {
        ResourceLocation key = BuiltInRegistries.ITEM.getKey(itemStack.getItem());
        return new McResourcePath(key);
    }

    @Override
    public boolean isTool()
    {
        return HeldItemHelper.isTool(itemStack.getItem());
    }

    @Override
    public boolean isBow()
    {
        return HeldItemHelper.isBow(itemStack.getItem());
    }

    @Override
    public boolean isCrossbow()
    {
        return HeldItemHelper.isCrossbow(itemStack.getItem());
    }

    @Override
    public boolean isShield()
    {
        return HeldItemHelper.isShield(itemStack.getItem());
    }

    @Override
    public boolean isFood()
    {
        //? if >=1.21 {
        /*return itemStack.has(net.minecraft.core.component.DataComponents.FOOD);
        *///?} else {
        return itemStack.isEdible();
        //?}
    }

    @Override
    public boolean isTrident()
    {
        return itemStack.getItem() instanceof TridentItem;
    }

    @Override
    public boolean isSpyglass()
    {
        return itemStack.getItem() instanceof SpyglassItem;
    }

    @Override
    public Object getNative()
    {
        return itemStack;
    }

    public ItemStack getItemStack()
    {
        return itemStack;
    }
}
