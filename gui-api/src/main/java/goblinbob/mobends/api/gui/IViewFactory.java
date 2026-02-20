package goblinbob.mobends.api.gui;

import goblinbob.mobends.api.gui.view.*;

/**
 * Factory for creating UI views.
 * Abstracts construction differences between backend versions.
 */
public interface IViewFactory
{
    int HORIZONTAL = 0;
    int VERTICAL = 1;

    // Basic Views
    IView createView();
    IButton createButton(String text);
    IToggle createToggle(boolean initialState);
    ITextField createTextField(String hint);
    IImageView createImageView();
    ITextView createTextView(String text);

    // Layouts
    ILinearLayout createLinearLayout(int orientation);
    IFrameLayout createFrameLayout();
    IScrollView createScrollView();
    IListView createListView();

    // Layout Params
    ILayoutParams createLayoutParams(int width, int height);
    ILayoutParams createLayoutParams(int width, int height, float weight);
    ILayoutParams createFrameLayoutParams(int width, int height, int gravity);

    default ILayoutParams createMatchParent()
    {
        return createLayoutParams(ILayoutParams.MATCH_PARENT, ILayoutParams.MATCH_PARENT);
    }

    default ILayoutParams createMatchParentCentered()
    {
        return createFrameLayoutParams(ILayoutParams.MATCH_PARENT, ILayoutParams.MATCH_PARENT, ILayoutParams.GRAVITY_CENTER);
    }

    default ILayoutParams createWrapContent()
    {
        return createLayoutParams(ILayoutParams.WRAP_CONTENT, ILayoutParams.WRAP_CONTENT);
    }

    // Entity Preview
    default IView createEntityPreviewView(IEntityRenderer renderer)
    {
        return createView();
    }

    // Utility
    int dp(int dp);
}
