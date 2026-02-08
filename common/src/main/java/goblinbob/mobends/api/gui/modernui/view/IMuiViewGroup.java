package goblinbob.mobends.api.gui.modernui.view;

import goblinbob.mobends.api.gui.modernui.ILayoutParams;

/**
 * Abstraction for Modern UI ViewGroup.
 * A container that holds and lays out child views.
 */
public interface IMuiViewGroup extends IMuiView
{
    /**
     * Adds a child view to this container.
     *
     * @param child The view to add
     */
    void addView(IMuiView child);

    /**
     * Adds a child view with specific layout parameters.
     *
     * @param child The view to add
     * @param params The layout parameters for the child
     */
    void addView(IMuiView child, ILayoutParams params);

    /**
     * Adds a child view at a specific index.
     *
     * @param child The view to add
     * @param index The position to insert at
     */
    void addView(IMuiView child, int index);

    /**
     * Removes a child view from this container.
     *
     * @param child The view to remove
     */
    void removeView(IMuiView child);

    /**
     * Removes the child at the specified index.
     *
     * @param index The index of the child to remove
     */
    void removeViewAt(int index);

    /**
     * Removes all children from this container.
     */
    void removeAllViews();

    /**
     * @return The number of child views
     */
    int getChildCount();

    /**
     * Gets the child at the specified index.
     *
     * @param index The child index
     * @return The child view, or null if index is invalid
     */
    IMuiView getChildAt(int index);

    /**
     * Finds a child view by ID.
     *
     * @param id The view ID to search for
     * @return The view with the given ID, or null if not found
     */
    IMuiView findViewById(int id);
}
