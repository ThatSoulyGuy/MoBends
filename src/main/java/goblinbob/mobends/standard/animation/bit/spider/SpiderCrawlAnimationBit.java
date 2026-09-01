package goblinbob.mobends.standard.animation.bit.spider;

import goblinbob.mobends.core.client.event.DataUpdateHandler;
import goblinbob.mobends.standard.data.SpiderData;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.util.Mth;

public class SpiderCrawlAnimationBit extends SpiderAnimationBitBase
{


    @Override
    public void perform(SpiderData data)
    {
        final float pt = DataUpdateHandler.partialTicks;
        final Spider spider = data.getEntity();

        final float headYaw = data.headYaw.get();
        final float headPitch = data.headPitch.get();
        final float limbSwing = data.getInterpolatedCrawlProgress() * 5.0F;

        float groundLevel = Mth.sin(limbSwing * 0.6F) * 1.2F;

        if (startTransition < 1.0F)
            startTransition += DataUpdateHandler.ticksPerFrame * 0.1F;

        data.spiderHead.rotation.orientInstantX(headPitch);
        data.spiderHead.rotation.rotateY(headYaw).finish();

        animateMovingLimb(data, groundLevel, limbSwing + .0F, 0, 20.0F, 10F, -80, -50);
        animateMovingLimb(data, groundLevel, limbSwing + .3F, 1, 20.0F, 10F, -80, -50);

        animateMovingLimb(data, groundLevel, limbSwing + .3F, 2, 15F, 15.0F, -30F, 10.0F);
        animateMovingLimb(data, groundLevel, limbSwing + .0F, 3, 15F, 15.0F, -30F, 10.0F);

        animateMovingLimb(data, groundLevel, limbSwing + .4F, 4, 7F, 15.0F, 20, 50.0F);
        animateMovingLimb(data, groundLevel, limbSwing + .7F, 5, 7F, 15.0F, 20, 50.0F);

        animateMovingLimb(data, groundLevel, limbSwing + .7F, 6, 10F, 20.0F, 60, 80.0F);
        animateMovingLimb(data, groundLevel, limbSwing + .4F, 7, 10F, 20.0F, 60, 80.0F);

        final float climbingRotation = data.getCrawlingRotation();
        final float yaw = spider.yRotO + (spider.getYRot() - spider.yRotO) * pt;
        final float renderRotationY = Mth.wrapDegrees(yaw - climbingRotation);
        data.renderRotation.orientX(-90F);
        data.renderRotation.setSmoothness(.6F).rotateY(renderRotationY);

        data.localOffset.slideTo(0, -10.0F, 0, 0.5F);
        data.centerRotation.orientZero();
    }

}
