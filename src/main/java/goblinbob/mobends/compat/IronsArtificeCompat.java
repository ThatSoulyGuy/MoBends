package goblinbob.mobends.compat;

import dev.architectury.platform.Platform;
import goblinbob.mobends.standard.mutators.BipedMutator;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Method;

public final class IronsArtificeCompat
{
    private static boolean initialized = false;
    private static Class<?> gunClass;
    private static Method poseForMethod;
    private static Method reloadStateMethod;
    private static Method reloadFinishedMethod;

    private IronsArtificeCompat()
    {
    }

    private static void init()
    {
        initialized = true;

        if (!Platform.isModLoaded("irons_artifice"))
        {
            return;
        }

        try
        {
            gunClass = Class.forName("io.redspace.irons_artifice.item.GunItem");
            poseForMethod = Class.forName("io.redspace.irons_artifice.client.gun.GunArmPoses")
                    .getMethod("poseFor", gunClass);
            final Class<?> reloadStateClass = Class.forName("io.redspace.irons_artifice.item.ReloadState");
            reloadStateMethod = reloadStateClass.getMethod("get", ItemStack.class);
            reloadFinishedMethod = reloadStateClass.getMethod("isFinished");
        }
        catch (Throwable e)
        {
            poseForMethod = null;
        }
    }

    public static void applyPose(LivingEntity entity, BipedMutator<?, ?, ?> mutator, HumanoidModel<?> vanillaModel)
    {
        if (!(entity instanceof Player || entity instanceof Drowned) || mutator == null || vanillaModel == null)
        {
            return;
        }

        if (!initialized)
        {
            init();
        }

        if (poseForMethod == null)
        {
            return;
        }

        final boolean rightMain = entity.getMainArm() == HumanoidArm.RIGHT;
        final Boolean right = posesBothArms(rightMain ? entity.getMainHandItem() : entity.getOffhandItem());
        final Boolean left = posesBothArms(rightMain ? entity.getOffhandItem() : entity.getMainHandItem());

        if (right == null && left == null)
        {
            return;
        }

        final boolean both = Boolean.TRUE.equals(right) || Boolean.TRUE.equals(left);
        mutator.adoptUpperBodyFromVanillaModel(vanillaModel, false, both || left != null, both || right != null, false);
    }

    private static Boolean posesBothArms(ItemStack itemStack)
    {
        if (!gunClass.isInstance(itemStack.getItem()))
        {
            return null;
        }

        try
        {
            final HumanoidModel.ArmPose pose = (HumanoidModel.ArmPose) poseForMethod.invoke(null, itemStack.getItem());
            final Object reloadState = reloadStateMethod.invoke(null, itemStack);
            return pose.isTwoHanded() || reloadState != null && !(Boolean) reloadFinishedMethod.invoke(reloadState);
        }
        catch (Throwable e)
        {
            return null;
        }
    }
}
