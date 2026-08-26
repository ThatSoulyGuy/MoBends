package goblinbob.mobends.platform;

import goblinbob.mobends.api.entity.IItemStack;
import goblinbob.mobends.api.resource.IResourcePath;
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
        Item item = itemStack.getItem();
        return item instanceof SwordItem ||
               item instanceof AxeItem ||
               item instanceof PickaxeItem ||
               item instanceof ShovelItem ||
               item instanceof HoeItem ||
               item instanceof TridentItem;
    }

    @Override
    public boolean isBow()
    {
        return itemStack.getItem() instanceof BowItem;
    }

    @Override
    public boolean isCrossbow()
    {
        return itemStack.getItem() instanceof CrossbowItem;
    }

    @Override
    public boolean isShield()
    {
        return itemStack.getItem() instanceof ShieldItem;
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
