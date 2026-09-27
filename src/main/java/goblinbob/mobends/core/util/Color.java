package goblinbob.mobends.core.util;

import goblinbob.mobends.lib.util.GUtil;

public class Color implements IColorRead
{
	public float r, g, b, a;

	public Color(float r, float g, float b, float a)
	{
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = a;
	}

	@Override
	public float getR()
	{
		return this.r;
	}

	@Override
	public float getG()
	{
		return this.g;
	}

	@Override
	public float getB()
	{
		return this.b;
	}

	@Override
	public float getA()
	{
		return this.a;
	}

	public static int asHex(IColorRead color)
	{
		int valueA = (int) GUtil.clamp(color.getA() * 255F, 0, 255);
		int valueR = (int) GUtil.clamp(color.getR() * 255F, 0, 255);
		int valueG = (int) GUtil.clamp(color.getG() * 255F, 0, 255);
		int valueB = (int) GUtil.clamp(color.getB() * 255F, 0, 255);
		int value = valueA;
		value <<= 8;
		value |= valueR & 255;
		value <<= 8;
		value |= valueG & 255;
		value <<= 8;
		value |= valueB & 255;
		return value;
	}

	public static Color fromHexRGB(int hexValue)
	{
		int valueB = hexValue & 255;
		hexValue >>= 8;
		int valueG = hexValue & 255;
		hexValue >>= 8;
		int valueR = hexValue & 255;
		hexValue >>= 8;

		return new Color(valueR / 255.0F, valueG / 255.0F, valueB / 255.0F, 1.0F);
	}
}
