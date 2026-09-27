package goblinbob.mobends.standard.animation.bit.player;

import goblinbob.mobends.core.animation.bit.AnimationBit;
import goblinbob.mobends.core.client.model.ModelPartTransform;
import goblinbob.mobends.standard.data.BipedEntityData;

public class ExternalPoseAnimationBit extends AnimationBit<BipedEntityData<?>>
{
    @Override
    public void perform(BipedEntityData<?> data)
    {
        data.globalOffset.set(0F, 0F, 0F);
        data.localOffset.set(0F, 0F, 0F);
        data.renderRotation.identity();
        data.centerRotation.identity();
        data.renderRightItemRotation.identity();
        data.renderLeftItemRotation.identity();

        neutralize(data);
    }

    private static void neutralize(BipedEntityData<?> data)
    {
        for (ModelPartTransform part : new ModelPartTransform[] {
                data.body, data.head, data.leftArm, data.rightArm, data.leftLeg, data.rightLeg,
                data.leftForeArm, data.rightForeArm, data.leftForeLeg, data.rightForeLeg })
        {
            part.globalOffset.set(0F, 0F, 0F);
            part.offset.set(0F, 0F, 0F);
            part.rotation.identity();
        }
    }
}
