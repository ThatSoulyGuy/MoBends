package goblinbob.mobends.standard.animation.bit.spider;

import goblinbob.mobends.core.animation.bit.AnimationBit;
import goblinbob.mobends.lib.util.GUtil;
import goblinbob.mobends.standard.animation.controller.SpiderController;
import goblinbob.mobends.standard.data.SpiderData;
import net.minecraft.util.Mth;

public abstract class SpiderAnimationBitBase extends AnimationBit<SpiderData>
{

    protected float startTransition = 0.0F;

    protected void animateLimbs(SpiderData data, float groundLevel, float limbSwing)
    {
        animateMovingLimb(data, groundLevel, limbSwing + .0F, 0, 20.0F, 10F, -80, -50);
        animateMovingLimb(data, groundLevel, limbSwing + .3F, 1, 20.0F, 10F, -80, -50);

        animateMovingLimb(data, groundLevel, limbSwing + .3F, 2, 15F, 15.0F, -30F, 10.0F);
        animateMovingLimb(data, groundLevel, limbSwing + .0F, 3, 15F, 15.0F, -30F, 10.0F);

        animateMovingLimb(data, groundLevel, limbSwing + .4F, 4, 7F, 15.0F, 20, 50.0F);
        animateMovingLimb(data, groundLevel, limbSwing + .7F, 5, 7F, 15.0F, 20, 50.0F);

        animateMovingLimb(data, groundLevel, limbSwing + .7F, 6, 10F, 20.0F, 60, 80.0F);
        animateMovingLimb(data, groundLevel, limbSwing + .4F, 7, 10F, 20.0F, 60, 80.0F);
    }

    protected void animateMovingLimb(SpiderData data, float groundLevel, float limbSwing, int index, float minDist, float maxDist, float minRot, float maxRot)
    {
        final boolean odd = index % 2 == 1;
        final float offset = (index + 1) / 2 % 2 == 0 ? GUtil.PI : 0;
        float smoothness = 1F;
        float sideRotation = minRot + (Mth.sin(limbSwing + offset) * .5F + .5F) * (maxRot - minRot);
        float dist = minDist + (Mth.sin(limbSwing + offset) * .5F + .5F) * (maxDist - minDist);
        groundLevel += -7 + Math.max(0, Mth.cos(limbSwing + offset)) * 4;

        SpiderData.Limb limb = data.limbs[index];
        limb.upperPart.rotation.setSmoothness(smoothness).orientY(odd ? sideRotation : -sideRotation);

        if (startTransition >= 1.0F)
        {
            SpiderController.putLimbOnGround(limb.upperPart.rotation, limb.lowerPart.rotation, odd, dist, groundLevel);
        }
        else
        {
            SpiderController.putLimbOnGround(limb.upperPart.rotation, limb.lowerPart.rotation, odd, dist, groundLevel, startTransition);
        }

        limb.setAngleAndDistance(odd ? sideRotation / 180F * GUtil.PI : GUtil.PI - sideRotation / 180F * GUtil.PI, dist * 0.0625F);
    }

}
