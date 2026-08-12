package goblinbob.mobends.standard.client.model.armor;

import java.util.List;

public final class LimbInflation
{
    public static final LimbInflation NONE = new LimbInflation(0.0F, 0.0F, 0.0F);

    public static final float ARM_INFLATION = 0.001F;
    public static final float LEG_INFLATION = 0.0F;
    public static final float LOWER_LIMB_INFLATION_STEP = 0.001F;

    private final float amount;
    private final float centerX;
    private final float centerZ;

    private LimbInflation(float amount, float centerX, float centerZ)
    {
        this.amount = amount;
        this.centerX = centerX;
        this.centerZ = centerZ;
    }

    public static LimbInflation of(List<CapturedVertex> vertices, float modelUnits)
    {
        if (modelUnits == 0.0F || vertices == null || vertices.isEmpty())
        {
            return NONE;
        }

        float minX = Float.MAX_VALUE, maxX = -Float.MAX_VALUE;
        float minZ = Float.MAX_VALUE, maxZ = -Float.MAX_VALUE;

        for (CapturedVertex v : vertices)
        {
            if (v.x < minX) minX = v.x;
            if (v.x > maxX) maxX = v.x;
            if (v.z < minZ) minZ = v.z;
            if (v.z > maxZ) maxZ = v.z;
        }

        return new LimbInflation(
                modelUnits * ArmorPoseHelper.SCALE,
                (minX + maxX) * 0.5F,
                (minZ + maxZ) * 0.5F
        );
    }

    public LimbInflation plus(float modelUnits)
    {
        if (modelUnits == 0.0F)
        {
            return this;
        }
        return new LimbInflation(amount + modelUnits * ArmorPoseHelper.SCALE, centerX, centerZ);
    }

    public float displaceX(float x)
    {
        if (amount == 0.0F)
        {
            return x;
        }
        return x + Math.signum(x - centerX) * amount;
    }

    public float displaceZ(float z)
    {
        if (amount == 0.0F)
        {
            return z;
        }
        return z + Math.signum(z - centerZ) * amount;
    }
}
