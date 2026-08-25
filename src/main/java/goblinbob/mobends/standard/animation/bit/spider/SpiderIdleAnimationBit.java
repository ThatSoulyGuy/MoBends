package goblinbob.mobends.standard.animation.bit.spider;

import goblinbob.mobends.core.animation.bit.AnimationBit;
import goblinbob.mobends.core.client.event.DataUpdateHandler;
import goblinbob.mobends.lib.util.GUtil;
import goblinbob.mobends.standard.data.SpiderData;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.util.Mth;

public class SpiderIdleAnimationBit extends AnimationBit<SpiderData>
{
	protected static final float KNEEL_DURATION = 10F;


	@Override
	public void onPlay(SpiderData data)
	{
		super.onPlay(data);
	}

	@Override
	public void perform(SpiderData data)
	{
		final float ticks = DataUpdateHandler.getTicks();
		final float pt = DataUpdateHandler.partialTicks;
		Spider spider = data.getEntity();

		final float headYaw = data.headYaw.get();
		final float headPitch = data.headPitch.get();

		double groundLevel = Math.sin(ticks * 0.1F) * 0.5;
		final float touchdown = Math.min(data.getTicksAfterTouchdown() / KNEEL_DURATION, 1.0F);

		if (touchdown < 1.0F)
		{
			final float preBounce = 0.0F;
			float touchdownInv = 1.0F - touchdown;
			groundLevel += Math.sin((touchdown * (1+preBounce) - preBounce) * Math.PI * 2) * 4.0F * touchdownInv;
		}

		data.spiderHead.rotation.orientInstantX(headPitch);
		data.spiderHead.rotation.rotateY(headYaw).finish();

    	final double bodyX = Math.sin(ticks * 0.2F) * 0.4;
    	final double bodyZ = Math.cos(ticks * 0.2F) * 0.4;

        for (SpiderData.Limb limb : data.limbs)
        {
        	SpiderData.IKResult ikResult = limb.solveIK(bodyX, bodyZ, pt);
        	double deviation = GUtil.getRadianDifference(limb.getNeutralYaw(), ikResult.xzAngle + Math.PI/2);

        	if (deviation > 0.9 || ikResult.xzDistance * 0.0625 > 1.2)
        	{
        		limb.adjustToNeutralPosition();
        	}

        	limb.applyIK(ikResult, groundLevel, 4, pt);
        }

		if (spider.tickCount % 100 < 10)
		{
			data.limbs[6].adjustToLocalPosition(0, 1.5, 0.2F);
			data.limbs[7].adjustToLocalPosition(0, 1.5, 0.2F);
		}

		float climbingRotation = 0;
		float renderRotationY = Mth.wrapDegrees(spider.getYRot() - data.headYaw.get() - climbingRotation);

		data.localOffset.slideToZero();
		data.globalOffset.set((float) bodyX, (float) -groundLevel, (float) -bodyZ);
		data.centerRotation.orientZero();
		data.renderRotation.orientZero();
	}
}
