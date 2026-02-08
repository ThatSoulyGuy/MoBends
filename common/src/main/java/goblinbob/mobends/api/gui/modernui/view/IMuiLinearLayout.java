package goblinbob.mobends.api.gui.modernui.view;

/**
 * Abstraction for Modern UI LinearLayout.
 * Arranges children in a single row (horizontal) or column (vertical).
 */
public interface IMuiLinearLayout extends IMuiViewGroup
{
    /**
     * Horizontal orientation - children are arranged left to right.
     */
    int HORIZONTAL = 0;

    /**
     * Vertical orientation - children are arranged top to bottom.
     */
    int VERTICAL = 1;

    /**
     * Sets the orientation of this layout.
     *
     * @param orientation HORIZONTAL or VERTICAL
     */
    void setOrientation(int orientation);

    /**
     * @return The current orientation (HORIZONTAL or VERTICAL)
     */
    int getOrientation();

    /**
     * Sets the gravity/alignment for children within this layout.
     *
     * @param gravity The gravity flags
     */
    void setGravity(int gravity);

    /**
     * Sets the spacing between children.
     *
     * @param spacing Spacing in dp
     */
    void setSpacing(int spacing);

    // ==================== Gravity Constants ====================

    int GRAVITY_START = 0x00800003;
    int GRAVITY_END = 0x00800005;
    int GRAVITY_TOP = 0x30;
    int GRAVITY_BOTTOM = 0x50;
    int GRAVITY_CENTER = 0x11;
    int GRAVITY_CENTER_HORIZONTAL = 0x01;
    int GRAVITY_CENTER_VERTICAL = 0x10;
}
