package goblinbob.mobends.standard.animation.bit.player;

import goblinbob.mobends.core.animation.bit.AnimationBit;
import goblinbob.mobends.standard.data.BipedEntityData;

public class EmoteAnimationBit extends AnimationBit<BipedEntityData<?>>
{
    private static final String[] ACTIONS = new String[] { "emote" };

    @Override
    public String[] getActions(BipedEntityData<?> entityData)
    {
        return ACTIONS;
    }

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
        data.body.globalOffset.set(0F, 0F, 0F);
        data.head.globalOffset.set(0F, 0F, 0F);
        data.leftArm.globalOffset.set(0F, 0F, 0F);
        data.rightArm.globalOffset.set(0F, 0F, 0F);
        data.leftLeg.globalOffset.set(0F, 0F, 0F);
        data.rightLeg.globalOffset.set(0F, 0F, 0F);
        data.leftForeArm.globalOffset.set(0F, 0F, 0F);
        data.rightForeArm.globalOffset.set(0F, 0F, 0F);
        data.leftForeLeg.globalOffset.set(0F, 0F, 0F);
        data.rightForeLeg.globalOffset.set(0F, 0F, 0F);

        data.body.offset.set(0F, 0F, 0F);
        data.head.offset.set(0F, 0F, 0F);
        data.leftArm.offset.set(0F, 0F, 0F);
        data.rightArm.offset.set(0F, 0F, 0F);
        data.leftLeg.offset.set(0F, 0F, 0F);
        data.rightLeg.offset.set(0F, 0F, 0F);
        data.leftForeArm.offset.set(0F, 0F, 0F);
        data.rightForeArm.offset.set(0F, 0F, 0F);
        data.leftForeLeg.offset.set(0F, 0F, 0F);
        data.rightForeLeg.offset.set(0F, 0F, 0F);

        data.body.rotation.identity();
        data.head.rotation.identity();
        data.leftArm.rotation.identity();
        data.rightArm.rotation.identity();
        data.leftLeg.rotation.identity();
        data.rightLeg.rotation.identity();
        data.leftForeArm.rotation.identity();
        data.rightForeArm.rotation.identity();
        data.leftForeLeg.rotation.identity();
        data.rightForeLeg.rotation.identity();
    }
}
