package goblinbob.mobends.standard.client.model.armor;

public final class BodyClearance
{
    private static final float SCALE = 1.0F / 16.0F;

    private static final float OUTER_SKIN_LAYER = 0.25F;
    private static final float MARGIN = 0.05F;
    private static final float FLUSH_TOLERANCE = 0.05F;
    private static final float PRESERVED_CLEARANCE = 0.5F;

    private static final float REQUIRED = (OUTER_SKIN_LAYER + MARGIN) * SCALE;
    private static final float PRESERVED = PRESERVED_CLEARANCE * SCALE;
    private static final float FLUSH = FLUSH_TOLERANCE * SCALE;

    private static final float ARM_AXIS_X = 6.0F * SCALE;
    private static final float LEG_AXIS_X = 1.9F * SCALE;

    private static final float LIMB_HALF = 2.0F * SCALE;
    private static final float BODY_HALF_X = 4.0F * SCALE;
    private static final float BODY_HALF_Z = 2.0F * SCALE;

    private BodyClearance()
    {
    }

    public static float clearX(BoneRegion region, float x)
    {
        return push(x, axisX(region), halfX(region));
    }

    public static float clearZ(BoneRegion region, float z)
    {
        return push(z, 0.0F, halfZ(region));
    }

    private static float push(float value, float axis, float half)
    {
        if (half <= 0.0F)
        {
            return value;
        }

        final float offset = value - axis;
        final float distance = Math.abs(offset);

        if (distance < half - FLUSH)
        {
            return value;
        }

        final float clearance = Math.max(0.0F, distance - half);

        if (clearance >= PRESERVED)
        {
            return value;
        }

        final float raised = REQUIRED + (PRESERVED - REQUIRED) * (clearance / PRESERVED);
        final float corrected = half + raised;

        return offset < 0.0F ? axis - corrected : axis + corrected;
    }

    private static float axisX(BoneRegion region)
    {
        switch (region)
        {
            case LEFT_ARM_UPPER:
            case LEFT_ARM_LOWER:
                return ARM_AXIS_X;
            case RIGHT_ARM_UPPER:
            case RIGHT_ARM_LOWER:
                return -ARM_AXIS_X;
            case LEFT_LEG_UPPER:
            case LEFT_LEG_LOWER:
                return LEG_AXIS_X;
            case RIGHT_LEG_UPPER:
            case RIGHT_LEG_LOWER:
                return -LEG_AXIS_X;
            default:
                return 0.0F;
        }
    }

    private static float halfX(BoneRegion region)
    {
        switch (region)
        {
            case BODY:
                return BODY_HALF_X;
            case LEFT_ARM_UPPER:
            case LEFT_ARM_LOWER:
            case RIGHT_ARM_UPPER:
            case RIGHT_ARM_LOWER:
            case LEFT_LEG_UPPER:
            case LEFT_LEG_LOWER:
            case RIGHT_LEG_UPPER:
            case RIGHT_LEG_LOWER:
                return LIMB_HALF;
            default:
                return 0.0F;
        }
    }

    private static float halfZ(BoneRegion region)
    {
        switch (region)
        {
            case BODY:
                return BODY_HALF_Z;
            case LEFT_ARM_UPPER:
            case LEFT_ARM_LOWER:
            case RIGHT_ARM_UPPER:
            case RIGHT_ARM_LOWER:
            case LEFT_LEG_UPPER:
            case LEFT_LEG_LOWER:
            case RIGHT_LEG_UPPER:
            case RIGHT_LEG_LOWER:
                return LIMB_HALF;
            default:
                return 0.0F;
        }
    }
}
