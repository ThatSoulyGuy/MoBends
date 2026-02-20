package goblinbob.mobends.api.gui.view;

/**
 * Abstraction for a linear layout.
 * Arranges children in a single row (horizontal) or column (vertical).
 */
public interface ILinearLayout extends IViewGroup
{
    int HORIZONTAL = 0;
    int VERTICAL = 1;

    int GRAVITY_START = 0x00800003;
    int GRAVITY_END = 0x00800005;
    int GRAVITY_TOP = 0x30;
    int GRAVITY_BOTTOM = 0x50;
    int GRAVITY_CENTER = 0x11;
    int GRAVITY_CENTER_HORIZONTAL = 0x01;
    int GRAVITY_CENTER_VERTICAL = 0x10;

    void setOrientation(int orientation);
    int getOrientation();
    void setGravity(int gravity);
    void setSpacing(int spacing);
}
