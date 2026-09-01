package goblinbob.mobends.standard.animation.bit.biped;

import goblinbob.mobends.core.animation.bit.AnimationBit;
import goblinbob.mobends.core.client.event.DataUpdateHandler;
import goblinbob.mobends.core.client.model.ModelPartTransform;
import goblinbob.mobends.standard.data.BipedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;

public class TradeOfferAnimationBit extends AnimationBit<BipedEntityData<?>>
{

    private static final float OFFER_PITCH = -72.0F;
    private static final float PITCH_INFLUENCE = 0.35F;
    private static final float YAW_INFLUENCE = 0.5F;
    private static final float INWARD_ROLL = 16.0F;
    private static final float ELBOW_BEND = -22.0F;

    private static final float BRING_UP_SPEED = 0.2F;

    private static final float BOB_SPEED = 0.12F;
    private static final float BOB_AMOUNT = 3.5F;

    protected float bringUp;

    @Nullable
    public static HumanoidArm getOfferedArm(LivingEntity entity)
    {
        if (!entity.getMainHandItem().isEmpty())
        {
            return entity.getMainArm();
        }

        return null;
    }

    @Override
    public void onPlay(BipedEntityData<?> data)
    {
        bringUp = 0.0F;
    }

    @Override
    public void perform(BipedEntityData<?> data)
    {
        final HumanoidArm offerArm = getOfferedArm(data.getEntity());

        if (offerArm == null)
        {
            return;
        }

        if (bringUp < 1.0F)
        {
            bringUp += DataUpdateHandler.ticksPerFrame * BRING_UP_SPEED;
            bringUp = Math.min(bringUp, 1.0F);
        }

        final boolean rightHanded = offerArm == HumanoidArm.RIGHT;
        final float handDirection = rightHanded ? 1.0F : -1.0F;

        final ModelPartTransform arm = rightHanded ? data.rightArm : data.leftArm;
        final ModelPartTransform foreArm = rightHanded ? data.rightForeArm : data.leftForeArm;

        final float bob = Mth.cos(DataUpdateHandler.getTicks() * BOB_SPEED) * BOB_AMOUNT;
        final float pitch = OFFER_PITCH + data.headPitch.get() * PITCH_INFLUENCE + bob;

        arm.rotation.orientX(pitch * bringUp)
                .rotateY(data.headYaw.get() * YAW_INFLUENCE * bringUp)
                .rotateZ(INWARD_ROLL * handDirection * bringUp);

        foreArm.rotation.orientX(ELBOW_BEND * bringUp);
    }
}
