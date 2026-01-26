package goblinbob.mobends.standard.client.model.armor;

import org.joml.Vector3f;

/**
 * Assigns armor vertices to bones based on their spatial position.
 * Uses bounding regions and joint planes to determine which bone a vertex belongs to.
 *
 * All coordinates are in model space (1/16th scale, Y+ down for player model).
 * The model is rendered at the entity position, so coordinates are relative to entity center.
 */
public class ArmorBoneAssignment
{
    // Scale factor: model units to render units
    private static final float SCALE = 1.0f / 16.0f;

    // Joint planes for elbows and knees
    // Normal points toward the upper segment (shoulder/hip direction)
    private final JointPlane leftElbowPlane;
    private final JointPlane rightElbowPlane;
    private final JointPlane leftKneePlane;
    private final JointPlane rightKneePlane;

    // Bounding regions matching vanilla HumanoidModel coordinates (in render units)
    // Coordinates are scaled by SCALE (1/16) from model units
    //
    // Vanilla HumanoidModel world coordinates after pivot translation:
    // - body: pivot (0,0,0), cube (-4,0,-2) to (4,12,2)
    // - head: pivot (0,0,0), cube (-4,-8,-4) to (4,0,4)
    // - leftArm: pivot (5,2,0), cube (-1,-2,-2) to (3,10,2) → world (4,0,-2) to (8,12,2)
    // - rightArm: pivot (-5,2,0), cube (-3,-2,-2) to (1,10,2) → world (-8,0,-2) to (-4,12,2)
    // - leftLeg: pivot (1.9,12,0), cube (-2,0,-2) to (2,12,2) → world (-0.1,12,-2) to (3.9,24,2)
    // - rightLeg: pivot (-1.9,12,0), cube (-2,0,-2) to (2,12,2) → world (-3.9,12,-2) to (0.1,24,2)

    // Body region bounds: X[-4,4], Y[0,12], Z[-2,2]
    private static final float BODY_MIN_X = -4.0f * SCALE;
    private static final float BODY_MAX_X = 4.0f * SCALE;
    private static final float BODY_MIN_Y = 0.0f * SCALE;
    private static final float BODY_MAX_Y = 12.0f * SCALE;
    private static final float BODY_MIN_Z = -2.0f * SCALE;
    private static final float BODY_MAX_Z = 2.0f * SCALE;

    // Head region bounds: X[-4,4], Y[-8,0], Z[-4,4]
    private static final float HEAD_MIN_X = -4.0f * SCALE;
    private static final float HEAD_MAX_X = 4.0f * SCALE;
    private static final float HEAD_MIN_Y = -8.0f * SCALE;
    private static final float HEAD_MAX_Y = 0.0f * SCALE;
    private static final float HEAD_MIN_Z = -4.0f * SCALE;
    private static final float HEAD_MAX_Z = 4.0f * SCALE;

    // Left arm region: X[4,8], Y[0,12], Z[-2,2]
    private static final float LEFT_ARM_MIN_X = 4.0f * SCALE;
    private static final float LEFT_ARM_MAX_X = 8.0f * SCALE;
    private static final float ARM_MIN_Y = 0.0f * SCALE;
    private static final float ARM_MAX_Y = 12.0f * SCALE;
    private static final float ARM_MIN_Z = -2.0f * SCALE;
    private static final float ARM_MAX_Z = 2.0f * SCALE;

    // Right arm region: X[-8,-4], Y[0,12], Z[-2,2]
    private static final float RIGHT_ARM_MIN_X = -8.0f * SCALE;
    private static final float RIGHT_ARM_MAX_X = -4.0f * SCALE;

    // Left leg region: X[-0.1,3.9], Y[12,24], Z[-2,2]
    private static final float LEFT_LEG_MIN_X = -0.1f * SCALE;
    private static final float LEFT_LEG_MAX_X = 3.9f * SCALE;
    private static final float LEG_MIN_Y = 12.0f * SCALE;
    private static final float LEG_MAX_Y = 24.0f * SCALE;
    private static final float LEG_MIN_Z = -2.0f * SCALE;
    private static final float LEG_MAX_Z = 2.0f * SCALE;

    // Right leg region: X[-3.9,0.1], Y[12,24], Z[-2,2]
    private static final float RIGHT_LEG_MIN_X = -3.9f * SCALE;
    private static final float RIGHT_LEG_MAX_X = 0.1f * SCALE;

    // Elbow Y position (where upper arm meets forearm) - at Y=6 in model units
    private static final float ELBOW_Y = 6.0f * SCALE;

    // Knee Y position (where thigh meets shin) - at Y=18 in model units
    private static final float KNEE_Y = 18.0f * SCALE;

    public ArmorBoneAssignment()
    {
        // Create joint planes
        // Elbow planes: point at elbow, normal pointing up (toward shoulder)
        // Y is inverted (Y+ is down), so normal Y should be negative to point "up"
        leftElbowPlane = new JointPlane(
                6.0f * SCALE, ELBOW_Y, 0.0f,
                0.0f, -1.0f, 0.0f
        );
        rightElbowPlane = new JointPlane(
                -6.0f * SCALE, ELBOW_Y, 0.0f,
                0.0f, -1.0f, 0.0f
        );

        // Knee planes: point at knee, normal pointing up (toward hip)
        leftKneePlane = new JointPlane(
                2.0f * SCALE, KNEE_Y, 0.0f,
                0.0f, -1.0f, 0.0f
        );
        rightKneePlane = new JointPlane(
                -2.0f * SCALE, KNEE_Y, 0.0f,
                0.0f, -1.0f, 0.0f
        );
    }

    /**
     * Assign a vertex to a bone region based on its position.
     * @param x Vertex X position in render units
     * @param y Vertex Y position in render units
     * @param z Vertex Z position in render units
     * @return The bone region this vertex belongs to
     */
    public BoneRegion assignVertex(float x, float y, float z)
    {
        // Check regions in order of specificity

        // Head region - check first as it's above body
        if (isInsideHeadBounds(x, y, z))
        {
            return BoneRegion.HEAD;
        }

        // Left arm
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

        // Right arm
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

        // Left leg
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

        // Right leg
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

        // Body (torso) - check after limbs to avoid false positives
        if (isInsideBodyBounds(x, y, z))
        {
            return BoneRegion.BODY;
        }

        // Fallback
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
