package goblinbob.mobends.lib.data;

public interface ILivingEntityAnimationData extends IEntityAnimationData
{


    float getLimbSwing();

    float getLimbSwingAmount();

    float getSwingProgress();

    float getHeadYaw();

    float getHeadPitch();


    float getTicksInAir();

    float getTicksAfterTouchdown();

    float getTicksAfterAttack();

    float getTicksFalling();


    boolean isClimbing();

    float getClimbingCycle();

    float getClimbingRotation();

    float getLedgeHeight();


    boolean isDrawingBow();

}
