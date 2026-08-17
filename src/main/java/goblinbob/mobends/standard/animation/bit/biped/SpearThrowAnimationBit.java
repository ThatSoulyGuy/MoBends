package goblinbob.mobends.standard.animation.bit.biped;

import goblinbob.mobends.core.animation.bit.AnimationBit;
import goblinbob.mobends.core.client.event.DataUpdateHandler;
import goblinbob.mobends.core.client.model.ModelPartTransform;
import goblinbob.mobends.standard.data.BipedEntityData;
import net.minecraft.world.entity.HumanoidArm;

public class SpearThrowAnimationBit extends AnimationBit<BipedEntityData<?>>
{
    private static final String[] ACTIONS = new String[] { "spear_throw" };

    private static final float THROW_PITCH = -180.0F;
    private static final float PITCH_INFLUENCE = 0.25F;
    private static final float ELBOW_BEND = 20.0F;
    private static final float WIND_UP_SPEED = 0.25F;

    protected final HumanoidArm actionHand;

    protected float windUp;

    public SpearThrowAnimationBit(HumanoidArm handSide)
    {
        this.actionHand = handSide;
    }

    @Override
    public String[] getActions(BipedEntityData<?> data)
    {
        return ACTIONS;
    }

    @Override
    public void onPlay(BipedEntityData<?> data)
    {
        windUp = 0F;
    }

    @Override
    public void perform(BipedEntityData<?> data)
    {
        final boolean rightHanded = this.actionHand == HumanoidArm.RIGHT;
        final ModelPartTransform mainArm = rightHanded ? data.rightArm : data.leftArm;
        final ModelPartTransform mainForeArm = rightHanded ? data.rightForeArm : data.leftForeArm;

        if (windUp < 1F)
        {
            windUp += DataUpdateHandler.ticksPerFrame * WIND_UP_SPEED;
            windUp = Math.min(windUp, 1F);
        }

        final float armPitch = THROW_PITCH + data.headPitch.get() * PITCH_INFLUENCE;

        mainArm.rotation.orientX((armPitch + ELBOW_BEND) * windUp);
        mainForeArm.rotation.orientX(-ELBOW_BEND * windUp);
    }
}
