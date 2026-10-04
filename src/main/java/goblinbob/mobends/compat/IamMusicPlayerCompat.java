package goblinbob.mobends.compat;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.architectury.platform.Platform;
import goblinbob.mobends.standard.mutators.BipedMutator;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Method;

public final class IamMusicPlayerCompat
{
    private static boolean initialized = false;
    private static Class<?> boomboxClass;
    private static Method transferMethod;

    private IamMusicPlayerCompat()
    {
    }

    private static void init()
    {
        initialized = true;

        if (!Platform.isModLoaded("iammusicplayer"))
        {
            return;
        }

        try
        {
            boomboxClass = Class.forName("dev.felnull.imp.item.BoomboxItem");
            transferMethod = boomboxClass.getMethod("getTransferProgress", ItemStack.class);
        }
        catch (Throwable e)
        {
            transferMethod = null;
        }
    }

    public static boolean isBoomboxRaised(ItemStack itemStack)
    {
        if (!initialized)
        {
            init();
        }

        if (transferMethod == null || !boomboxClass.isInstance(itemStack.getItem()))
        {
            return false;
        }

        try
        {
            return (Integer) transferMethod.invoke(null, itemStack) >= 1;
        }
        catch (Throwable e)
        {
            return false;
        }
    }

    public static void applyPose(LivingEntity entity, BipedMutator<?, ?, ?> mutator, HumanoidModel<?> vanillaModel)
    {
        if (entity == null || mutator == null || vanillaModel == null)
        {
            return;
        }

        final boolean rightMain = entity.getMainArm() == HumanoidArm.RIGHT;
        final boolean right = isBoomboxRaised(rightMain ? entity.getMainHandItem() : entity.getOffhandItem());
        final boolean left = isBoomboxRaised(rightMain ? entity.getOffhandItem() : entity.getMainHandItem());

        if (left || right)
        {
            mutator.adoptUpperBodyFromVanillaModel(vanillaModel, false, left, right, true);
        }
    }

    public static void transformRaisedBoombox(HumanoidArm arm, PoseStack poseStack)
    {
        final float side = arm == HumanoidArm.RIGHT ? 1.0F : -1.0F;
        poseStack.mulPose(Axis.ZP.rotationDegrees(-15.0F * side));
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.translate(side / 16.0F, 0.325F, 0.575F);
    }
}
