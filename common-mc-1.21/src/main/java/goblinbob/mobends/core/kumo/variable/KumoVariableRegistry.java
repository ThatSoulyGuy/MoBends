package goblinbob.mobends.core.kumo.variable;

import goblinbob.mobends.core.client.event.DataUpdateHandler;
import goblinbob.mobends.core.data.EntityData;
import goblinbob.mobends.core.data.LivingEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.HashMap;
import java.util.Set;

public class KumoVariableRegistry
{

    public static final KumoVariableRegistry instance = new KumoVariableRegistry();

    private EntityData<?> tempData;
    private HashMap<String, KumoVariableEntry> variables = new HashMap<>();

    public void provideTemporaryData(EntityData<?> tempData)
    {
        this.tempData = tempData;
    }

    /**
     * Gets the value of a variable by name.
     * @return The variable value, or 0 if not found
     */
    public double getVariableValue(String name)
    {
        KumoVariableEntry entry = variables.get(name);
        if (entry != null)
        {
            return entry.getVariable().getValue();
        }
        return 0;
    }

    /**
     * Checks if a variable exists in the registry.
     */
    public boolean hasVariable(String name)
    {
        return variables.containsKey(name);
    }

    /**
     * Gets all registered variable names.
     */
    public Set<String> getVariableNames()
    {
        return variables.keySet();
    }

    /**
     * Gets the temporary entity data currently being animated.
     */
    public EntityData<?> getTempData()
    {
        return tempData;
    }

    // Static methods

    static
    {
        // Time
        registerVariable("ticks", DataUpdateHandler::getTicks);

        // Constants
        registerVariable("PI", () -> Math.PI);
        registerVariable("E", () -> Math.E);

        // Random
        registerVariable("random", Math::random);

        // Attack timing
        registerVariable("ticksAfterPunch", () -> {
            if (instance.tempData instanceof LivingEntityData<?> livingData)
                return livingData.getTicksAfterAttack();
            return 0;
        });
        registerVariable("ticksAfterAttack", () -> {
            if (instance.tempData instanceof LivingEntityData<?> livingData)
                return livingData.getTicksAfterAttack();
            return 0;
        });

        // Health
        registerVariable("health", () -> {
            Entity entity = instance.tempData.getEntity();
            if (entity instanceof LivingEntity living)
                return living.getHealth();
            return 0;
        });
        registerVariable("maxHealth", () -> {
            Entity entity = instance.tempData.getEntity();
            if (entity instanceof LivingEntity living)
                return living.getMaxHealth();
            return 0;
        });
        registerVariable("healthPercent", () -> {
            Entity entity = instance.tempData.getEntity();
            if (entity instanceof LivingEntity living)
                return living.getHealth() / living.getMaxHealth();
            return 0;
        });

        // Motion
        registerVariable("motionX", () -> instance.tempData != null ? instance.tempData.getMotionX() : 0);
        registerVariable("motionY", () -> instance.tempData != null ? instance.tempData.getMotionY() : 0);
        registerVariable("motionZ", () -> instance.tempData != null ? instance.tempData.getMotionZ() : 0);
        registerVariable("motionMagnitude", () -> instance.tempData != null ? instance.tempData.getMotionMagnitude() : 0);
        registerVariable("xzMotionMagnitude", () -> instance.tempData != null ? instance.tempData.getXZMotionMagnitude() : 0);

        // Ground state
        registerVariable("onGround", () -> instance.tempData != null && instance.tempData.isOnGround() ? 1 : 0);
        registerVariable("isStill", () -> instance.tempData != null && instance.tempData.isStillHorizontally() ? 1 : 0);
        registerVariable("isStrafing", () -> instance.tempData != null && instance.tempData.isStrafing() ? 1 : 0);
        registerVariable("isUnderwater", () -> instance.tempData != null && instance.tempData.isUnderwater() ? 1 : 0);

        // Movement angles
        registerVariable("movementAngle", () -> instance.tempData != null ? instance.tempData.getMovementAngle() : 0);
        registerVariable("lookAngle", () -> instance.tempData != null ? instance.tempData.getLookAngle() : 0);
        registerVariable("forwardMomentum", () -> instance.tempData != null ? instance.tempData.getForwardMomentum() : 0);
        registerVariable("sidewaysMomentum", () -> instance.tempData != null ? instance.tempData.getSidewaysMomentum() : 0);

        // Living entity timing
        registerVariable("ticksInAir", () -> {
            if (instance.tempData instanceof LivingEntityData<?> livingData)
                return livingData.getTicksInAir();
            return 0;
        });
        registerVariable("ticksAfterTouchdown", () -> {
            if (instance.tempData instanceof LivingEntityData<?> livingData)
                return livingData.getTicksAfterTouchdown();
            return 0;
        });
        registerVariable("ticksFalling", () -> {
            if (instance.tempData instanceof LivingEntityData<?> livingData)
                return livingData.getTicksFalling();
            return 0;
        });

        // Limb swing
        registerVariable("limbSwing", () -> {
            if (instance.tempData instanceof LivingEntityData<?> livingData)
                return livingData.limbSwing.get();
            return 0;
        });
        registerVariable("limbSwingAmount", () -> {
            if (instance.tempData instanceof LivingEntityData<?> livingData)
                return livingData.limbSwingAmount.get();
            return 0;
        });

        // Head rotation
        registerVariable("headYaw", () -> {
            if (instance.tempData instanceof LivingEntityData<?> livingData)
                return livingData.headYaw.get();
            return 0;
        });
        registerVariable("headPitch", () -> {
            if (instance.tempData instanceof LivingEntityData<?> livingData)
                return livingData.headPitch.get();
            return 0;
        });

        // Arm swing
        registerVariable("swingProgress", () -> {
            if (instance.tempData instanceof LivingEntityData<?> livingData)
                return livingData.swingProgress.get();
            return 0;
        });

        // Climbing
        registerVariable("isClimbing", () -> {
            if (instance.tempData instanceof LivingEntityData<?> livingData)
                return livingData.isClimbing() ? 1 : 0;
            return 0;
        });
        registerVariable("climbingCycle", () -> {
            if (instance.tempData instanceof LivingEntityData<?> livingData)
                return livingData.getClimbingCycle();
            return 0;
        });

        // Bow
        registerVariable("isDrawingBow", () -> {
            if (instance.tempData instanceof LivingEntityData<?> livingData)
                return livingData.isDrawingBow() ? 1 : 0;
            return 0;
        });
    }

    private static void registerVariable(String key, IKumoVariable variable)
    {
        instance.variables.put(key, new KumoVariableEntry(variable, key));
    }

}
