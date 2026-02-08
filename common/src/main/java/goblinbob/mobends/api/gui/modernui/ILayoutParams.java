package goblinbob.mobends.api.gui.modernui;

/**
 * Abstraction for Modern UI LayoutParams.
 * Defines how a view should be laid out within its parent.
 */
public interface ILayoutParams
{
    /**
     * Special value for width/height meaning "match parent size".
     */
    int MATCH_PARENT = -1;

    /**
     * Special value for width/height meaning "wrap content size".
     */
    int WRAP_CONTENT = -2;

    // Gravity constants (matching Android/Modern UI Gravity class)
    int GRAVITY_NO_GRAVITY = 0;
    int GRAVITY_CENTER = 0x11; // CENTER_HORIZONTAL | CENTER_VERTICAL
    int GRAVITY_CENTER_HORIZONTAL = 0x01;
    int GRAVITY_CENTER_VERTICAL = 0x10;
    int GRAVITY_TOP = 0x30;
    int GRAVITY_BOTTOM = 0x50;
    int GRAVITY_LEFT = 0x03;
    int GRAVITY_RIGHT = 0x05;
    int GRAVITY_START = 0x00800003;
    int GRAVITY_END = 0x00800005;

    /**
     * @return The requested width (-1 for MATCH_PARENT, -2 for WRAP_CONTENT, or specific dp)
     */
    int getWidth();

    /**
     * @return The requested height (-1 for MATCH_PARENT, -2 for WRAP_CONTENT, or specific dp)
     */
    int getHeight();

    /**
     * Sets margins around this view.
     *
     * @param left Left margin in dp
     * @param top Top margin in dp
     * @param right Right margin in dp
     * @param bottom Bottom margin in dp
     */
    void setMargins(int left, int top, int right, int bottom);

    /**
     * Sets the weight for this view in a LinearLayout.
     * Weight determines how much of the remaining space this view should take.
     *
     * @param weight The weight value (0 = no weight, positive = takes remaining space proportionally)
     */
    void setWeight(float weight);

    /**
     * Sets the gravity for this view in a FrameLayout.
     * Gravity determines where the view is positioned within its parent.
     *
     * @param gravity The gravity flags (use GRAVITY_* constants)
     */
    void setGravity(int gravity);

    /**
     * @return The native LayoutParams object
     */
    Object getNativeLayoutParams();
}
