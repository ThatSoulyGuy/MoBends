package goblinbob.mobends.compat;

import dev.architectury.platform.Platform;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.lang.reflect.Method;

public class AzureLibCompat
{
    private static final String MOD_ID = "azurelib";

    private static boolean initialized = false;
    private static Method getRenderer = null;
    private static boolean rendererByStack = false;
    private static Method prepForRender;
    private static Method rendererPipeline;
    private static Method armorModel;
    private static Method config;
    private static Method textureLocation;
    private static Class<?> armorModelClass = null;
    private static Entity lastEntity;

    private static void init()
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

        for (String root : new String[] {"mod.azure.azurelib.common.render.", "mod.azure.azurelib.render."})
        {
            try
            {
                final Class<?> registry = Class.forName(root + "armor.AzArmorRendererRegistry");
                final Class<?> renderer = Class.forName(root + "armor.AzArmorRenderer");
                final Class<?> pipeline = Class.forName(root + "armor.AzArmorRendererPipeline");

                try
                {
                    getRenderer = registry.getMethod("getOrNull", ItemStack.class);
                    rendererByStack = true;
                }
                catch (NoSuchMethodException e)
                {
                    getRenderer = registry.getMethod("getOrNull", Item.class);
                    rendererByStack = false;
                }

                prepForRender = renderer.getMethod("prepForRender",
                        Entity.class, ItemStack.class, EquipmentSlot.class, HumanoidModel.class);
                rendererPipeline = renderer.getMethod("rendererPipeline");
                armorModel = pipeline.getMethod("armorModel");
                config = pipeline.getMethod("config");
                textureLocation = Class.forName(root + "AzRendererConfig")
                        .getMethod("textureLocation", Entity.class, Object.class);
                armorModelClass = Class.forName(root + "armor.AzArmorModel");
                return;
            }
            catch (Throwable e)
            {
                getRenderer = null;
            }
        }
    }

    @Nullable
    private static Object getRenderer(ItemStack itemStack) throws ReflectiveOperationException
    {
        init();

        if (getRenderer == null || itemStack.isEmpty())
        {
            return null;
        }

        return getRenderer.invoke(null, rendererByStack ? itemStack : itemStack.getItem());
    }

    @Nullable
    public static Model getArmorModel(LivingEntity entity, ItemStack itemStack, EquipmentSlot slot,
                                      HumanoidModel<?> defaultModel)
    {
        try
        {
            final Object renderer = getRenderer(itemStack);

            if (renderer == null)
            {
                return null;
            }

            prepForRender.invoke(renderer, entity, itemStack, slot, defaultModel);
            lastEntity = entity;

            return (Model) armorModel.invoke(rendererPipeline.invoke(renderer));
        }
        catch (Throwable e)
        {
            return null;
        }
    }

    @Nullable
    public static ResourceLocation getTexture(ItemStack itemStack)
    {
        try
        {
            final Object renderer = getRenderer(itemStack);

            return renderer == null
                    ? null
                    : (ResourceLocation) textureLocation.invoke(
                            config.invoke(rendererPipeline.invoke(renderer)), lastEntity, itemStack);
        }
        catch (Throwable e)
        {
            return null;
        }
    }

    public static boolean isArmorModel(Model model)
    {
        init();
        return armorModelClass != null && armorModelClass.isInstance(model);
    }
}
