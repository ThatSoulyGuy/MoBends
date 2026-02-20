package goblinbob.mobends.api.gui.view;

/**
 * Abstraction for an image display view.
 */
public interface IImageView extends IView
{
    void setImageDrawable(Object drawable);
    void setImageResource(String resourcePath);
    void setImageDrawable();
    void setScaleType(ScaleType scaleType);
    void setImageTintColor(int color);

    enum ScaleType
    {
        CENTER_CROP, FIT_CENTER, FIT_XY, CENTER
    }
}
