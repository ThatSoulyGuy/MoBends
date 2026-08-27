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

        if (!stack.has(DataComponents.DYED_COLOR))
        {
            Integer own = itemDefinedColor(stack);
            if (own != null)
            {
                return own & 0xFFFFFF;
            }
        }

        return DyedItemColor.getOrDefault(stack, 0xA06540) & 0xFFFFFF;
    }

    private static final java.util.Map<Class<?>, java.lang.reflect.Method> COLOR_METHODS =
            new java.util.concurrent.ConcurrentHashMap<>();

    private static final java.lang.reflect.Method NONE;

    static
    {
        java.lang.reflect.Method none = null;
        try
        {
            none = NeoForgeArmorColorProvider.class.getDeclaredMethod("noColor");
        }
        catch (NoSuchMethodException ignored)
        {
        }
        NONE = none;
    }

    @SuppressWarnings("unused")
    private static void noColor()
    {
    }

    private static Integer itemDefinedColor(ItemStack stack)
    {
        final Class<?> itemClass = stack.getItem().getClass();

        java.lang.reflect.Method method = COLOR_METHODS.computeIfAbsent(itemClass, type -> {
            try
            {
                return type.getMethod("getColor", ItemStack.class);
            }
            catch (Throwable ignored)
            {
                return NONE;
            }
        });

        if (method == null || method == NONE)
        {
            return null;
        }

        try
        {
            Object value = method.invoke(stack.getItem(), stack);
            return value instanceof Integer color ? color : null;
        }
        catch (Throwable ignored)
        {
            return null;
        }
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
