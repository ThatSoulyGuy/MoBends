package goblinbob.mobends.api.gui;

import javax.annotation.Nullable;

/**
 * Interface for rendering entities in UI screens.
 * Provides a bridge between the UI layout system and
 * Minecraft's entity rendering system.
 */
public interface IEntityRenderer
{
    void setBender(@Nullable Object bender);
    void setAnimationType(String animationType);
    void setRotation(float rotationX, float rotationY);
    void setScale(float scale);
    float getRotationX();
    float getRotationY();
    float getScale();
    void update();
    void render(int x, int y, int width, int height, float partialTicks);
    void resetView();
    boolean hasEntity();
}
