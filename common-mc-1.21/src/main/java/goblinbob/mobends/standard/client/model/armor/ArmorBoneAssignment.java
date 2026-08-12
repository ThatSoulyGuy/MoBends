package goblinbob.mobends.standard.client.model.armor;

import org.joml.Vector3f;

public class ArmorBoneAssignment
{
    private static final float SCALE = 1.0f / 16.0f;

    private final JointPlane leftElbowPlane;
    private final JointPlane rightElbowPlane;
    private final JointPlane leftKneePlane;
    private final JointPlane rightKneePlane;

    private static final float BODY_MIN_X = -4.0f * SCALE;
    private static final float BODY_MAX_X = 4.0f * SCALE;
    private static final float BODY_MIN_Y = 0.0f * SCALE;
    private static final float BODY_MAX_Y = 12.0f * SCALE;
    private static final float BODY_MIN_Z = -2.0f * SCALE;
    private static final float BODY_MAX_Z = 2.0f * SCALE;

    private static final float HEAD_MIN_X = -4.0f * SCALE;
    private static final float HEAD_MAX_X = 4.0f * SCALE;
    private static final float HEAD_MIN_Y = -8.0f * SCALE;
    private static final float HEAD_MAX_Y = 0.0f * SCALE;
    private static final float HEAD_MIN_Z = -4.0f * SCALE;
    private static final float HEAD_MAX_Z = 4.0f * SCALE;

    private static final float LEFT_ARM_MIN_X = 4.0f * SCALE;
    private static final float LEFT_ARM_MAX_X = 8.0f * SCALE;
    private static final float ARM_MIN_Y = 0.0f * SCALE;
    private static final float ARM_MAX_Y = 12.0f * SCALE;
    private static final float ARM_MIN_Z = -2.0f * SCALE;
    private static final float ARM_MAX_Z = 2.0f * SCALE;

    private static final float RIGHT_ARM_MIN_X = -8.0f * SCALE;
    private static final float RIGHT_ARM_MAX_X = -4.0f * SCALE;

    private static final float LEFT_LEG_MIN_X = -0.1f * SCALE;
    private static final float LEFT_LEG_MAX_X = 3.9f * SCALE;
    private static final float LEG_MIN_Y = 12.0f * SCALE;
    private static final float LEG_MAX_Y = 24.0f * SCALE;
    private static final float LEG_MIN_Z = -2.0f * SCALE;
    private static final float LEG_MAX_Z = 2.0f * SCALE;

    private static final float RIGHT_LEG_MIN_X = -3.9f * SCALE;
    private static final float RIGHT_LEG_MAX_X = 0.1f * SCALE;

    private static final float ELBOW_Y = 6.0f * SCALE;

    private static final float KNEE_Y = 18.0f * SCALE;

    public ArmorBoneAssignment()
    {
        leftElbowPlane = new JointPlane(
                6.0f * SCALE, ELBOW_Y, 0.0f,
                0.0f, -1.0f, 0.0f
        );
        rightElbowPlane = new JointPlane(
                -6.0f * SCALE, ELBOW_Y, 0.0f,
                0.0f, -1.0f, 0.0f
        );

        leftKneePlane = new JointPlane(
                2.0f * SCALE, KNEE_Y, 0.0f,
                0.0f, -1.0f, 0.0f
        );
        rightKneePlane = new JointPlane(
                -2.0f * SCALE, KNEE_Y, 0.0f,
                0.0f, -1.0f, 0.0f
        );
    }

    public BoneRegion assignVertex(float x, float y, float z)
    {

        if (isInsideHeadBounds(x, y, z))
        {
            return BoneRegion.HEAD;
        }

        if (isInsideLeftArmBounds(x, y, z))
        {
            if (leftElbowPlane.isAbovePlane(x, y, z))
            {
                return BoneRegion.LEFT_ARM_UPPER;
            }
            else
            {
                return BoneRegion.LEFT_ARM_LOWER;
            }
        }

        if (isInsideRightArmBounds(x, y, z))
        {
            if (rightElbowPlane.isAbovePlane(x, y, z))
            {
                return BoneRegion.RIGHT_ARM_UPPER;
            }
            else
            {
                return BoneRegion.RIGHT_ARM_LOWER;
            }
        }

        if (isInsideLeftLegBounds(x, y, z))
        {
            if (leftKneePlane.isAbovePlane(x, y, z))
            {
                return BoneRegion.LEFT_LEG_UPPER;
            }
            else
            {
                return BoneRegion.LEFT_LEG_LOWER;
            }
        }

        if (isInsideRightLegBounds(x, y, z))
        {
            if (rightKneePlane.isAbovePlane(x, y, z))
            {
                return BoneRegion.RIGHT_LEG_UPPER;
            }
            else
            {
                return BoneRegion.RIGHT_LEG_LOWER;
            }
        }

        if (isInsideBodyBounds(x, y, z))
        {
            return BoneRegion.BODY;
        }

        return BoneRegion.ROOT;
    }

    private boolean isInsideHeadBounds(float x, float y, float z)
    {
        return x >= HEAD_MIN_X && x <= HEAD_MAX_X &&
               y >= HEAD_MIN_Y && y <= HEAD_MAX_Y &&
               z >= HEAD_MIN_Z && z <= HEAD_MAX_Z;
    }

    private boolean isInsideBodyBounds(float x, float y, float z)
    {
        return x >= BODY_MIN_X && x <= BODY_MAX_X &&
               y >= BODY_MIN_Y && y <= BODY_MAX_Y &&
               z >= BODY_MIN_Z && z <= BODY_MAX_Z;
    }

    private boolean isInsideLeftArmBounds(float x, float y, float z)
    {
        return x >= LEFT_ARM_MIN_X && x <= LEFT_ARM_MAX_X &&
               y >= ARM_MIN_Y && y <= ARM_MAX_Y &&
               z >= ARM_MIN_Z && z <= ARM_MAX_Z;
    }

    private boolean isInsideRightArmBounds(float x, float y, float z)
    {
        return x >= RIGHT_ARM_MIN_X && x <= RIGHT_ARM_MAX_X &&
               y >= ARM_MIN_Y && y <= ARM_MAX_Y &&
               z >= ARM_MIN_Z && z <= ARM_MAX_Z;
    }

    private boolean isInsideLeftLegBounds(float x, float y, float z)
    {
        return x >= LEFT_LEG_MIN_X && x <= LEFT_LEG_MAX_X &&
               y >= LEG_MIN_Y && y <= LEG_MAX_Y &&
               z >= LEG_MIN_Z && z <= LEG_MAX_Z;
    }

    private boolean isInsideRightLegBounds(float x, float y, float z)
    {
        return x >= RIGHT_LEG_MIN_X && x <= RIGHT_LEG_MAX_X &&
               y >= LEG_MIN_Y && y <= LEG_MAX_Y &&
               z >= LEG_MIN_Z && z <= LEG_MAX_Z;
    }

    public JointPlane getLeftElbowPlane()
    {
        return leftElbowPlane;
    }

    public JointPlane getRightElbowPlane()
    {
        return rightElbowPlane;
    }

    public JointPlane getLeftKneePlane()
    {
        return leftKneePlane;
    }

    public JointPlane getRightKneePlane()
    {
        return rightKneePlane;
    }
}
