package goblinbob.mobends.lib.data;

/**
 * The core-side view of a living entity's animation state.
 *
 * <p>Implemented in the mod by {@code goblinbob.mobends.core.data.LivingEntityData}. The five
 * swing/head values are exposed as plain floats rather than as the {@code OverridableProperty}
 * fields that back them, so that the override mechanism stays an implementation detail on the
 * mod side.
 */
public interface ILivingEntityAnimationData extends IEntityAnimationData
{

    // --- Swing and head, read from the backing OverridableProperty fields ---

    float getLimbSwing();

    float getLimbSwingAmount();

    float getSwingProgress();

    float getHeadYaw();

    float getHeadPitch();

    // --- Elapsed-time counters ---

    float getTicksInAir();

    float getTicksAfterTouchdown();

    float getTicksAfterAttack();

    float getTicksFalling();

    // --- Climbing ---

    boolean isClimbing();

    float getClimbingCycle();

    float getClimbingRotation();

    float getLedgeHeight();

    // --- Item use ---

    boolean isDrawingBow();

}
