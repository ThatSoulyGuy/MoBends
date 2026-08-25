package goblinbob.mobends.standard.client.model.armor;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.lang.reflect.Method;

public final class PalladiumSupport
{
    private static final boolean AVAILABLE;

    private static Class<?> armorWithRendererClass;
    private static Class<?> armorRendererDataClass;

    private static Method hasCustomRenderer;
    private static Method getCachedArmorRenderer;
    private static Method forArmorInSlot;
    private static Method getModel;
    private static Method getTexture;
    private static Method getTextureByKey;

    static
    {
        boolean available = false;

        try
        {
            armorWithRendererClass = Class.forName("net.threetag.palladium.item.ArmorWithRenderer");
            armorRendererDataClass = Class.forName("net.threetag.palladium.client.renderer.item.armor.ArmorRendererData");

            Class<?> dataContextClass = Class.forName("net.threetag.palladium.util.context.DataContext");

            hasCustomRenderer = armorWithRendererClass.getMethod("hasCustomRenderer");
            getCachedArmorRenderer = armorWithRendererClass.getMethod("getCachedArmorRenderer");

            forArmorInSlot = dataContextClass.getMethod("forArmorInSlot", LivingEntity.class, EquipmentSlot.class);

            getModel = armorRendererDataClass.getMethod("getModel", LivingEntity.class, dataContextClass);
            getTexture = armorRendererDataClass.getMethod("getTexture", dataContextClass);
            getTextureByKey = armorRendererDataClass.getMethod("getTexture", dataContextClass, String.class);

            available = true;
        }
        catch (Throwable ignored)
        {
        }

        AVAILABLE = available;
    }

    private PalladiumSupport()
    {
    }

    public static boolean isAvailable()
    {
        return AVAILABLE;
    }

    @Nullable
    public static Armor resolve(ItemStack itemStack, LivingEntity entity, EquipmentSlot slot)
    {
        if (!AVAILABLE || itemStack == null || itemStack.isEmpty() || entity == null)
        {
            return null;
        }

        final net.minecraft.world.item.Item item = itemStack.getItem();
        if (!armorWithRendererClass.isInstance(item))
        {
            return null;
        }

        try
        {
            if (!Boolean.TRUE.equals(hasCustomRenderer.invoke(item)))
            {
                return null;
            }

            Object rendererData = getCachedArmorRenderer.invoke(item);
            if (!armorRendererDataClass.isInstance(rendererData))
            {
                return null;
            }

            Object context = forArmorInSlot.invoke(null, entity, slot);

            Object model = getModel.invoke(rendererData, entity, context);
            Object texture = getTexture.invoke(rendererData, context);

            if (!(texture instanceof ResourceLocation armorTexture))
            {
                return null;
            }

            ResourceLocation overlayTexture = null;

            try
            {
                Object overlay = getTextureByKey.invoke(rendererData, context, "overlay");
                if (overlay instanceof ResourceLocation resolvedOverlay && !resolvedOverlay.equals(armorTexture))
                {
                    overlayTexture = resolvedOverlay;
                }
            }
            catch (Throwable ignored)
            {
            }

            return new Armor(model instanceof HumanoidModel<?> humanoidModel ? humanoidModel : null,
                    armorTexture, overlayTexture);
        }
        catch (Throwable ignored)
        {
            return null;
        }
    }

    public static int getDyeColor(ItemStack itemStack)
    {
        goblinbob.mobends.api.rendering.IArmorColorProvider colorProvider =
                goblinbob.mobends.api.rendering.IArmorColorProvider.Holder.getProvider();

        if (colorProvider == null || itemStack == null)
        {
            return 0xFFFFFFFF;
        }

        int dyed = colorProvider.getDyedColor(itemStack);
        return dyed == -1 ? 0xFFFFFFFF : (0xFF000000 | dyed);
    }

    public static boolean isPalladiumArmor(ItemStack itemStack)
    {
        return AVAILABLE
                && itemStack != null
                && !itemStack.isEmpty()
                && itemStack.getItem() instanceof ArmorItem
                && armorWithRendererClass.isInstance(itemStack.getItem());
    }

    public static final class Armor
    {
        @Nullable
        public final HumanoidModel<?> model;
        public final ResourceLocation texture;
        @Nullable
        public final ResourceLocation overlayTexture;

        private Armor(@Nullable HumanoidModel<?> model, ResourceLocation texture,
                      @Nullable ResourceLocation overlayTexture)
        {
            this.model = model;
            this.texture = texture;
            this.overlayTexture = overlayTexture;
        }
    }
}
