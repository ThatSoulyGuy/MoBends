package goblinbob.mobends.standard.animation.bit.biped;

import goblinbob.mobends.core.animation.bit.AnimationBit;
import goblinbob.mobends.core.client.model.IModelPart;
import goblinbob.mobends.standard.data.BipedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import goblinbob.mobends.core.util.HeldItemHelper;
import goblinbob.mobends.platform.armor.ArmorModelProviderHolder;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;

import javax.annotation.Nullable;

public class CrossbowHoldAnimationBit extends AnimationBit<BipedEntityData<?>>
{

    private static final float TO_DEGREES = 180F / (float) Math.PI;

    private static final float HOLDING_PITCH = (-1.5707964F + 0.1F) * TO_DEGREES;
    private static final float SUPPORT_PITCH = -1.5F * TO_DEGREES;
    private static final float HOLDING_YAW = 0.3F * TO_DEGREES;
    private static final float SUPPORT_YAW = 0.6F * TO_DEGREES;

    private static final float SMOOTHNESS = 0.4F;

    @Nullable
    public static HumanoidArm getChargedCrossbowArm(LivingEntity entity)
    {
        final HumanoidArm mainArm = entity.getMainArm();
        final HumanoidArm offArm = mainArm.getOpposite();

        if (isReadyCrossbow(entity, entity.getMainHandItem(), InteractionHand.MAIN_HAND))
        {
            return mainArm;
        }

        if (isReadyCrossbow(entity, entity.getOffhandItem(), InteractionHand.OFF_HAND))
        {
            return offArm;
        }

        return null;
    }

    private static boolean isReadyCrossbow(LivingEntity entity, ItemStack itemStack, InteractionHand hand)
    {
        if (entity.getUsedItemHand() == hand && entity.getUseItemRemainingTicks() > 0
                && itemStack.getUseAnimation() != UseAnim.NONE)
        {
            return false;
        }

        if (HeldItemHelper.isLoadedCrossbow(itemStack))
        {
            return !entity.swinging;
        }

        return entity instanceof Player
                && ArmorModelProviderHolder.getProvider().getArmPose(entity, hand, itemStack) == HumanoidModel.ArmPose.CROSSBOW_HOLD;
    }


    @Override
    public void perform(BipedEntityData<?> data)
    {
        final LivingEntity entity = data.getEntity();

        final HumanoidArm holdingSide = getChargedCrossbowArm(entity);
        if (holdingSide == null)
        {
            return;
        }

        final boolean rightHanded = holdingSide == HumanoidArm.RIGHT;

        final float headYaw = data.headYaw.get();
        final float headPitch = data.headPitch.get();

        final IModelPart holdingArm = rightHanded ? data.rightArm : data.leftArm;
        final IModelPart supportArm = rightHanded ? data.leftArm : data.rightArm;
        final IModelPart holdingForeArm = rightHanded ? data.rightForeArm : data.leftForeArm;
        final IModelPart supportForeArm = rightHanded ? data.leftForeArm : data.rightForeArm;

        if (SwimmingAnimationBit.isSwimmingHorizontally(data))
        {
            final float side = rightHanded ? 1F : -1F;
            final float pitch = Mth.clamp(headPitch, -80F, 80F);
            final float yaw = Mth.clamp(headYaw, -60F, 60F);

            holdingArm.getRotation().setSmoothness(SMOOTHNESS)
                    .orientX(-172F + pitch)
                    .rotateZ(-yaw + HOLDING_YAW * side);
            supportArm.getRotation().setSmoothness(SMOOTHNESS)
                    .orientX(-165F + pitch)
                    .rotateZ(-yaw - SUPPORT_YAW * side);

            holdingForeArm.getRotation().setSmoothness(SMOOTHNESS).orientX(-5F);
            supportForeArm.getRotation().setSmoothness(SMOOTHNESS).orientX(-20F);

            data.renderRightItemRotation.set(0F, 0F, 0F, 1F);
            data.renderLeftItemRotation.set(0F, 0F, 0F, 1F);
            return;
        }

        holdingArm.getRotation().setSmoothness(SMOOTHNESS)
                .orientX(HOLDING_PITCH + headPitch)
                .rotateY((rightHanded ? -HOLDING_YAW : HOLDING_YAW) + headYaw);
        supportArm.getRotation().setSmoothness(SMOOTHNESS)
                .orientX(SUPPORT_PITCH + headPitch)
                .rotateY((rightHanded ? SUPPORT_YAW : -SUPPORT_YAW) + headYaw);

        holdingForeArm.getRotation().setSmoothness(SMOOTHNESS).orientX(-5F);
        supportForeArm.getRotation().setSmoothness(SMOOTHNESS).orientX(-20F);

        data.renderRightItemRotation.setSmoothness(SMOOTHNESS).orientZero();
        data.renderLeftItemRotation.setSmoothness(SMOOTHNESS).orientZero();
    }
}
