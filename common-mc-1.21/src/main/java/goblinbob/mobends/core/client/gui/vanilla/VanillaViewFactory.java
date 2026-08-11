package goblinbob.mobends.core.client.gui.vanilla;

import goblinbob.mobends.core.client.gui.EntityPreviewRenderer;

public class VanillaViewFactory
{
    public static final int HORIZONTAL = 0;
    public static final int VERTICAL = 1;

    public VanillaView createView() { return new VanillaView(); }

    public VanillaButton createButton(String text) { return new VanillaButton(text); }

    public VanillaToggle createToggle(boolean initialState) { return new VanillaToggle(initialState); }

    public VanillaTextField createTextField(String hint) { return new VanillaTextField(hint); }

    public VanillaTextView createTextView(String text) { return new VanillaTextView(text); }

    public VanillaLinearLayout createLinearLayout(int orientation)
    {
        VanillaLinearLayout layout = new VanillaLinearLayout();
        layout.setOrientation(orientation);
        return layout;
    }

    public VanillaFrameLayout createFrameLayout() { return new VanillaFrameLayout(); }

    public VanillaScrollView createScrollView() { return new VanillaScrollView(); }

    public VanillaListView createListView() { return new VanillaListView(); }

    public VanillaLayoutParams createLayoutParams(int width, int height)
    {
        return new VanillaLayoutParams(width, height);
    }

    public VanillaLayoutParams createLayoutParams(int width, int height, float weight)
    {
        return new VanillaLayoutParams(width, height, weight);
    }

    public VanillaLayoutParams createFrameLayoutParams(int width, int height, int gravity)
    {
        VanillaLayoutParams params = new VanillaLayoutParams(width, height);
        params.setGravity(gravity);
        return params;
    }

    public VanillaView createEntityPreviewView(EntityPreviewRenderer renderer)
    {
        return new VanillaEntityPreviewView(renderer);
    }

    public VanillaLayoutParams createMatchParent()
    {
        return createLayoutParams(VanillaLayoutParams.MATCH_PARENT, VanillaLayoutParams.MATCH_PARENT);
    }

    public VanillaLayoutParams createMatchParentCentered()
    {
        return createFrameLayoutParams(VanillaLayoutParams.MATCH_PARENT, VanillaLayoutParams.MATCH_PARENT, VanillaLayoutParams.GRAVITY_CENTER);
    }

    public VanillaLayoutParams createWrapContent()
    {
        return createLayoutParams(VanillaLayoutParams.WRAP_CONTENT, VanillaLayoutParams.WRAP_CONTENT);
    }

    public int dp(int dp)
    {
        return dp;
    }
}
