package goblinbob.mobends.standard.animation.bit.biped;

import goblinbob.mobends.core.animation.bit.AnimationBit;
import goblinbob.mobends.core.client.event.DataUpdateHandler;
import goblinbob.mobends.standard.data.BipedEntityData;
import net.minecraft.util.Mth;

public class HumanoidSleepingAnimationBit<T extends BipedEntityData<?>> extends AnimationBit<T>
{
    @Override
    public void perform(T data)
    {
        StandAnimationBit.restPose(data);

        float phase = DataUpdateHandler.getTicks() / 10;
        data.head.rotation.setSmoothness(1.0F).orientX(((Mth.cos(phase) - 1) / 2) * -3);
        data.rightArm.rotation.setSmoothness(0.4F).orientX(0.0F)
                .rotateZ(2.5F);
        data.leftArm.rotation.setSmoothness(0.4F).orientX(0.0F)
                .rotateZ(-2.5F);
    }
}
