package goblinbob.mobends.core.client.gui.vanilla;

import goblinbob.mobends.api.gui.ILayoutParams;

public class VanillaLayoutParams implements ILayoutParams
{
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

    @Override
    public int getWidth() { return width; }

    @Override
    public int getHeight() { return height; }

    @Override
    public void setMargins(int left, int top, int right, int bottom)
    {
        this.marginLeft = left;
        this.marginTop = top;
        this.marginRight = right;
        this.marginBottom = bottom;
    }

    @Override
    public void setWeight(float weight) { this.weight = weight; }

    @Override
    public void setGravity(int gravity) { this.gravity = gravity; }

    @Override
    public Object getNativeLayoutParams() { return this; }

    public float getWeight() { return weight; }

    public int getGravity() { return gravity; }

    public int getMarginLeft() { return marginLeft; }

    public int getMarginTop() { return marginTop; }

    public int getMarginRight() { return marginRight; }

    public int getMarginBottom() { return marginBottom; }
}
