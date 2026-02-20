package goblinbob.mobends.api.gui;

/**
 * Abstraction for layout parameters.
 * Defines how a view should be laid out within its parent.
 */
public interface ILayoutParams
{
    int MATCH_PARENT = -1;
    int WRAP_CONTENT = -2;

    int GRAVITY_NO_GRAVITY = 0;
    int GRAVITY_CENTER = 0x11;
    int GRAVITY_CENTER_HORIZONTAL = 0x01;
    int GRAVITY_CENTER_VERTICAL = 0x10;
    int GRAVITY_TOP = 0x30;
    int GRAVITY_BOTTOM = 0x50;
    int GRAVITY_LEFT = 0x03;
    int GRAVITY_RIGHT = 0x05;
    int GRAVITY_START = 0x00800003;
    int GRAVITY_END = 0x00800005;

    int getWidth();
    int getHeight();
    void setMargins(int left, int top, int right, int bottom);
    void setWeight(float weight);
    void setGravity(int gravity);
    Object getNativeLayoutParams();
}
