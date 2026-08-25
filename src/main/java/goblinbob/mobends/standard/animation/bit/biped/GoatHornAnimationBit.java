package goblinbob.mobends.standard.animation.bit.biped;

import goblinbob.mobends.core.animation.bit.AnimationBit;
import goblinbob.mobends.core.client.event.DataUpdateHandler;
import goblinbob.mobends.core.client.model.ModelPartTransform;
import goblinbob.mobends.standard.data.BipedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;

public class GoatHornAnimationBit extends AnimationBit<BipedEntityData<?>>
{
    private static final String[] ACTIONS = new String[] { "goat_horn" };

    private static final float ARM_PITCH_OFFSET = 85.0F;
    private static final float ARM_YAW_OFFSET = 30.0F;
    private static final float HEAD_PITCH_LIMIT = 68.75F;
    private static final float ELBOW_BEND = 15.0F;
    private static final float BRING_UP_SPEED = 0.3F;

    protected final HumanoidArm actionHand;

    protected float bringUpAnimation;

    public GoatHornAnimationBit(HumanoidArm handSide)
    {
        this.actionHand = handSide;
    }


    @Override
    public void onPlay(BipedEntityData<?> data)
    {
        bringUpAnimation = 0F;
    }

    @Override
    public void perform(BipedEntityData<?> data)
    {
        final boolean rightHanded = this.actionHand == HumanoidArm.RIGHT;
        final float handDirMtp = rightHanded ? 1F : -1F;
        final ModelPartTransform mainArm = rightHanded ? data.rightArm : data.leftArm;
        final ModelPartTransform mainForeArm = rightHanded ? data.rightForeArm : data.leftForeArm;

        if (bringUpAnimation < 1F)
        {
            bringUpAnimation += DataUpdateHandler.ticksPerFrame * BRING_UP_SPEED;
            bringUpAnimation = Math.min(bringUpAnimation, 1F);
        }

        final float armPitch = Mth.clamp(data.headPitch.get(), -HEAD_PITCH_LIMIT, HEAD_PITCH_LIMIT) - ARM_PITCH_OFFSET;
        final float armYaw = data.headYaw.get() - ARM_YAW_OFFSET * handDirMtp;

        mainArm.rotation.orientX((armPitch + ELBOW_BEND) * bringUpAnimation)
                        .rotateY(armYaw * bringUpAnimation);
        mainForeArm.rotation.orientX(-ELBOW_BEND * bringUpAnimation);
    }
}
