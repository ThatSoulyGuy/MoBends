package goblinbob.mobends.standard.animation.bit.biped;

import goblinbob.mobends.core.animation.bit.AnimationBit;
import goblinbob.mobends.core.client.model.ModelPartTransform;
import goblinbob.mobends.standard.data.BipedEntityData;
import net.minecraft.world.entity.HumanoidArm;

public class AxeStanceSprintAnimationBit extends AnimationBit<BipedEntityData<?>>
{
    @Override
    public void perform(BipedEntityData<?> data)
    {
        data.localOffset.slideToZero(0.3F);

        final boolean rightHanded = AttackArms.attackingArm(data, data.getEntity()) == HumanoidArm.RIGHT;
        final float side = rightHanded ? 1.0F : -1.0F;
        final ModelPartTransform mainArm = rightHanded ? data.rightArm : data.leftArm;
        final ModelPartTransform mainForeArm = rightHanded ? data.rightForeArm : data.leftForeArm;
        final ModelPartTransform offArm = rightHanded ? data.leftArm : data.rightArm;

        data.body.rotation.rotateY(20.0F * side);
        data.head.rotation.rotateY(-20.0F * side);

        mainArm.rotation.setSmoothness(.3F).orientX(30.0F).rotateZ(20.0F * side);
        mainForeArm.rotation.setSmoothness(.3F).orientX(0.0F);
        offArm.rotation.rotateZ(-30.0F * side);

        (rightHanded ? data.renderRightItemRotation : data.renderLeftItemRotation).setSmoothness(.3F).orientZero();
    }
}
