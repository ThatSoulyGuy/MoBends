package goblinbob.mobends.standard.animation.bit.biped;

import goblinbob.mobends.standard.data.BipedEntityData;

public class GroundSittingAnimationBit<T extends BipedEntityData<?>> extends SittingAnimationBit<T>
{
    private static final float GROUND_OFFSET = -10.0F;

    @Override
    public void perform(T data)
    {
        super.perform(data);

        data.globalOffset.slideY(GROUND_OFFSET, 0.3F);
    }
}
