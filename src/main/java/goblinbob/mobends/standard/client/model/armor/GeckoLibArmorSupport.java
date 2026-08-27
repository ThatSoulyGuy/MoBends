package goblinbob.mobends.standard.client.model.armor;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.lang.reflect.Method;

public final class GeckoLibArmorSupport
{
    private static boolean initialized = false;
    private static boolean available = false;

    private static Method providerOfStack;
    private static Method providerGetArmorRenderer;
    private static Class<?> armorRendererClass;
    private static Method rendererPrepForRender;

    private GeckoLibArmorSupport()
    {
    }

    private static void init()
    {
        if (initialized)
        {
            return;
        }
        initialized = true;

        try
        {
            Class<?> providerClass = Class.forName("software.bernie.geckolib.animatable.client.GeoRenderProvider");

            providerOfStack = providerClass.getMethod("of", ItemStack.class);
            providerGetArmorRenderer = providerClass.getMethod("getGeoArmorRenderer",
                    LivingEntity.class, ItemStack.class, EquipmentSlot.class, HumanoidModel.class);

            try
            {
                armorRendererClass = Class.forName("software.bernie.geckolib.renderer.GeoArmorRenderer");
                rendererPrepForRender = armorRendererClass.getMethod("prepForRender",
                        net.minecraft.world.entity.Entity.class, ItemStack.class,
                        EquipmentSlot.class, HumanoidModel.class);
            }
            catch (Throwable ignored)
            {
                rendererPrepForRender = null;
            }

            available = true;
        }
        catch (Throwable t)
        {
            available = false;
        }
    }

    private static Model lastRenderer;
    private static Object lastEntity;
    private static ItemStack lastStack;
    private static EquipmentSlot lastSlot;
    private static HumanoidModel<?> lastOriginal;

    public static <E extends LivingEntity> void prepare(
            Model renderer, E entity, ItemStack itemStack, EquipmentSlot slot, HumanoidModel<E> original)
    {
        init();

        if (rendererPrepForRender == null || renderer == null || !armorRendererClass.isInstance(renderer))
        {
            return;
        }

        lastRenderer = renderer;
        lastEntity = entity;
        lastStack = itemStack;
        lastSlot = slot;
        lastOriginal = original;

        invokePrep(renderer);
    }

    public static void reprepare(Model renderer)
    {
        if (renderer == null || renderer != lastRenderer || rendererPrepForRender == null)
        {
            return;
        }

        invokePrep(renderer);
    }

    private static void invokePrep(Model renderer)
    {
        try
        {
            rendererPrepForRender.invoke(renderer, lastEntity, lastStack, lastSlot, lastOriginal);
        }
        catch (Throwable ignored)
        {
        }
    }

    @Nullable
    public static <E extends LivingEntity> Model getArmorRenderer(
            E entity, ItemStack itemStack, EquipmentSlot slot, HumanoidModel<E> original)
    {
        init();

        if (!available || itemStack == null || itemStack.isEmpty())
        {
            return null;
        }

        try
        {
            Object provider = providerOfStack.invoke(null, itemStack);
            if (provider == null)
            {
                return null;
            }

            Object renderer = providerGetArmorRenderer.invoke(provider, entity, itemStack, slot, original);


            if (!(renderer instanceof Model model) || model == original)
            {
                return null;
            }

            prepare(model, entity, itemStack, slot, original);

            return model;
        }
        catch (Throwable ignored)
        {
            return null;
        }
    }
}
