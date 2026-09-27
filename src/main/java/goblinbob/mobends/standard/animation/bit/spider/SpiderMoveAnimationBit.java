package goblinbob.mobends.standard.animation.bit.spider;

import goblinbob.mobends.core.client.event.DataUpdateHandler;
import goblinbob.mobends.standard.data.SpiderData;
import net.minecraft.util.Mth;

public class SpiderMoveAnimationBit extends SpiderAnimationBitBase
{

    protected static final float KNEEL_DURATION = 10F;


    @Override
    public void perform(SpiderData data)
    {
        final float ticks = DataUpdateHandler.getTicks();
        final float headYaw = data.headYaw.get();
        final float headPitch = data.headPitch.get();
        final float limbSwing = data.limbSwing.get() * 0.6662F;

        float groundLevel = Mth.sin(ticks * 0.6F) * 1.2F;
        final float touchdown = Math.min(data.getTicksAfterTouchdown() / KNEEL_DURATION, 1.0F);

        if (startTransition < 1.0F)
            startTransition += DataUpdateHandler.ticksPerFrame * 0.1F;

        if (touchdown < 1.0F)
        {
            float touchdownInv = 1.0F - touchdown;
            groundLevel += Math.sin((touchdown * 1.2F - 0.2F) * Math.PI * 2) * 3.0F * touchdownInv;
        }

        data.spiderHead.rotation.orientInstantX(headPitch);
        data.spiderHead.rotation.rotateY(headYaw).finish();

        final float bodyX = Mth.sin(ticks * 0.2F) * 0.4F;
        final float bodyZ = Mth.cos(ticks * 0.2F) * 0.4F;

        animateLimbs(data, groundLevel, limbSwing);

        data.localOffset.slideToZero();
        data.globalOffset.set(bodyX, -groundLevel, -bodyZ);
        data.renderRotation.orientZero();
        data.centerRotation.orientZero();
    }

}
