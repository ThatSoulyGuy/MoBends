package goblinbob.mobends.compat;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.architectury.platform.Platform;
import goblinbob.mobends.standard.AttackActionType;
import goblinbob.mobends.standard.animation.bit.biped.item.BipedActionController;
import goblinbob.mobends.standard.animation.bit.biped.item.StabAction;
import goblinbob.mobends.standard.mutators.BipedMutator;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Method;

public final class VanillaBackportCompat
{
    private static boolean initialized = false;
    private static Method attackItemMethod;
    private static Method useItemMethod;

    private VanillaBackportCompat()
    {
    }

    private static void init()
    {
        initialized = true;

        if (!Platform.isModLoaded("vanillabackport"))
        {
            return;
        }

        try
        {
            final Class<?> spearAnimations = Class.forName("com.blackgear.vanillabackport.common.level.items.spear.SpearAnimations");
            attackItemMethod = spearAnimations.getMethod("thirdPersonAttackItem",
                    HumanoidModel.class, LivingEntity.class, PoseStack.class);
            useItemMethod = spearAnimations.getMethod("thirdPersonUseItem", HumanoidModel.class, LivingEntity.class,
                    PoseStack.class, float.class, HumanoidArm.class, ItemStack.class);
        }
        catch (Throwable e)
        {
            useItemMethod = null;
        }
    }

    private static HumanoidArm getJoustingArm(LivingEntity entity, HumanoidModel<?> model)
    {
        if (!entity.isUsingItem())
        {
            return null;
        }

        final HumanoidArm arm = entity.getUsedItemHand() == InteractionHand.MAIN_HAND
                ? entity.getMainArm()
                : entity.getMainArm().getOpposite();
        final HumanoidModel.ArmPose pose = arm == HumanoidArm.RIGHT ? model.rightArmPose : model.leftArmPose;

        return pose != null && "SPEAR".equals(pose.name()) ? arm : null;
    }

    public static void applyPose(LivingEntity entity, BipedMutator<?, ?, ?> mutator, HumanoidModel<?> vanillaModel)
    {
        if (entity == null || mutator == null || vanillaModel == null)
        {
            return;
        }

        final HumanoidArm arm = getJoustingArm(entity, vanillaModel);
        if (arm != null)
        {
            mutator.adoptUpperBodyFromVanillaModel(vanillaModel, false,
                    arm == HumanoidArm.LEFT, arm == HumanoidArm.RIGHT, false);
        }
    }

    private static float stabProgress(LivingEntity entity, HumanoidArm arm, ItemStack itemStack)
    {
        return entity.getMainArm() == arm
                && BipedActionController.getItemAttackAction(itemStack.getItem()) == AttackActionType.STAB
                && goblinbob.mobends.core.data.EntityDatabase.instance.get(entity)
                        instanceof goblinbob.mobends.standard.data.BipedEntityData<?> data
                && !goblinbob.mobends.standard.animation.bit.biped.SwimmingAnimationBit.isSwimmingHorizontally(data)
                ? StabAction.stabProgress(data, arm)
                : 1.0F;
    }

    public static void transformSpearItem(LivingEntity entity, EntityModel<?> model, HumanoidArm arm,
                                          ItemStack itemStack, PoseStack poseStack)
    {
        if (!(model instanceof HumanoidModel<?> humanoidModel))
        {
            return;
        }

        final float stab = stabProgress(entity, arm, itemStack);
        final boolean realSpear = BipedActionController.isRealSpear(itemStack.getItem());

        if (stab < 1.0F && !realSpear)
        {
            poseStack.rotateAround(Axis.XN.rotationDegrees(StabAction.itemPitch(stab)), 0.0F, -0.125F, 0.125F);
        }

        final boolean stabbing = stab < 1.0F && realSpear;
        final boolean jousting = getJoustingArm(entity, humanoidModel) == arm;

        if (!stabbing && !jousting)
        {
            return;
        }

        if (!initialized)
        {
            init();
        }

        if (useItemMethod == null)
        {
            return;
        }

        try
        {
            if (stabbing)
            {
                attackItemMethod.invoke(null, humanoidModel, entity, poseStack);
            }

            if (jousting)
            {
                useItemMethod.invoke(null, humanoidModel, entity, poseStack, (float) entity.getTicksUsingItem(), arm, itemStack);
            }
        }
        catch (Throwable e)
        {
            useItemMethod = null;
        }
    }
}
