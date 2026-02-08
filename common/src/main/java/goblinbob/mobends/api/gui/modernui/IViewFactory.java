package goblinbob.mobends.api.gui.modernui;

import goblinbob.mobends.api.gui.modernui.view.*;

import java.util.function.Consumer;

/**
 * Factory for creating Modern UI views.
 * Abstracts construction differences between API versions.
 */
public interface IViewFactory
{
    // ==================== Basic Views ====================

    /**
     * Creates a basic empty view.
     *
     * @return A new view
     */
    IMuiView createView();

    /**
     * Creates a button with text.
     *
     * @param text The button text
     * @return A new button
     */
    IMuiButton createButton(String text);

    /**
     * Creates a toggle/checkbox.
     *
     * @param initialState The initial checked state
     * @return A new toggle
     */
    IMuiToggle createToggle(boolean initialState);

    /**
     * Creates a text input field.
     *
     * @param hint The placeholder hint text
     * @return A new text field
     */
    IMuiTextField createTextField(String hint);

    /**
     * Creates an image view.
     *
     * @return A new image view
     */
    IMuiImageView createImageView();

    /**
     * Creates a text view/label.
     *
     * @param text The initial text
     * @return A new text view
     */
    IMuiTextView createTextView(String text);

    // ==================== Layouts ====================

    /**
     * Creates a linear layout (vertical or horizontal).
     *
     * @param orientation VERTICAL (1) or HORIZONTAL (0)
     * @return A new linear layout
     */
    IMuiLinearLayout createLinearLayout(int orientation);

    /**
     * Creates a frame layout (stacking children on top of each other).
     *
     * @return A new frame layout
     */
    IMuiFrameLayout createFrameLayout();

    /**
     * Creates a scrollable container.
     *
     * @return A new scroll view
     */
    IMuiScrollView createScrollView();

    /**
     * Creates a list view for displaying scrollable lists.
     *
     * @return A new list view
     */
    IMuiListView createListView();

    // ==================== Layout Params ====================

    /**
     * Creates LinearLayout parameters with specific dimensions.
     * Use this when adding views to a LinearLayout.
     *
     * @param width Width in dp, or MATCH_PARENT/WRAP_CONTENT
     * @param height Height in dp, or MATCH_PARENT/WRAP_CONTENT
     * @return New layout parameters suitable for LinearLayout
     */
    ILayoutParams createLayoutParams(int width, int height);

    /**
     * Creates LinearLayout parameters with weight.
     * Use this for views that should expand to fill remaining space.
     *
     * @param width Width in dp, or MATCH_PARENT/WRAP_CONTENT
     * @param height Height in dp, or MATCH_PARENT/WRAP_CONTENT
     * @param weight The weight for remaining space distribution
     * @return New layout parameters with weight
     */
    ILayoutParams createLayoutParams(int width, int height, float weight);

    /**
     * Creates FrameLayout parameters with gravity.
     * Use this when adding views to a FrameLayout.
     *
     * @param width Width in dp, or MATCH_PARENT/WRAP_CONTENT
     * @param height Height in dp, or MATCH_PARENT/WRAP_CONTENT
     * @param gravity The gravity flags (use ILayoutParams.GRAVITY_* constants)
     * @return New layout parameters with gravity
     */
    ILayoutParams createFrameLayoutParams(int width, int height, int gravity);

    /**
     * Creates layout parameters that match the parent size.
     *
     * @return New layout parameters with MATCH_PARENT for both dimensions
     */
    default ILayoutParams createMatchParent()
    {
        return createLayoutParams(ILayoutParams.MATCH_PARENT, ILayoutParams.MATCH_PARENT);
    }

    /**
     * Creates FrameLayout parameters that match parent and center the view.
     * Use this for root views returned from fragments.
     *
     * @return New FrameLayout parameters with MATCH_PARENT and CENTER gravity
     */
    default ILayoutParams createMatchParentCentered()
    {
        return createFrameLayoutParams(ILayoutParams.MATCH_PARENT, ILayoutParams.MATCH_PARENT, ILayoutParams.GRAVITY_CENTER);
    }

    /**
     * Creates layout parameters that wrap the content size.
     *
     * @return New layout parameters with WRAP_CONTENT for both dimensions
     */
    default ILayoutParams createWrapContent()
    {
        return createLayoutParams(ILayoutParams.WRAP_CONTENT, ILayoutParams.WRAP_CONTENT);
    }

    // ==================== Utility ====================

    /**
     * Converts density-independent pixels (dp) to actual pixels.
     * All dimension values should be passed through this method.
     *
     * @param dp The value in dp
     * @return The value in pixels
     */
    int dp(int dp);

    // ==================== Constants ====================

    /**
     * Horizontal orientation for LinearLayout.
     */
    int HORIZONTAL = 0;

    /**
     * Vertical orientation for LinearLayout.
     */
    int VERTICAL = 1;
}
