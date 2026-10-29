package goblinbob.mobends.standard.animation.bit.biped;

import goblinbob.mobends.core.animation.bit.AnimationBit;
import goblinbob.mobends.core.client.model.ModelPartTransform;
import goblinbob.mobends.standard.data.BipedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;

public class SwimPunchAnimationBit extends AnimationBit<BipedEntityData<?>>
{
    private final HumanoidArm fistPunchArm;

    public SwimPunchAnimationBit(HumanoidArm fistPunchArm)
    {
        this.fistPunchArm = fistPunchArm;
    }

    @Override
    public void perform(BipedEntityData<?> data)
    {
        final boolean right = this.fistPunchArm == HumanoidArm.RIGHT;
        final float side = right ? 1.0F : -1.0F;
        final ModelPartTransform arm = right ? data.rightArm : data.leftArm;
        final ModelPartTransform foreArm = right ? data.rightForeArm : data.leftForeArm;

        arm.rotation.setSmoothness(.9F).orientX(-170.0F + Mth.clamp(data.headPitch.get(), -60.0F, 60.0F) * 0.5F)
                .rotateZ(-10.0F * side);
        foreArm.rotation.setSmoothness(.9F).orientX(0.0F);

        data.body.rotation.rotateY(-15.0F * side);
    }
}
