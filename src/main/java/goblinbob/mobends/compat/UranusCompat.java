package goblinbob.mobends.compat;

import dev.architectury.platform.Platform;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
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
            renderers = (Map<?, ?>) rendererBase.getField("RENDERERS").get(null);
        }
        catch (Throwable e)
        {
            renderers = null;
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
}
