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

        animateLimbs(data, groundLevel, limbSwing);

        final float climbingRotation = data.getCrawlingRotation();
        final float yaw = spider.yRotO + (spider.getYRot() - spider.yRotO) * pt;
        final float renderRotationY = Mth.wrapDegrees(yaw - climbingRotation);
        data.renderRotation.orientX(-90F);
        data.renderRotation.setSmoothness(.6F).rotateY(renderRotationY);

        data.localOffset.slideTo(0, -10.0F, 0, 0.5F);
        data.centerRotation.orientZero();
    }

}
