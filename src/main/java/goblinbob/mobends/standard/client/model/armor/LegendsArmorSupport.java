package goblinbob.mobends.standard.client.model.armor;

import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Method;

public final class LegendsArmorSupport
{
    private static final int DEFAULT_SHINY_COLOR = 0xFFFFFF00;

    private static final boolean AVAILABLE;

    private static Class<?> suitModelClass;
    private static Class<?> armorItemClass;
    private static Method isShiny;
    private static Method getSuit;
    private static Method getShinyColor;
    private static Method getShinyRenderType;

    static
    {
        boolean available = false;

        try
        {
            suitModelClass = Class.forName("com.tihyo.legends.client.models.LegendsHumanoidModel");
            armorItemClass = Class.forName("com.tihyo.legends.armors.LegendsArmorItem");

            Class<?> suitClass = Class.forName("com.tihyo.legends.armors.LegendsSuit");
            Class<?> renderTypesClass = Class.forName("com.tihyo.legends.utils.client.LegendsRenderTypes");

            isShiny = armorItemClass.getMethod("isShiny", ItemStack.class);
            getSuit = armorItemClass.getMethod("getSuit");
            getShinyColor = suitClass.getMethod("getShinyColor");
            getShinyRenderType = renderTypesClass.getMethod("getShiny", ResourceLocation.class);

            available = true;
        }
        catch (Throwable ignored)
        {
        }

        AVAILABLE = available;
    }

    private LegendsArmorSupport()
    {
    }

    public static boolean isAvailable()
    {
        return AVAILABLE;
    }

    public static boolean isSuitModel(Model model)
    {
        return AVAILABLE && model != null && suitModelClass.isInstance(model);
    }

    public static boolean isSuitItem(ItemStack itemStack)
    {
        return AVAILABLE && itemStack != null && !itemStack.isEmpty() && armorItemClass.isInstance(itemStack.getItem());
    }

    public static boolean isShiny(ItemStack itemStack)
    {
        if (!isSuitItem(itemStack))
        {
            return false;
        }

        try
        {
            return Boolean.TRUE.equals(isShiny.invoke(null, itemStack));
        }
        catch (Throwable t)
        {
            return false;
        }
    }

    public static int getShinyColor(ItemStack itemStack)
    {
        if (!isSuitItem(itemStack))
        {
            return DEFAULT_SHINY_COLOR;
        }

        try
        {
            Object suit = getSuit.invoke(itemStack.getItem());
            if (suit == null)
            {
                return DEFAULT_SHINY_COLOR;
            }

            Object color = getShinyColor.invoke(suit);
            if (color instanceof java.awt.Color awtColor)
            {
                return 0xFF000000
                        | (awtColor.getRed() << 16)
                        | (awtColor.getGreen() << 8)
                        | awtColor.getBlue();
            }
        }
        catch (Throwable ignored)
        {
        }

        return DEFAULT_SHINY_COLOR;
    }

    public static RenderType shinyRenderType(ResourceLocation texture)
    {
        if (AVAILABLE)
        {
            try
            {
                Object renderType = getShinyRenderType.invoke(null, texture);
                if (renderType instanceof RenderType type)
                {
                    return type;
                }
            }
            catch (Throwable ignored)
            {
            }
        }

        return RenderType.entityCutoutNoCull(texture);
    }
}
