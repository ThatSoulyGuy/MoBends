package goblinbob.mobends.lib.data;

import goblinbob.mobends.lib.client.model.IBendsModel;
import goblinbob.mobends.lib.math.SmoothOrientation;
import goblinbob.mobends.lib.math.vector.SmoothVector3f;

public interface IEntityAnimationData extends IBendsModel
{


    SmoothVector3f getGlobalOffset();

    SmoothOrientation getCenterRotation();


    double getPositionX();

    double getPositionY();

    double getPositionZ();


    double getMotionX();

    double getMotionY();

    double getMotionZ();

    double getInterpolatedMotionX();

    double getInterpolatedMotionY();

    double getInterpolatedMotionZ();

    double getMotionMagnitude();

    double getXZMotionMagnitude();

    double getInterpolatedMotionMagnitude();

    double getInterpolatedXZMotionMagnitude();

    double getForwardMomentum();

    double getSidewaysMomentum();

    float getMovementAngle();

    float getLookAngle();


    boolean isOnGround();

    boolean isStillHorizontally();

    boolean isStrafing();

    boolean isUnderwater();

    boolean isSprinting();


    boolean isLiving();

    float getHealth();

    float getMaxHealth();

}
