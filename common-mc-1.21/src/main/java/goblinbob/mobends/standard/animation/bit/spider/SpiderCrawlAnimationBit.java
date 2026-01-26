package goblinbob.mobends.standard.animation.bit.spider;

import goblinbob.mobends.core.client.event.DataUpdateHandler;
import goblinbob.mobends.standard.data.SpiderData;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.util.Mth;

public class SpiderCrawlAnimationBit extends SpiderAnimationBitBase
{

    protected static final String[] ACTIONS = new String[] { "crawl" };

    @Override
    public String[] getActions(SpiderData entityData)
    {
        return ACTIONS;
    }

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

        // Back limbs
        animateMovingLimb(data, groundLevel, limbSwing + .0F, 0, 20.0F, 10F, -80, -50);
        animateMovingLimb(data, groundLevel, limbSwing + .3F, 1, 20.0F, 10F, -80, -50);

        // Back-middle limbs
        animateMovingLimb(data, groundLevel, limbSwing + .3F, 2, 15F, 15.0F, -30F, 10.0F);
        animateMovingLimb(data, groundLevel, limbSwing + .0F, 3, 15F, 15.0F, -30F, 10.0F);

        // Front-middle limbs
        animateMovingLimb(data, groundLevel, limbSwing + .4F, 4, 7F, 15.0F, 20, 50.0F);
        animateMovingLimb(data, groundLevel, limbSwing + .7F, 5, 7F, 15.0F, 20, 50.0F);

        // Front limbs
        animateMovingLimb(data, groundLevel, limbSwing + .7F, 6, 10F, 20.0F, 60, 80.0F);
        animateMovingLimb(data, groundLevel, limbSwing + .4F, 7, 10F, 20.0F, 60, 80.0F);

        final float climbingRotation = data.getCrawlingRotation();
        // Use body yaw, not entity yaw - Minecraft's renderer applies body yaw rotation,
        // so we need to compensate based on body yaw to avoid mismatch when spider looks around
        final float bodyYaw = spider.yBodyRotO + (spider.yBodyRot - spider.yBodyRotO) * pt;
        final float renderRotationY = Mth.wrapDegrees(bodyYaw - climbingRotation);
        data.renderRotation.orientX(-90F);
        data.renderRotation.setSmoothness(.6F).rotateY(renderRotationY);

        // Reset local offset - position handled by the rotation transforms
        data.localOffset.slideToZero();
        data.centerRotation.orientZero();
    }

}
