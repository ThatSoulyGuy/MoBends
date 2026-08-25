package goblinbob.mobends.standard.animation.bit.biped;

import goblinbob.mobends.core.animation.bit.AnimationBit;
import goblinbob.mobends.core.client.event.DataUpdateHandler;
import goblinbob.mobends.core.client.model.ModelPartTransform;
import goblinbob.mobends.standard.data.BipedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;

public class SpyglassAnimationBit extends AnimationBit<BipedEntityData<?>>
{
    private static final String[] ACTIONS = new String[] { "spyglass" };

    private static final float ARM_PITCH_OFFSET = 110.0F;
    private static final float ARM_YAW_OFFSET = 15.0F;
    private static final float CROUCH_PITCH_OFFSET = 15.0F;
    private static final float ARM_PITCH_MIN = -137.5F;
    private static final float ARM_PITCH_MAX = 189.0F;
    private static final float BRING_UP_SPEED = 0.35F;

    protected final HumanoidArm actionHand;

    protected float bringUpAnimation;

    public SpyglassAnimationBit(HumanoidArm handSide)
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
        final LivingEntity living = data.getEntity();
        final boolean rightHanded = this.actionHand == HumanoidArm.RIGHT;
        final float handDirMtp = rightHanded ? 1F : -1F;
        final ModelPartTransform mainArm = rightHanded ? data.rightArm : data.leftArm;
        final ModelPartTransform mainForeArm = rightHanded ? data.rightForeArm : data.leftForeArm;

        if (bringUpAnimation < 1F)
        {
            bringUpAnimation += DataUpdateHandler.ticksPerFrame * BRING_UP_SPEED;
            bringUpAnimation = Math.min(bringUpAnimation, 1F);
        }

        final float crouchOffset = living != null && living.isCrouching() ? CROUCH_PITCH_OFFSET : 0F;
        final float armPitch = Mth.clamp(
                data.headPitch.get() - ARM_PITCH_OFFSET - crouchOffset,
                ARM_PITCH_MIN, ARM_PITCH_MAX);
        final float armYaw = data.headYaw.get() - ARM_YAW_OFFSET * handDirMtp;

        mainArm.rotation.orientX(armPitch * bringUpAnimation)
                        .rotateY(armYaw * bringUpAnimation);
        mainForeArm.rotation.setSmoothness(1F).orientX(0F);
    }
}
