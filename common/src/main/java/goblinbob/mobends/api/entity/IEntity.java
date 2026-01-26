package goblinbob.mobends.api.entity;

/**
 * Platform-agnostic entity abstraction.
 * Provides access to basic entity properties without direct MC dependencies.
 */
public interface IEntity
{
    /**
     * @return The unique identifier for this entity
     */
    int getId();

    /**
     * @return The entity's X position in the world
     */
    double getX();

    /**
     * @return The entity's Y position in the world
     */
    double getY();

    /**
     * @return The entity's Z position in the world
     */
    double getZ();

    /**
     * @return The entity's previous X position (for interpolation)
     */
    double getPrevX();

    /**
     * @return The entity's previous Y position (for interpolation)
     */
    double getPrevY();

    /**
     * @return The entity's previous Z position (for interpolation)
     */
    double getPrevZ();

    /**
     * @return The entity's yaw rotation in degrees
     */
    float getYRot();

    /**
     * @return The entity's pitch rotation in degrees
     */
    float getXRot();

    /**
     * @return The entity's previous yaw rotation (for interpolation)
     */
    float getPrevYRot();

    /**
     * @return The entity's previous pitch rotation (for interpolation)
     */
    float getPrevXRot();

    /**
     * @return Whether the entity is currently on the ground
     */
    boolean isOnGround();

    /**
     * @return Whether the entity is in water
     */
    boolean isInWater();

    /**
     * @return Whether the entity is a passenger of another entity
     */
    boolean isPassenger();

    /**
     * @return The width of the entity's bounding box
     */
    float getBbWidth();

    /**
     * @return The height of the entity's bounding box
     */
    float getBbHeight();

    /**
     * @return The native platform entity object
     */
    Object getNative();

    /**
     * Calculates the interpolated X position
     * @param partialTicks The partial tick time
     * @return The interpolated X position
     */
    default double getLerpedX(float partialTicks)
    {
        return getPrevX() + (getX() - getPrevX()) * partialTicks;
    }

    /**
     * Calculates the interpolated Y position
     * @param partialTicks The partial tick time
     * @return The interpolated Y position
     */
    default double getLerpedY(float partialTicks)
    {
        return getPrevY() + (getY() - getPrevY()) * partialTicks;
    }

    /**
     * Calculates the interpolated Z position
     * @param partialTicks The partial tick time
     * @return The interpolated Z position
     */
    default double getLerpedZ(float partialTicks)
    {
        return getPrevZ() + (getZ() - getPrevZ()) * partialTicks;
    }
}
