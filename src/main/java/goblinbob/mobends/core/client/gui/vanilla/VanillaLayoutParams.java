package goblinbob.mobends.core.client.gui.vanilla;

public class VanillaLayoutParams
{
    public static final int MATCH_PARENT = -1;
    public static final int WRAP_CONTENT = -2;

    public static final int GRAVITY_NO_GRAVITY = 0;
    public static final int GRAVITY_CENTER = 0x11;
    public static final int GRAVITY_CENTER_HORIZONTAL = 0x01;
    public static final int GRAVITY_CENTER_VERTICAL = 0x10;

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

    public static VanillaLayoutParams matchParent()
    {
        return new VanillaLayoutParams(MATCH_PARENT, MATCH_PARENT);
    }

    public static int alignH(int gravity, int start, int avail, int size)
    {
        int g = gravity & 0x07;
        return g == 0x05 ? start + avail - size : g == 0x01 ? start + (avail - size) / 2 : start;
    }

    public static int alignV(int gravity, int start, int avail, int size)
    {
        int g = gravity & 0x70;
        return g == 0x50 ? start + avail - size : g == 0x10 ? start + (avail - size) / 2 : start;
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

    public VanillaLayoutParams setGravity(int gravity)
    {
        this.gravity = gravity;
        return this;
    }

    public float getWeight() { return weight; }

    public int getGravity() { return gravity; }

    public int getMarginLeft() { return marginLeft; }

    public int getMarginTop() { return marginTop; }

    public int getMarginRight() { return marginRight; }

    public int getMarginBottom() { return marginBottom; }
}
