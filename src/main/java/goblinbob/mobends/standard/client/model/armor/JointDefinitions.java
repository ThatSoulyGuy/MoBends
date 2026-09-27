package goblinbob.mobends.standard.client.model.armor;

import org.joml.Vector3f;

public final class JointDefinitions
{
    private static final float SCALE = 1.0f / 16.0f;

    private static final float ARM_ELBOW_Y = 4.0f * SCALE;

    private static final float LEG_KNEE_Y = 6.0f * SCALE;

    public static final JointPlane ELBOW = new JointPlane(
        new Vector3f(0, ARM_ELBOW_Y, 0),
        new Vector3f(0, -1, 0)
    );

    public static final JointPlane KNEE = new JointPlane(
        new Vector3f(0, LEG_KNEE_Y, 0),
        new Vector3f(0, -1, 0)
    );

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
