package goblinbob.mobends.standard.animation.bit.biped;

import goblinbob.mobends.core.animation.bit.AnimationBit;
import goblinbob.mobends.core.client.event.DataUpdateHandler;
import goblinbob.mobends.core.client.model.ModelPartTransform;
import goblinbob.mobends.standard.data.BipedEntityData;
import net.minecraft.world.entity.HumanoidArm;

public class SwordBlockAnimationBit extends AnimationBit<BipedEntityData<?>>
{
    private static final float ARM_RAISE = -55.0F;
    private static final float ARM_INWARD = -15.0F;

    protected final HumanoidArm actionHand;

    protected float bringUpAnimation;

    public SwordBlockAnimationBit(HumanoidArm handSide)
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
        final boolean mainHandSwitch = this.actionHand == HumanoidArm.RIGHT;
        final float handDirMtp = mainHandSwitch ? 1 : -1;
        final ModelPartTransform mainArm = mainHandSwitch ? data.rightArm : data.leftArm;
        final ModelPartTransform mainForeArm = mainHandSwitch ? data.rightForeArm : data.leftForeArm;

        bringUpAnimation = Math.min(bringUpAnimation + DataUpdateHandler.ticksPerFrame * 0.7F, 1F);

        mainArm.rotation.setSmoothness(0.5F).orientX(ARM_RAISE * bringUpAnimation)
                .rotateY(ARM_INWARD * bringUpAnimation * handDirMtp);

        mainForeArm.rotation.setSmoothness(0.5F).orientX(0.0F);
    }
}
