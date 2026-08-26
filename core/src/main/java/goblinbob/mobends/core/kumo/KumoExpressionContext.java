package goblinbob.mobends.core.kumo;

import goblinbob.mobends.core.expression.ExpressionContext;

import goblinbob.mobends.lib.time.ITickSource;
import goblinbob.mobends.lib.data.IEntityAnimationData;
import goblinbob.mobends.lib.data.ILivingEntityAnimationData;
import goblinbob.mobends.core.kumo.state.condition.ITriggerConditionContext;

public class KumoExpressionContext implements ExpressionContext {
    private final ITriggerConditionContext kumoContext;
    private final IEntityAnimationData entityData;

    public KumoExpressionContext(ITriggerConditionContext kumoContext) {
        this.kumoContext = kumoContext;
        this.entityData = kumoContext.getEntityData();
    }

    public KumoExpressionContext(IEntityAnimationData entityData) {
        this.kumoContext = null;
        this.entityData = entityData;
    }

    @Override
    public double getVariable(String name) {
        return switch (name) {
            case "ticks" -> ITickSource.Holder.getTicks();

            case "PI" -> Math.PI;
            case "E" -> Math.E;

            case "random" -> Math.random();

            case "motionX" -> entityData.getMotionX();
            case "motionY" -> entityData.getMotionY();
            case "motionZ" -> entityData.getMotionZ();

            case "interpMotionX" -> entityData.getInterpolatedMotionX();
            case "interpMotionY" -> entityData.getInterpolatedMotionY();
            case "interpMotionZ" -> entityData.getInterpolatedMotionZ();

            case "motionMagnitude" -> entityData.getMotionMagnitude();
            case "xzMotionMagnitude" -> entityData.getXZMotionMagnitude();
            case "interpMotionMagnitude" -> entityData.getInterpolatedMotionMagnitude();
            case "interpXZMotionMagnitude" -> entityData.getInterpolatedXZMotionMagnitude();

            case "forwardMomentum" -> entityData.getForwardMomentum();
            case "sidewaysMomentum" -> entityData.getSidewaysMomentum();
            case "movementAngle" -> entityData.getMovementAngle();
            case "lookAngle" -> entityData.getLookAngle();

            case "onGround" -> entityData.isOnGround() ? 1.0 : 0.0;
            case "isStill" -> entityData.isStillHorizontally() ? 1.0 : 0.0;
            case "isStrafing" -> entityData.isStrafing() ? 1.0 : 0.0;
            case "isUnderwater" -> entityData.isUnderwater() ? 1.0 : 0.0;

            case "health" -> entityData.isLiving() ? entityData.getHealth() : 0.0;
            case "maxHealth" -> entityData.isLiving() ? entityData.getMaxHealth() : 0.0;
            case "healthPercent" -> entityData.isLiving() && entityData.getMaxHealth() > 0F
                    ? entityData.getHealth() / entityData.getMaxHealth()
                    : 0.0;

            case "ticksInAir" -> getLivingDataValue(d -> (double) d.getTicksInAir(), 0.0);
            case "ticksAfterTouchdown" -> getLivingDataValue(d -> (double) d.getTicksAfterTouchdown(), 0.0);
            case "ticksAfterPunch", "ticksAfterAttack" -> getLivingDataValue(d -> (double) d.getTicksAfterAttack(), 0.0);
            case "ticksFalling" -> getLivingDataValue(d -> (double) d.getTicksFalling(), 0.0);

            case "limbSwing" -> getLivingDataValue(d -> (double) d.getLimbSwing(), 0.0);
            case "limbSwingAmount" -> getLivingDataValue(d -> (double) d.getLimbSwingAmount(), 0.0);

            case "headYaw" -> getLivingDataValue(d -> (double) d.getHeadYaw(), 0.0);
            case "headPitch" -> getLivingDataValue(d -> (double) d.getHeadPitch(), 0.0);

            case "swingProgress" -> getLivingDataValue(d -> (double) d.getSwingProgress(), 0.0);

            case "isClimbing" -> getLivingDataValue(d -> d.isClimbing() ? 1.0 : 0.0, 0.0);
            case "climbingCycle" -> getLivingDataValue(d -> (double) d.getClimbingCycle(), 0.0);
            case "climbingRotation" -> getLivingDataValue(d -> (double) d.getClimbingRotation(), 0.0);
            case "ledgeHeight" -> getLivingDataValue(d -> (double) d.getLedgeHeight(), 0.0);

            case "isDrawingBow" -> getLivingDataValue(d -> d.isDrawingBow() ? 1.0 : 0.0, 0.0);

            case "nodeProgress" -> getNodeProgress();
            case "nodeAnimationFinished" -> isNodeAnimationFinished() ? 1.0 : 0.0;

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


    private double getLivingDataValue(java.util.function.Function<ILivingEntityAnimationData, Double> getter, double defaultValue) {
        if (entityData instanceof ILivingEntityAnimationData livingData) {
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
