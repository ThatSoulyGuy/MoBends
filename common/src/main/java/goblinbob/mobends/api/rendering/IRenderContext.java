package goblinbob.mobends.api.rendering;

import goblinbob.mobends.api.entity.ILivingEntity;

/**
 * Unified render context containing all information needed for entity rendering.
 * Combines pose stack, buffer source, and render parameters.
 */
public interface IRenderContext
{
    /**
     * @return The pose stack for transformations
     */
    IPoseStack getPoseStack();

    /**
     * @return The buffer source for obtaining vertex consumers
     */
    IBufferSource getBufferSource();

    /**
     * @return The entity being rendered
     */
    ILivingEntity getEntity();

    /**
     * @return The packed light value for lighting
     */
    int getPackedLight();

    /**
     * @return The packed overlay value (hurt/death flash)
     */
    int getPackedOverlay();

    /**
     * @return The partial tick time for interpolation
     */
    float getPartialTicks();

    /**
     * @return The entity's yaw body rotation (interpolated)
     */
    float getYBodyRot();

    /**
     * @return The entity's head yaw rotation relative to body
     */
    float getHeadYaw();

    /**
     * @return The entity's head pitch rotation
     */
    float getHeadPitch();

    /**
     * @return The limb swing progress
     */
    float getLimbSwing();

    /**
     * @return The limb swing amount (intensity)
     */
    float getLimbSwingAmount();

    /**
     * @return The age in ticks for texture animation
     */
    float getAgeInTicks();

    /**
     * Creates a new render context with a different entity
     * @param entity The new entity
     * @return A new render context
     */
    IRenderContext withEntity(ILivingEntity entity);
}
