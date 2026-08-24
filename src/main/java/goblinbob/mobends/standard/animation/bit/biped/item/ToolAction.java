package goblinbob.mobends.standard.animation.bit.biped.item;

import goblinbob.mobends.core.animation.bit.AnimationBit;
import goblinbob.mobends.core.client.model.ModelPartTransform;
import goblinbob.mobends.standard.data.BipedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;

public class ToolAction extends AnimationBit<BipedEntityData<?>>
{
    protected final HumanoidArm actionHand;

    public ToolAction(HumanoidArm actionHand)
    {
        this.actionHand = actionHand;
    }

    @Override
    public void perform(BipedEntityData<?> data)
    {
        LivingEntity entity = data.getEntity();
        if (!entity.swinging)
        {
            return;
        }

        final float headPitch = data.headPitch.get();
        final float headYaw = data.headYaw.get();

        final HumanoidArm activeHand = goblinbob.mobends.standard.animation.bit.biped.AttackArms
                .attackingArm(data, entity);

        boolean mainHandSwitch = activeHand == HumanoidArm.RIGHT;
        float sideMultiplier = activeHand == HumanoidArm.RIGHT ? 1.0F : -1.0F;
        ModelPartTransform mainArm = mainHandSwitch ? data.rightArm : data.leftArm;
        ModelPartTransform offArm = mainHandSwitch ? data.leftArm : data.rightArm;
        ModelPartTransform mainForeArm = mainHandSwitch ? data.rightForeArm : data.leftForeArm;
        ModelPartTransform offForeArm = mainHandSwitch ? data.leftForeArm : data.rightForeArm;

        data.localOffset.slideToZero(0.3F);
        data.centerRotation.setSmoothness(.3F).orientZero();

        float swingProgress = data.swingProgress.get();
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
