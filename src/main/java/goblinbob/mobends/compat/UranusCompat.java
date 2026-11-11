package goblinbob.mobends.compat;

import dev.architectury.platform.Platform;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.Map;

public class UranusCompat
{
    private static final String MOD_ID = "uranus";

    private static boolean initialized = false;
    private static Map<?, ?> renderers = null;
    private static Method getHumanoidArmorModelMethod = null;
    private static Method rendererTextureMethod = null;
    private static Method providerTextureMethod = null;

    public static void init()
    {
        if (initialized)
        {
            return;
        }
        initialized = true;

        if (!Platform.isModLoaded(MOD_ID))
        {
            return;
        }

        try
        {
            final Class<?> rendererBase = Class.forName("com.iafenvoy.uranus.client.render.armor.IArmorRendererBase");
            getHumanoidArmorModelMethod = rendererBase.getMethod("getHumanoidArmorModel",
                    LivingEntity.class, ItemStack.class, EquipmentSlot.class, HumanoidModel.class);
            rendererTextureMethod = rendererBase.getMethod("getArmorTexture",
                    ItemStack.class, Entity.class, EquipmentSlot.class);
            renderers = (Map<?, ?>) rendererBase.getField("RENDERERS").get(null);
        }
        catch (Throwable e)
        {
            renderers = null;
        }

        try
        {
            providerTextureMethod = Class.forName("com.iafenvoy.uranus.client.render.armor.IArmorTextureProvider")
                    .getMethod("getArmorTexture", ItemStack.class, Entity.class, EquipmentSlot.class, String.class);
        }
        catch (Throwable e)
        {
            providerTextureMethod = null;
        }
    }

    @Nullable
    public static Model getArmorModel(LivingEntity entity, ItemStack itemStack, EquipmentSlot slot,
                                      HumanoidModel<?> defaultModel)
    {
        if (!initialized)
        {
            init();
        }

        if (renderers == null)
        {
            return null;
        }

        final Object renderer = renderers.get(itemStack.getItem());

        if (renderer == null)
        {
            return null;
        }

        try
        {
            return (Model) getHumanoidArmorModelMethod.invoke(renderer, entity, itemStack, slot, defaultModel);
        }
        catch (Throwable e)
        {
            return null;
        }
    }

    @Nullable
    public static ResourceLocation getArmorTexture(ItemStack itemStack, Entity entity, EquipmentSlot slot)
    {
        if (!initialized)
        {
            init();
        }

        try
        {
            final Object renderer = renderers == null ? null : renderers.get(itemStack.getItem());

            if (renderer != null)
            {
                return (ResourceLocation) rendererTextureMethod.invoke(renderer, itemStack, entity, slot);
            }

            if (providerTextureMethod != null && providerTextureMethod.getDeclaringClass().isInstance(itemStack.getItem()))
            {
                return (ResourceLocation) providerTextureMethod.invoke(itemStack.getItem(), itemStack, entity, slot, null);
            }
        }
        catch (Throwable e)
        {
            return null;
        }

        return null;
    }
}
