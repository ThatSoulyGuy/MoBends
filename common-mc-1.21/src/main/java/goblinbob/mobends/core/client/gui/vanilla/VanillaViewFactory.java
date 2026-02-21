package goblinbob.mobends.core.client.gui.vanilla;

import goblinbob.mobends.api.gui.IEntityRenderer;
import goblinbob.mobends.api.gui.ILayoutParams;
import goblinbob.mobends.api.gui.IViewFactory;
import goblinbob.mobends.api.gui.view.*;

public class VanillaViewFactory implements IViewFactory
{
    @Override
    public IView createView() { return new VanillaView(); }

    @Override
    public IButton createButton(String text) { return new VanillaButton(text); }

    @Override
    public IToggle createToggle(boolean initialState) { return new VanillaToggle(initialState); }

    @Override
    public ITextField createTextField(String hint) { return new VanillaTextField(hint); }

    @Override
    public IImageView createImageView() { return new VanillaImageView(); }

    @Override
    public ITextView createTextView(String text) { return new VanillaTextView(text); }

    @Override
    public ILinearLayout createLinearLayout(int orientation)
    {
        VanillaLinearLayout layout = new VanillaLinearLayout();
        layout.setOrientation(orientation);
        return layout;
    }

    @Override
    public IFrameLayout createFrameLayout() { return new VanillaFrameLayout(); }

    @Override
    public IScrollView createScrollView() { return new VanillaScrollView(); }

    @Override
    public IListView createListView() { return new VanillaListView(); }

    @Override
    public ILayoutParams createLayoutParams(int width, int height)
    {
        return new VanillaLayoutParams(width, height);
    }

    @Override
    public ILayoutParams createLayoutParams(int width, int height, float weight)
    {
        return new VanillaLayoutParams(width, height, weight);
    }

    @Override
    public ILayoutParams createFrameLayoutParams(int width, int height, int gravity)
    {
        VanillaLayoutParams params = new VanillaLayoutParams(width, height);
        params.setGravity(gravity);
        return params;
    }

    @Override
    public IView createEntityPreviewView(IEntityRenderer renderer)
    {
        return new VanillaEntityPreviewView(renderer);
    }

    @Override
    public int dp(int dp)
    {
        return dp;
    }
}
