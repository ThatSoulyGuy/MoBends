package goblinbob.mobends.lib.data;

import goblinbob.mobends.lib.client.model.IBendsModel;
import goblinbob.mobends.lib.math.SmoothOrientation;
import goblinbob.mobends.lib.math.vector.SmoothVector3f;

/**
 * The core-side view of a mutated entity's animation state.
 *
 * <p>Extends {@link IBendsModel} so {@code Object getPartForName(String)} is inherited rather than
 * duplicated. Implemented in the mod by {@code goblinbob.mobends.core.data.EntityData}, which
 * remains the only implementor.
 *
 * <p>Deliberately not parameterised. The {@code EntityData<E extends Entity>} type parameter
 * existed only to type {@code getEntity()} — the single irreducible Minecraft leak — which is
 * replaced here by the flat probes {@link #isSprinting()}, {@link #isLiving()},
 * {@link #getHealth()} and {@link #getMaxHealth()}. Several implementations still use Minecraft
 * types in their bodies; only signatures have to cross the module boundary.
 */
public interface IEntityAnimationData extends IBendsModel
{

    // Root transform. These return the LIVE objects and callers mutate them in place, exactly as
    // the public fields were mutated before, so no setters are needed.
    //
    // The concrete types matter: SmoothVector3f does NOT implement IVec3f, and KeyframeUtils has
    // separate tweenVectorAdditive overloads for the two. Widening either return type here would
    // silently select a different overload and change the tween arithmetic.

    SmoothVector3f getGlobalOffset();

    SmoothOrientation getCenterRotation();

    // --- Position ---

    double getPositionX();

    double getPositionY();

    double getPositionZ();

    // --- Motion ---

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

    // --- Boolean state ---

    boolean isOnGround();

    boolean isStillHorizontally();

    boolean isStrafing();

    boolean isUnderwater();

    boolean isSprinting();

    // --- Vitals ---

    /**
     * Whether the backing entity is a living entity. Callers must consult this before dividing
     * {@link #getHealth()} by {@link #getMaxHealth()}: both return 0 for a non-living entity, and
     * an unguarded ratio would produce NaN where the previous instanceof-gated code produced 0.
     */
    boolean isLiving();

    /** Current health, or 0 when the backing entity is not living. */
    float getHealth();

    /** Maximum health, or 0 when the backing entity is not living. */
    float getMaxHealth();

}
