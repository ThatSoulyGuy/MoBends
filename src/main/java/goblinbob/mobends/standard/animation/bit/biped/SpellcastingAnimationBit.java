package goblinbob.mobends.standard.animation.bit.biped;

import goblinbob.mobends.core.animation.bit.AnimationBit;
import goblinbob.mobends.core.client.event.DataUpdateHandler;
import goblinbob.mobends.standard.data.BipedEntityData;
import net.minecraft.util.Mth;

public class SpellcastingAnimationBit extends AnimationBit<BipedEntityData<?>>
{
    private static final String[] ACTIONS = new String[] { "spellcasting" };

    private static final float TO_DEGREES = 180F / (float) Math.PI;

    private static final float ARM_RAISE = 2.3561945F;
    private static final float SMOOTHNESS = 0.3F;


    @Override
    public void perform(BipedEntityData<?> data)
    {
        final float age = DataUpdateHandler.getTicks();
        final float sway = Mth.cos(age * 0.6662F) * 0.25F * TO_DEGREES;

        data.rightArm.rotation.setSmoothness(SMOOTHNESS)
                .orientX(sway)
                .rotateZ(ARM_RAISE * TO_DEGREES);
        data.leftArm.rotation.setSmoothness(SMOOTHNESS)
                .orientX(sway)
                .rotateZ(-ARM_RAISE * TO_DEGREES);

        data.rightForeArm.rotation.setSmoothness(SMOOTHNESS).orientX(-8F);
        data.leftForeArm.rotation.setSmoothness(SMOOTHNESS).orientX(-8F);

        data.renderRightItemRotation.setSmoothness(SMOOTHNESS).orientZero();
        data.renderLeftItemRotation.setSmoothness(SMOOTHNESS).orientZero();
    }
}
