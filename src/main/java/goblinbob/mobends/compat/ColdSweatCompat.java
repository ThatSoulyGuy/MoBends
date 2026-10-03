package goblinbob.mobends.compat;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.architectury.platform.Platform;
import goblinbob.mobends.core.util.ResourceLocationFactory;
import goblinbob.mobends.standard.mutators.BipedMutator;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.function.Supplier;

public final class ColdSweatCompat
{
    private static final String LAYER_CLASS = "com.momosoftworks.coldsweat.client.renderer.layer.ChameleonArmorLayer";

    private static boolean initialized = false;
    private static Class<?> chameleonItemClass;
    private static Method realArmorModelMethod;
    private static Method adaptiveFactorMethod;
    private static Method holdingLampMethod;
    private static Supplier<?> poseLampSetting;

    private ColdSweatCompat()
    {
    }

    private static void init()
    {
        initialized = true;

        if (!Platform.isModLoaded("cold_sweat"))
        {
            return;
        }

        try
        {
            chameleonItemClass = Class.forName("com.momosoftworks.coldsweat.common.item.ChameleonArmorItem");
            realArmorModelMethod = Class.forName("com.momosoftworks.coldsweat.common.item.ChameleonArmorItem$Client")
                    .getMethod("getRealArmorModel", LivingEntity.class, ItemStack.class, EquipmentSlot.class);
            adaptiveFactorMethod = Class.forName("com.momosoftworks.coldsweat.api.insulation.AdaptiveInsulation")
                    .getMethod("getFactorFromArmor", ItemStack.class);
        }
        catch (Throwable e)
        {
            chameleonItemClass = null;
        }

        try
        {
            holdingLampMethod = Class.forName("com.momosoftworks.coldsweat.util.entity.EntityHelper")
                    .getMethod("holdingLamp", LivingEntity.class, HumanoidArm.class);
            poseLampSetting = (Supplier<?>) Class.forName("com.momosoftworks.coldsweat.config.ConfigSettings")
                    .getField("POSE_SOULSPRING_LAMP").get(null);
        }
        catch (Throwable e)
        {
            holdingLampMethod = null;
        }
    }

    public static boolean isChameleonLayer(Object layer)
    {
        return layer != null && layer.getClass().getName().equals(LAYER_CLASS);
    }

    public static <E extends LivingEntity, M extends EntityModel<E>> RenderLayer<E, M> emptyLayer(RenderLayerParent<E, M> parent)
    {
        return new RenderLayer<>(parent)
        {
            @Override
            public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, E entity,
                               float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks,
                               float netHeadYaw, float headPitch)
            {
            }
        };
    }

    public static boolean isChameleonArmor(ItemStack itemStack)
    {
        if (!initialized)
        {
            init();
        }
        return chameleonItemClass != null && chameleonItemClass.isInstance(itemStack.getItem());
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public static <E extends LivingEntity> HumanoidModel<E> getChameleonModel(LivingEntity entity, ItemStack itemStack, EquipmentSlot slot)
    {
        try
        {
            return (HumanoidModel<E>) realArmorModelMethod.invoke(null, entity, itemStack, slot);
        }
        catch (Throwable e)
        {
            return null;
        }
    }

    public static double getAdaptiveFactor(ItemStack itemStack)
    {
        try
        {
            return (Double) adaptiveFactorMethod.invoke(null, itemStack);
        }
        catch (Throwable e)
        {
            return 0.0D;
        }
    }

    public static ResourceLocation getChameleonTexture(EquipmentSlot slot, String suffix)
    {
        return ResourceLocationFactory.create("cold_sweat",
                "textures/models/armor/chameleon_layer_" + (slot == EquipmentSlot.LEGS ? 2 : 1) + suffix + ".png");
    }

    public static void applyLampPose(LivingEntity entity, BipedMutator<?, ?, ?> mutator, HumanoidModel<?> vanillaModel)
    {
        if (!initialized)
        {
            init();
        }

        if (holdingLampMethod == null || !(entity instanceof Player) || mutator == null || vanillaModel == null)
        {
            return;
        }

        try
        {
            if (!Boolean.TRUE.equals(poseLampSetting.get()))
            {
                return;
            }

            final boolean left = (Boolean) holdingLampMethod.invoke(null, entity, HumanoidArm.LEFT);
            final boolean right = (Boolean) holdingLampMethod.invoke(null, entity, HumanoidArm.RIGHT);

            if (left || right)
            {
                mutator.adoptUpperBodyFromVanillaModel(vanillaModel, false, left, right, true);
            }
        }
        catch (Throwable ignored)
        {
        }
    }
}
