package goblinbob.mobends.standard.animation.bit.player;

import goblinbob.mobends.compat.ZiplineCompat;
import goblinbob.mobends.core.animation.bit.AnimationBit;
import goblinbob.mobends.core.client.event.DataUpdateHandler;
import goblinbob.mobends.standard.data.BipedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;

public class ZiplineHangAnimationBit extends AnimationBit<BipedEntityData<?>>
{
    private static final float FREE_ARM_PITCH = 6.0F;
    private static final float FREE_ARM_SPREAD = 10.0F;
    private static final float FREE_ELBOW_BEND = 14.0F;
    private static final float LEG_DANGLE = 8.0F;
    private static final float KNEE_BEND = 15.0F;
    private static final float SWAY_SPAN = 4.0F;
    private static final float SWAY_SPEED = 0.09F;

    @Override
    public void perform(BipedEntityData<?> data)
    {
        final float sway = Mth.cos(DataUpdateHandler.getTicks() * SWAY_SPEED) * SWAY_SPAN;
        final HumanoidArm raisedArm = ZiplineCompat.getZipliningArm(data.getEntity());

        data.centerRotation.setSmoothness(0.7F).orientZero();
        data.renderRotation.setSmoothness(0.5F).orientX(0.0F);
        data.globalOffset.slideToZero(0.7F);

        data.body.rotation.setSmoothness(0.5F).orientX(0.0F);
        data.head.rotation.setSmoothness(1.0F).orientX(data.headPitch.get())
                .rotateY(data.headYaw.get());

        if (raisedArm != HumanoidArm.LEFT)
        {
            data.leftArm.rotation.setSmoothness(0.4F).orientX(FREE_ARM_PITCH + sway).rotateZ(-FREE_ARM_SPREAD);
            data.leftForeArm.rotation.setSmoothness(0.4F).orientX(-FREE_ELBOW_BEND);
        }
        if (raisedArm != HumanoidArm.RIGHT)
        {
            data.rightArm.rotation.setSmoothness(0.4F).orientX(FREE_ARM_PITCH - sway).rotateZ(FREE_ARM_SPREAD);
            data.rightForeArm.rotation.setSmoothness(0.4F).orientX(-FREE_ELBOW_BEND);
        }

        data.leftLeg.rotation.setSmoothness(0.4F).orientX(-LEG_DANGLE + sway).rotateZ(-3.0F);
        data.rightLeg.rotation.setSmoothness(0.4F).orientX(-LEG_DANGLE - sway).rotateZ(3.0F);
        data.leftForeLeg.rotation.setSmoothness(0.4F).orientX(KNEE_BEND - sway);
        data.rightForeLeg.rotation.setSmoothness(0.4F).orientX(KNEE_BEND + sway);
    }
}
