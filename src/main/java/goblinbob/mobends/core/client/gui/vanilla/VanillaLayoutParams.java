package goblinbob.mobends.core.client.gui.vanilla;

public class VanillaLayoutParams
{
    public static final int MATCH_PARENT = -1;
    public static final int WRAP_CONTENT = -2;

    public static final int GRAVITY_NO_GRAVITY = 0;
    public static final int GRAVITY_CENTER = 0x11;
    public static final int GRAVITY_CENTER_HORIZONTAL = 0x01;
    public static final int GRAVITY_CENTER_VERTICAL = 0x10;
    public static final int GRAVITY_TOP = 0x30;
    public static final int GRAVITY_BOTTOM = 0x50;
    public static final int GRAVITY_LEFT = 0x03;
    public static final int GRAVITY_RIGHT = 0x05;
    public static final int GRAVITY_START = 0x00800003;
    public static final int GRAVITY_END = 0x00800005;

    private int width;
    private int height;
    private float weight;
    private int gravity;
    private int marginLeft, marginTop, marginRight, marginBottom;

    public VanillaLayoutParams(int width, int height)
    {
        this.width = width;
        this.height = height;
    }

    public VanillaLayoutParams(int width, int height, float weight)
    {
        this(width, height);
        this.weight = weight;
    }

    public int getWidth() { return width; }

    public int getHeight() { return height; }

    public void setMargins(int left, int top, int right, int bottom)
    {
        this.marginLeft = left;
        this.marginTop = top;
        this.marginRight = right;
        this.marginBottom = bottom;
    }

    public void setWeight(float weight) { this.weight = weight; }

    public void setGravity(int gravity) { this.gravity = gravity; }

    public Object getNativeLayoutParams() { return this; }

    public float getWeight() { return weight; }

    public int getGravity() { return gravity; }

    public int getMarginLeft() { return marginLeft; }

    public int getMarginTop() { return marginTop; }

    public int getMarginRight() { return marginRight; }

    public int getMarginBottom() { return marginBottom; }
}
