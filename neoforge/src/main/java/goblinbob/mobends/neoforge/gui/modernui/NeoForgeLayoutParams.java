package goblinbob.mobends.neoforge.gui.modernui;

import goblinbob.mobends.api.gui.modernui.ILayoutParams;
import goblinbob.mobends.api.gui.modernui.IViewFactory;
import icyllis.modernui.view.ViewGroup;
import icyllis.modernui.widget.FrameLayout;
import icyllis.modernui.widget.LinearLayout;

/**
 * NeoForge wrapper for Modern UI LayoutParams.
 */
public class NeoForgeLayoutParams implements ILayoutParams
{
    private final ViewGroup.LayoutParams nativeParams;
    private final IViewFactory factory;

    /**
     * Constructor for read-only LayoutParams (used by getLayoutParams()).
     * setMargins() will not convert dp values.
     */
    public NeoForgeLayoutParams(ViewGroup.LayoutParams nativeParams)
    {
        this.nativeParams = nativeParams;
        this.factory = null;
    }

    /**
     * Full constructor with factory for dp conversion.
     */
    public NeoForgeLayoutParams(ViewGroup.LayoutParams nativeParams, IViewFactory factory)
    {
        this.nativeParams = nativeParams;
        this.factory = factory;
    }

    @Override
    public int getWidth()
    {
        return nativeParams.width;
    }

    @Override
    public int getHeight()
    {
        return nativeParams.height;
    }

    @Override
    public void setMargins(int left, int top, int right, int bottom)
    {
        if (nativeParams instanceof ViewGroup.MarginLayoutParams marginParams)
        {
            // Convert dp to pixels if factory is available
            if (factory != null)
            {
                marginParams.setMargins(
                        factory.dp(left),
                        factory.dp(top),
                        factory.dp(right),
                        factory.dp(bottom)
                );
            }
            else
            {
                // Assume values are already in pixels
                marginParams.setMargins(left, top, right, bottom);
            }
        }
    }

    @Override
    public void setWeight(float weight)
    {
        if (nativeParams instanceof LinearLayout.LayoutParams linearParams)
        {
            linearParams.weight = weight;
        }
    }

    @Override
    public void setGravity(int gravity)
    {
        if (nativeParams instanceof FrameLayout.LayoutParams frameParams)
        {
            frameParams.gravity = gravity;
        }
        else if (nativeParams instanceof LinearLayout.LayoutParams linearParams)
        {
            linearParams.gravity = gravity;
        }
    }

    @Override
    public Object getNativeLayoutParams()
    {
        return nativeParams;
    }
}
