package goblinbob.mobends.standard.client.model.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.SkullModelBase;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

public final class TinkersArmorSupport
{
    private static final boolean AVAILABLE;

    private static Class<?> multilayerModelClass;
    private static Class<?> slimeskullModelClass;

    private static Method supplierGetArmorTexture;
    private static Method textureRenderTexture;
    private static Method armorModelLayers;

    private static Field modelField;
    private static Field registryAccessField;
    private static Field hasWingsField;

    private static Field skullModelField;
    private static Field skullTextureField;
    private static Field skullColorField;
    private static Field skullWalkAnimationField;

    private static Object emptyTexture;
    private static Object typeArmor;
    private static Object typeLeggings;
    private static Object typeWings;

    static
    {
        boolean available = false;

        try
        {
            multilayerModelClass = Class.forName("slimeknights.tconstruct.library.client.armor.MultilayerArmorModel");
            Class<?> abstractModelClass = Class.forName("slimeknights.tconstruct.library.client.armor.AbstractArmorModel");
            Class<?> armorModelClass = Class.forName("slimeknights.tconstruct.library.client.armor.ArmorModelManager$ArmorModel");
            Class<?> supplierClass = Class.forName("slimeknights.tconstruct.library.client.armor.texture.ArmorTextureSupplier");
            Class<?> armorTextureClass = Class.forName("slimeknights.tconstruct.library.client.armor.texture.ArmorTextureSupplier$ArmorTexture");
            Class<?> textureTypeClass = Class.forName("slimeknights.tconstruct.library.client.armor.texture.ArmorTextureSupplier$TextureType");

            armorModelLayers = armorModelClass.getMethod("layers");
            supplierGetArmorTexture = supplierClass.getMethod("getArmorTexture",
                    ItemStack.class, textureTypeClass, net.minecraft.core.RegistryAccess.class);
            textureRenderTexture = armorTextureClass.getMethod("renderTexture",
                    Model.class, PoseStack.class, MultiBufferSource.class, int.class, int.class,
                    float.class, float.class, float.class, float.class, boolean.class);

            modelField = declaredField(multilayerModelClass, "model");
            registryAccessField = declaredField(multilayerModelClass, "registryAccess");
            hasWingsField = declaredField(abstractModelClass, "hasWings");

            emptyTexture = armorTextureClass.getField("EMPTY").get(null);

            for (Object constant : textureTypeClass.getEnumConstants())
            {
                String name = ((Enum<?>) constant).name();

                if ("ARMOR".equals(name)) typeArmor = constant;
                else if ("LEGGINGS".equals(name)) typeLeggings = constant;
                else if ("WINGS".equals(name)) typeWings = constant;
            }

            try
            {
                slimeskullModelClass = Class.forName("slimeknights.tconstruct.tools.client.SlimeskullArmorModel");
                skullModelField = declaredField(slimeskullModelClass, "headModel");
                skullTextureField = declaredField(slimeskullModelClass, "headTexture");
                skullColorField = declaredField(slimeskullModelClass, "headColor");
                skullWalkAnimationField = declaredField(slimeskullModelClass, "walkAnimation");
            }
            catch (Throwable ignored)
            {
                slimeskullModelClass = null;
            }

            available = typeArmor != null && typeLeggings != null && typeWings != null;
        }
        catch (Throwable ignored)
        {
        }

        AVAILABLE = available;
    }

    private TinkersArmorSupport()
    {
    }

    private static Field declaredField(Class<?> owner, String name) throws NoSuchFieldException
    {
        Field field = owner.getDeclaredField(name);
        field.setAccessible(true);
        return field;
    }

    public static boolean isAvailable()
    {
        return AVAILABLE;
    }

    public static boolean isTinkersArmorModel(@Nullable Model model)
    {
        return AVAILABLE && model != null && multilayerModelClass.isInstance(model);
    }

    public static boolean isSlimeskullModel(@Nullable Model model)
    {
        return AVAILABLE && slimeskullModelClass != null && slimeskullModelClass.isInstance(model);
    }

    public static List<?> getLayers(Model model)
    {
        if (!isTinkersArmorModel(model))
        {
            return Collections.emptyList();
        }

        try
        {
            Object armorModel = modelField.get(model);
            if (armorModel == null)
            {
                return Collections.emptyList();
            }

            Object layers = armorModelLayers.invoke(armorModel);
            return layers instanceof List<?> list ? list : Collections.emptyList();
        }
        catch (Throwable ignored)
        {
            return Collections.emptyList();
        }
    }

    @Nullable
    public static Object getRegistryAccess(Model model)
    {
        try
        {
            return registryAccessField.get(model);
        }
        catch (Throwable ignored)
        {
            return null;
        }
    }

    public static boolean hasWings(Model model)
    {
        try
        {
            return Boolean.TRUE.equals(hasWingsField.get(model));
        }
        catch (Throwable ignored)
        {
            return false;
        }
    }

    public static void setWings(Model model, boolean value)
    {
        try
        {
            hasWingsField.set(model, value);
        }
        catch (Throwable ignored)
        {
        }
    }

    public static Object textureTypeFor(EquipmentSlot slot)
    {
        return slot == EquipmentSlot.LEGS ? typeLeggings : typeArmor;
    }

    public static Object wingsTextureType()
    {
        return typeWings;
    }

    @Nullable
    public static Object getArmorTexture(Object supplier, ItemStack stack, Object textureType, @Nullable Object registryAccess)
    {
        if (supplier == null || textureType == null || registryAccess == null)
        {
            return null;
        }

        try
        {
            Object texture = supplierGetArmorTexture.invoke(supplier, stack, textureType, registryAccess);
            return texture == null || texture == emptyTexture ? null : texture;
        }
        catch (Throwable ignored)
        {
            return null;
        }
    }

    public static boolean renderTexture(Object armorTexture, Model model, PoseStack poseStack,
                                        MultiBufferSource bufferSource, int packedLight, int packedOverlay,
                                        boolean hasGlint)
    {
        try
        {
            textureRenderTexture.invoke(armorTexture, model, poseStack, bufferSource,
                    packedLight, packedOverlay, 1.0F, 1.0F, 1.0F, 1.0F, hasGlint);
            return true;
        }
        catch (Throwable ignored)
        {
            return false;
        }
    }

    @Nullable
    public static SkullModelBase getSkullModel(Model model)
    {
        if (!isSlimeskullModel(model))
        {
            return null;
        }

        try
        {
            Object skull = skullModelField.get(model);
            return skull instanceof SkullModelBase skullModel ? skullModel : null;
        }
        catch (Throwable ignored)
        {
            return null;
        }
    }

    public static void setSkullModel(Model model, @Nullable SkullModelBase skull)
    {
        try
        {
            skullModelField.set(model, skull);
        }
        catch (Throwable ignored)
        {
        }
    }

    @Nullable
    public static ResourceLocation getSkullTexture(Model model)
    {
        try
        {
            Object texture = skullTextureField.get(model);
            return texture instanceof ResourceLocation location ? location : null;
        }
        catch (Throwable ignored)
        {
            return null;
        }
    }

    public static int getSkullColor(Model model)
    {
        try
        {
            Object color = skullColorField.get(model);
            return color instanceof Integer value ? value : -1;
        }
        catch (Throwable ignored)
        {
            return -1;
        }
    }

    public static float getSkullWalkAnimation(Model model)
    {
        try
        {
            Object animation = skullWalkAnimationField.get(model);
            return animation instanceof Float value ? value : 0.0F;
        }
        catch (Throwable ignored)
        {
            return 0.0F;
        }
    }
}
