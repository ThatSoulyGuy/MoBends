package goblinbob.mobends.api.entity;

import javax.annotation.Nullable;

/**
 * Platform-agnostic living entity abstraction.
 * Extends IEntity with properties specific to living entities.
 */
public interface ILivingEntity extends IEntity
{
    /**
     * @return The current health of this entity
     */
    float getHealth();

    /**
     * @return The maximum health of this entity
     */
    float getMaxHealth();

    /**
     * @return The limb swing progress (used for walk animation)
     */
    float getLimbSwing();

    /**
     * @return The limb swing amount (intensity of walk animation)
     */
    float getLimbSwingAmount();

    /**
     * @return The attack animation progress (0.0 to 1.0)
     */
    float getAttackAnim();

    /**
     * @return The hurt time remaining
     */
    int getHurtTime();

    /**
     * @return The death time
     */
    int getDeathTime();

    /**
     * @return The head yaw rotation (independent of body)
     */
    float getHeadYaw();

    /**
     * @return The previous head yaw rotation
     */
    float getPrevHeadYaw();

    /**
     * @return The body yaw rotation
     */
    float getBodyYaw();

    /**
     * @return The previous body yaw rotation
     */
    float getPrevBodyYaw();

    /**
     * @return Whether this entity is a baby
     */
    boolean isBaby();

    /**
     * @return Whether this entity is currently sleeping
     */
    boolean isSleeping();

    /**
     * @return Whether this entity is dead
     */
    boolean isDead();

    /**
     * @return Whether this entity is currently using an item
     */
    boolean isUsingItem();

    /**
     * @return The number of ticks this entity has been using the current item
     */
    int getTicksUsingItem();

    /**
     * @return The hand currently being used for an item (0 = main, 1 = off), or -1 if not using
     */
    int getUsedItemHand();

    /**
     * @return Whether this entity is currently gliding with elytra
     */
    boolean isFallFlying();

    /**
     * @return Whether this entity is currently swimming
     */
    boolean isSwimming();

    /**
     * @return Whether this entity is visually swimming (pose)
     */
    boolean isVisuallySwimming();

    /**
     * @return Whether this entity is currently crouching/sneaking
     */
    boolean isCrouching();

    /**
     * @return The item in the specified equipment slot
     */
    IItemStack getItemBySlot(IEquipmentSlot slot);

    /**
     * @return The item in the main hand
     */
    default IItemStack getMainHandItem()
    {
        return getItemBySlot(IEquipmentSlot.MAINHAND);
    }

    /**
     * @return The item in the off hand
     */
    default IItemStack getOffhandItem()
    {
        return getItemBySlot(IEquipmentSlot.OFFHAND);
    }

    /**
     * @return The vehicle this entity is riding, or null if not riding
     */
    @Nullable
    IEntity getVehicle();

    /**
     * Calculates the interpolated body yaw
     * @param partialTicks The partial tick time
     * @return The interpolated body yaw
     */
    default float getLerpedBodyYaw(float partialTicks)
    {
        return getPrevBodyYaw() + (getBodyYaw() - getPrevBodyYaw()) * partialTicks;
    }

    /**
     * Calculates the interpolated head yaw
     * @param partialTicks The partial tick time
     * @return The interpolated head yaw
     */
    default float getLerpedHeadYaw(float partialTicks)
    {
        return getPrevHeadYaw() + (getHeadYaw() - getPrevHeadYaw()) * partialTicks;
    }
}
