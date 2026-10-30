package goblinbob.mobends.standard.animation.bit.biped.item;

import goblinbob.mobends.core.animation.bit.AnimationBit;
import goblinbob.mobends.core.client.model.ModelPartTransform;
import goblinbob.mobends.standard.data.BipedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;

public class ToolAction extends AnimationBit<BipedEntityData<?>>
{
    private static final AnimationBit<BipedEntityData<?>> SWIM_SWING =
            new goblinbob.mobends.standard.animation.bit.biped.SwimSlashAnimationBit(-205.0F, -140.0F, -15.0F, 10.0F,
                    0.0F, goblinbob.mobends.standard.animation.bit.biped.SwimSlashAnimationBit.SWORD);

    @Override
    public void perform(BipedEntityData<?> data)
    {
        LivingEntity entity = data.getEntity();

        if (goblinbob.mobends.standard.animation.bit.biped.SwimmingAnimationBit.isSwimmingHorizontally(data))
        {
            if (data.getTicksAfterAnyAttack() < 10.0F)
            {
                SWIM_SWING.perform(data);
            }
            return;
        }

        final float swingProgress = entity.swinging ? data.swingProgress.get() : data.getExternalSwingProgress();
        if (!entity.swinging && swingProgress <= 0.0F)
        {
            return;
        }

        final float headPitch = data.headPitch.get();
        final float headYaw = data.headYaw.get();

        final HumanoidArm activeHand = goblinbob.mobends.standard.animation.bit.biped.AttackArms
                .attackingArm(data, entity);

        float sideMultiplier = activeHand == HumanoidArm.RIGHT ? 1.0F : -1.0F;
        final ModelPartTransform mainArm = activeHand == HumanoidArm.RIGHT ? data.rightArm : data.leftArm;

        data.localOffset.slideToZero(0.3F);
        data.centerRotation.setSmoothness(.3F).orientZero();

        final float bodyYaw = Mth.sin(Mth.sqrt(swingProgress) * ((float)Math.PI * 2F)) * 30.0F * sideMultiplier;
        data.body.rotation.setSmoothness(0.8F).orientY(bodyYaw);

        float bodyPitch = 0;
        if (data.getEntity().isCrouching())
        {
            data.body.rotation.rotateX(20.0F);
            bodyPitch = 20.0F;
        }

        data.head.rotation.setSmoothness(0.8F).orientX(headPitch - bodyPitch)
                .rotateY(headYaw - bodyYaw);

        mainArm.rotation.orientInstantX(Mth.sin(Mth.sqrt(swingProgress) * ((float)Math.PI * 2F)) * 50.0F - 30.0F);
        mainArm.rotation.localRotateZ(Mth.cos(Mth.sqrt(swingProgress) * ((float)Math.PI * 2F)) * -20.0F + 10.0F).finish();
    }
}
