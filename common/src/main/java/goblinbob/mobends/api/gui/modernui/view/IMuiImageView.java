package goblinbob.mobends.api.gui.modernui.view;

/**
 * Abstraction for Modern UI ImageView.
 * Displays images and drawables.
 */
public interface IMuiImageView extends IMuiView
{
    /**
     * Sets the image from a drawable.
     *
     * @param drawable The native drawable object
     */
    void setImageDrawable(Object drawable);

    /**
     * Sets the image from a resource path.
     *
     * @param resourcePath The resource path (e.g., "mobends:textures/gui/logo.png")
     */
    void setImageResource(String resourcePath);

    /**
     * Clears the image.
     */
    void setImageDrawable();

    /**
     * Sets the scale type for how the image should fit the view bounds.
     *
     * @param scaleType The scale type
     */
    void setScaleType(ScaleType scaleType);

    /**
     * Sets a tint color to apply over the image.
     *
     * @param color The tint color in ARGB format
     */
    void setImageTintColor(int color);

    /**
     * Image scaling modes.
     */
    enum ScaleType
    {
        /**
         * Scale the image to fill the view, maintaining aspect ratio.
         */
        CENTER_CROP,

        /**
         * Scale the image to fit entirely within the view, maintaining aspect ratio.
         */
        FIT_CENTER,

        /**
         * Scale the image to exactly match the view dimensions.
         */
        FIT_XY,

        /**
         * Center the image without scaling.
         */
        CENTER
    }
}
