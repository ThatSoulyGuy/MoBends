package goblinbob.mobends.neoforge.gui.modernui.view;

import goblinbob.mobends.api.gui.modernui.view.IMuiImageView;
import icyllis.modernui.graphics.drawable.Drawable;
import icyllis.modernui.widget.ImageView;

/**
 * NeoForge wrapper for Modern UI ImageView.
 * Uses Modern UI 3.11.x API.
 */
public class NeoForgeImageView extends NeoForgeView implements IMuiImageView
{
    private final ImageView nativeImageView;

    public NeoForgeImageView(ImageView nativeView)
    {
        super(nativeView);
        this.nativeImageView = nativeView;
    }

    @Override
    public void setImageDrawable(Object drawable)
    {
        if (drawable instanceof Drawable d)
        {
            nativeImageView.setImageDrawable(d);
        }
    }

    @Override
    public void setImageResource(String resourcePath)
    {
        // Resource loading from path would require ResourceManager integration
        // This is a placeholder - actual implementation needs resource handling
    }

    @Override
    public void setImageDrawable()
    {
        nativeImageView.setImageDrawable(null);
    }

    @Override
    public void setScaleType(ScaleType scaleType)
    {
        ImageView.ScaleType nativeScaleType = switch (scaleType)
        {
            case CENTER_CROP -> ImageView.ScaleType.CENTER_CROP;
            case FIT_CENTER -> ImageView.ScaleType.FIT_CENTER;
            case FIT_XY -> ImageView.ScaleType.FIT_XY;
            case CENTER -> ImageView.ScaleType.CENTER;
        };
        nativeImageView.setScaleType(nativeScaleType);
    }

    @Override
    public void setImageTintColor(int color)
    {
        // Modern UI 3.11.x may not have setImageTintList
        // This is a no-op placeholder - tinting needs different approach
    }
}
