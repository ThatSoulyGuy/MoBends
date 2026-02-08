package goblinbob.mobends.api.gui.modernui;

import javax.annotation.Nullable;

/**
 * Interface for rendering entities in Modern UI screens.
 * This provides a bridge between Modern UI's layout system and
 * Minecraft's entity rendering system.
 */
public interface IEntityRenderer
{
    /**
     * Sets the bender whose entity should be rendered.
     * The bender should be an EntityBender instance from the core module.
     *
     * @param bender The bender to preview, or null to clear
     */
    void setBender(@Nullable Object bender);

    /**
     * Sets the animation type to display.
     *
     * @param animationType The animation type name (e.g., "walk", "sprint")
     */
    void setAnimationType(String animationType);

    /**
     * Sets the view rotation.
     *
     * @param rotationX Pitch (vertical rotation)
     * @param rotationY Yaw (horizontal rotation)
     */
    void setRotation(float rotationX, float rotationY);

    /**
     * Sets the view scale.
     *
     * @param scale The scale factor
     */
    void setScale(float scale);

    /**
     * Gets the current X rotation (pitch).
     */
    float getRotationX();

    /**
     * Gets the current Y rotation (yaw).
     */
    float getRotationY();

    /**
     * Gets the current scale.
     */
    float getScale();

    /**
     * Updates the renderer state (animation, etc.).
     * Should be called each frame.
     */
    void update();

    /**
     * Renders the entity at the specified screen position.
     * This should be called during Minecraft's render phase.
     *
     * @param x            Screen X position (center of render area)
     * @param y            Screen Y position (bottom of render area)
     * @param width        Width of render area
     * @param height       Height of render area
     * @param partialTicks Partial ticks for interpolation
     */
    void render(int x, int y, int width, int height, float partialTicks);

    /**
     * Resets the view to default rotation and scale.
     */
    void resetView();

    /**
     * @return Whether an entity is currently loaded for preview
     */
    boolean hasEntity();
}
