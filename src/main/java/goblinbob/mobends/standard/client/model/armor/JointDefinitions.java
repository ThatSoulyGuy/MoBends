package goblinbob.mobends.standard.client.model.armor;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector3f;

/**
 * Predefined joint planes for the humanoid model.
 * These define where limbs should be split for proper bone-weighted rendering.
 *
 * All coordinates are in Minecraft's model space where:
 * - Y is up (negative Y is toward head)
 * - Z is forward (toward front of entity)
 * - X is left/right (positive X is left side of entity when facing forward)
 *
 * Joint positions are based on the vanilla player model dimensions.
 */
@OnlyIn(Dist.CLIENT)
public final class JointDefinitions
{
    // Model scale factor (Minecraft uses 1/16 scale)
    private static final float SCALE = 1.0f / 16.0f;

    // Arm joint positions (relative to arm pivot)
    // Vanilla arm is 12 units long, elbow at roughly 4 units down
    private static final float ARM_ELBOW_Y = 4.0f * SCALE;

    // Leg joint positions (relative to leg pivot)
    // Vanilla leg is 12 units long, knee at roughly 6 units down
    private static final float LEG_KNEE_Y = 6.0f * SCALE;

    /**
     * Left elbow joint plane.
     * Positioned at the elbow joint of the left arm.
     * Normal points upward (toward shoulder).
     */
    public static final JointPlane LEFT_ELBOW = new JointPlane(
        new Vector3f(0, ARM_ELBOW_Y, 0),
        new Vector3f(0, -1, 0)  // Normal points toward upper arm (negative Y in arm space)
    );

    /**
     * Right elbow joint plane.
     * Positioned at the elbow joint of the right arm.
     * Normal points upward (toward shoulder).
     */
    public static final JointPlane RIGHT_ELBOW = new JointPlane(
        new Vector3f(0, ARM_ELBOW_Y, 0),
        new Vector3f(0, -1, 0)  // Normal points toward upper arm
    );

    /**
     * Left knee joint plane.
     * Positioned at the knee joint of the left leg.
     * Normal points upward (toward hip).
     */
    public static final JointPlane LEFT_KNEE = new JointPlane(
        new Vector3f(0, LEG_KNEE_Y, 0),
        new Vector3f(0, -1, 0)  // Normal points toward upper leg
    );

    /**
     * Right knee joint plane.
     * Positioned at the knee joint of the right leg.
     * Normal points upward (toward hip).
     */
    public static final JointPlane RIGHT_KNEE = new JointPlane(
        new Vector3f(0, LEG_KNEE_Y, 0),
        new Vector3f(0, -1, 0)  // Normal points toward upper leg
    );

    /**
     * Get the appropriate elbow joint plane for a given side.
     * @param isLeft true for left arm, false for right arm
     * @return The elbow joint plane
     */
    public static JointPlane getElbow(boolean isLeft)
    {
        return isLeft ? LEFT_ELBOW : RIGHT_ELBOW;
    }

    /**
     * Get the appropriate knee joint plane for a given side.
     * @param isLeft true for left leg, false for right leg
     * @return The knee joint plane
     */
    public static JointPlane getKnee(boolean isLeft)
    {
        return isLeft ? LEFT_KNEE : RIGHT_KNEE;
    }

    /**
     * Get the joint plane for a specific bone region.
     * Returns null for non-limb regions (head, body, root).
     *
     * @param region The bone region
     * @return The joint plane that separates this region from its parent, or null
     */
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

    /**
     * Create a custom elbow joint plane with adjusted position.
     * Useful for armor models with non-standard proportions.
     *
     * @param elbowY Y position of the elbow in model space (typically 4.0 for vanilla)
     * @return A new joint plane at the specified position
     */
    public static JointPlane createElbowPlane(float elbowY)
    {
        return new JointPlane(
            new Vector3f(0, elbowY * SCALE, 0),
            new Vector3f(0, -1, 0)
        );
    }

    /**
     * Create a custom knee joint plane with adjusted position.
     * Useful for armor models with non-standard proportions.
     *
     * @param kneeY Y position of the knee in model space (typically 6.0 for vanilla)
     * @return A new joint plane at the specified position
     */
    public static JointPlane createKneePlane(float kneeY)
    {
        return new JointPlane(
            new Vector3f(0, kneeY * SCALE, 0),
            new Vector3f(0, -1, 0)
        );
    }

    private JointDefinitions()
    {
        // Utility class, no instantiation
    }
}
