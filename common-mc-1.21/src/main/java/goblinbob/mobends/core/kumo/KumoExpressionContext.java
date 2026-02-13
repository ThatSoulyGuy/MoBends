package goblinbob.mobends.core.kumo;

import goblinbob.mobends.core.expression.ExpressionContext;

import goblinbob.mobends.core.client.event.DataUpdateHandler;
import goblinbob.mobends.core.data.EntityData;
import goblinbob.mobends.core.data.LivingEntityData;
import goblinbob.mobends.core.kumo.state.condition.ITriggerConditionContext;
import net.minecraft.world.entity.LivingEntity;

/**
 * Expression context that bridges to entity data for Kumo animations.
 * Provides access to entity state, motion, and animation variables.
 */
public class KumoExpressionContext implements ExpressionContext {
    private final ITriggerConditionContext kumoContext;
    private final EntityData<?> entityData;

    public KumoExpressionContext(ITriggerConditionContext kumoContext) {
        this.kumoContext = kumoContext;
        this.entityData = kumoContext.getEntityData();
    }

    public KumoExpressionContext(EntityData<?> entityData) {
        this.kumoContext = null;
        this.entityData = entityData;
    }

    @Override
    public double getVariable(String name) {
        return switch (name) {
            // Time
            case "ticks" -> DataUpdateHandler.getTicks();

            // Constants
            case "PI" -> Math.PI;
            case "E" -> Math.E;

            // Random (non-deterministic)
            case "random" -> Math.random();

            // Motion (raw)
            case "motionX" -> entityData.getMotionX();
            case "motionY" -> entityData.getMotionY();
            case "motionZ" -> entityData.getMotionZ();

            // Motion (interpolated)
            case "interpMotionX" -> entityData.getInterpolatedMotionX();
            case "interpMotionY" -> entityData.getInterpolatedMotionY();
            case "interpMotionZ" -> entityData.getInterpolatedMotionZ();

            // Motion magnitude
            case "motionMagnitude" -> entityData.getMotionMagnitude();
            case "xzMotionMagnitude" -> entityData.getXZMotionMagnitude();
            case "interpMotionMagnitude" -> entityData.getInterpolatedMotionMagnitude();
            case "interpXZMotionMagnitude" -> entityData.getInterpolatedXZMotionMagnitude();

            // Relative motion
            case "forwardMomentum" -> entityData.getForwardMomentum();
            case "sidewaysMomentum" -> entityData.getSidewaysMomentum();
            case "movementAngle" -> entityData.getMovementAngle();
            case "lookAngle" -> entityData.getLookAngle();

            // Ground state
            case "onGround" -> entityData.isOnGround() ? 1.0 : 0.0;
            case "isStill" -> entityData.isStillHorizontally() ? 1.0 : 0.0;
            case "isStrafing" -> entityData.isStrafing() ? 1.0 : 0.0;
            case "isUnderwater" -> entityData.isUnderwater() ? 1.0 : 0.0;

            // Living entity specific
            case "health" -> getLivingValue(e -> (double) e.getHealth(), 0.0);
            case "maxHealth" -> getLivingValue(e -> (double) e.getMaxHealth(), 0.0);
            case "healthPercent" -> getLivingValue(e -> (double) (e.getHealth() / e.getMaxHealth()), 0.0);

            // Living entity data specific
            case "ticksInAir" -> getLivingDataValue(d -> (double) d.getTicksInAir(), 0.0);
            case "ticksAfterTouchdown" -> getLivingDataValue(d -> (double) d.getTicksAfterTouchdown(), 0.0);
            case "ticksAfterPunch", "ticksAfterAttack" -> getLivingDataValue(d -> (double) d.getTicksAfterAttack(), 0.0);
            case "ticksFalling" -> getLivingDataValue(d -> (double) d.getTicksFalling(), 0.0);

            // Limb swing
            case "limbSwing" -> getLivingDataValue(d -> (double) d.limbSwing.get(), 0.0);
            case "limbSwingAmount" -> getLivingDataValue(d -> (double) d.limbSwingAmount.get(), 0.0);

            // Head rotation
            case "headYaw" -> getLivingDataValue(d -> (double) d.headYaw.get(), 0.0);
            case "headPitch" -> getLivingDataValue(d -> (double) d.headPitch.get(), 0.0);

            // Arm swing
            case "swingProgress" -> getLivingDataValue(d -> (double) d.swingProgress.get(), 0.0);

            // Climbing
            case "isClimbing" -> getLivingDataValue(d -> d.isClimbing() ? 1.0 : 0.0, 0.0);
            case "climbingCycle" -> getLivingDataValue(d -> (double) d.getClimbingCycle(), 0.0);
            case "climbingRotation" -> getLivingDataValue(d -> (double) d.getClimbingRotation(), 0.0);
            case "ledgeHeight" -> getLivingDataValue(d -> (double) d.getLedgeHeight(), 0.0);

            // Drawing bow
            case "isDrawingBow" -> getLivingDataValue(d -> d.isDrawingBow() ? 1.0 : 0.0, 0.0);

            // Node state (if available)
            case "nodeProgress" -> getNodeProgress();
            case "nodeAnimationFinished" -> isNodeAnimationFinished() ? 1.0 : 0.0;

            // Position
            case "posX" -> entityData.getPositionX();
            case "posY" -> entityData.getPositionY();
            case "posZ" -> entityData.getPositionZ();

            default -> 0.0;
        };
    }

    @Override
    public boolean hasVariable(String name) {
        return switch (name) {
            case "ticks", "PI", "E", "random",
                 "motionX", "motionY", "motionZ",
                 "interpMotionX", "interpMotionY", "interpMotionZ",
                 "motionMagnitude", "xzMotionMagnitude", "interpMotionMagnitude", "interpXZMotionMagnitude",
                 "forwardMomentum", "sidewaysMomentum", "movementAngle", "lookAngle",
                 "onGround", "isStill", "isStrafing", "isUnderwater",
                 "health", "maxHealth", "healthPercent",
                 "ticksInAir", "ticksAfterTouchdown", "ticksAfterPunch", "ticksAfterAttack", "ticksFalling",
                 "limbSwing", "limbSwingAmount", "headYaw", "headPitch", "swingProgress",
                 "isClimbing", "climbingCycle", "climbingRotation", "ledgeHeight",
                 "isDrawingBow",
                 "nodeProgress", "nodeAnimationFinished",
                 "posX", "posY", "posZ" -> true;
            default -> false;
        };
    }

    private double getLivingValue(java.util.function.Function<LivingEntity, Double> getter, double defaultValue) {
        if (entityData.getEntity() instanceof LivingEntity living) {
            return getter.apply(living);
        }
        return defaultValue;
    }

    private double getLivingDataValue(java.util.function.Function<LivingEntityData<?>, Double> getter, double defaultValue) {
        if (entityData instanceof LivingEntityData<?> livingData) {
            return getter.apply(livingData);
        }
        return defaultValue;
    }

    private double getNodeProgress() {
        if (kumoContext != null && kumoContext.getCurrentNode() != null) {
            return kumoContext.getCurrentNode().getProgress();
        }
        return 0.0;
    }

    private boolean isNodeAnimationFinished() {
        if (kumoContext != null && kumoContext.getCurrentNode() != null) {
            return kumoContext.getCurrentNode().isAnimationFinished();
        }
        return false;
    }
}
