package goblinbob.mobends.standard.client.model.armor;

import org.joml.Vector3f;

public final class JointDefinitions
{
    private static final float SCALE = 1.0f / 16.0f;

    private static final float ARM_ELBOW_Y = 4.0f * SCALE;

    private static final float LEG_KNEE_Y = 6.0f * SCALE;

    public static final JointPlane LEFT_ELBOW = new JointPlane(
        new Vector3f(0, ARM_ELBOW_Y, 0),
        new Vector3f(0, -1, 0)
    );

    public static final JointPlane RIGHT_ELBOW = new JointPlane(
        new Vector3f(0, ARM_ELBOW_Y, 0),
        new Vector3f(0, -1, 0)
    );

    public static final JointPlane LEFT_KNEE = new JointPlane(
        new Vector3f(0, LEG_KNEE_Y, 0),
        new Vector3f(0, -1, 0)
    );

    public static final JointPlane RIGHT_KNEE = new JointPlane(
        new Vector3f(0, LEG_KNEE_Y, 0),
        new Vector3f(0, -1, 0)
    );

    public static JointPlane getElbow(boolean isLeft)
    {
        return isLeft ? LEFT_ELBOW : RIGHT_ELBOW;
    }

    public static JointPlane getKnee(boolean isLeft)
    {
        return isLeft ? LEFT_KNEE : RIGHT_KNEE;
    }

    public static JointPlane getJointForRegion(BoneRegion region)
    {
        return switch (region)
        {
            case LEFT_ARM_LOWER -> LEFT_ELBOW;
            case RIGHT_ARM_LOWER -> RIGHT_ELBOW;
            case LEFT_LEG_LOWER -> LEFT_KNEE;
            case RIGHT_LEG_LOWER -> RIGHT_KNEE;
            default -> null;
        };
    }

    public static JointPlane createElbowPlane(float elbowY)
    {
        return new JointPlane(
            new Vector3f(0, elbowY * SCALE, 0),
            new Vector3f(0, -1, 0)
        );
    }

    public static JointPlane createWaistPlane(float waistY)
    {
        return new JointPlane(
            new Vector3f(0, waistY * SCALE, 0),
            new Vector3f(0, -1, 0)
        );
    }

    public static JointPlane createKneePlane(float kneeY)
    {
        return new JointPlane(
            new Vector3f(0, kneeY * SCALE, 0),
            new Vector3f(0, -1, 0)
        );
    }

    private JointDefinitions()
    {
    }
}
