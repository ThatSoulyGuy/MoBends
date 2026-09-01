package goblinbob.mobends.standard.animation.bit.biped;

import goblinbob.mobends.core.animation.bit.AnimationBit;
import goblinbob.mobends.core.client.event.DataUpdateHandler;
import goblinbob.mobends.core.client.model.ModelPartTransform;
import goblinbob.mobends.standard.data.BipedEntityData;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;

import javax.annotation.Nullable;

public class SpearThrowAnimationBit extends AnimationBit<BipedEntityData<?>>
{

    private static final float THROW_PITCH = -180.0F;
    private static final float PITCH_INFLUENCE = 0.25F;
    private static final float ELBOW_BEND = 20.0F;
    private static final float WIND_UP_SPEED = 0.25F;

    @Nullable
    protected final HumanoidArm actionHand;

    protected float windUp;

    public SpearThrowAnimationBit()
    {
        this(null);
    }

    public SpearThrowAnimationBit(@Nullable HumanoidArm handSide)
    {
        this.actionHand = handSide;
    }

    @Nullable
    public static HumanoidArm getRaisedSpearArm(LivingEntity entity)
    {
        if (entity.swinging || !(entity instanceof Mob mob) || !mob.isAggressive())
        {
            return null;
        }

        final HumanoidArm mainArm = entity.getMainArm();
        final HumanoidArm offArm = mainArm == HumanoidArm.RIGHT ? HumanoidArm.LEFT : HumanoidArm.RIGHT;

        if (isSpear(entity.getMainHandItem()))
        {
            return mainArm;
        }

        if (isSpear(entity.getOffhandItem()))
        {
            return offArm;
        }

        return null;
    }

    private static boolean isSpear(ItemStack itemStack)
    {
        return !itemStack.isEmpty() && itemStack.getUseAnimation() == UseAnim.SPEAR;
    }


    @Override
    public void onPlay(BipedEntityData<?> data)
    {
        windUp = 0F;
    }

    @Override
    public void perform(BipedEntityData<?> data)
    {
        final HumanoidArm handSide = this.actionHand != null
                ? this.actionHand
                : getRaisedSpearArm(data.getEntity());

        if (handSide == null)
        {
            return;
        }

        final boolean rightHanded = handSide == HumanoidArm.RIGHT;
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
