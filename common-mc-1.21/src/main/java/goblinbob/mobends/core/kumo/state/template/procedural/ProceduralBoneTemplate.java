package goblinbob.mobends.core.kumo.state.template.procedural;

/**
 * Template for a single bone's procedural animation expressions.
 * All rotation values are in degrees, all offset values are in units.
 */
public class ProceduralBoneTemplate
{
    /**
     * Expression for X rotation (degrees).
     * Example: "sin(ticks * 0.1) * 30"
     */
    public String rotationX;

    /**
     * Expression for Y rotation (degrees).
     * Example: "headYaw"
     */
    public String rotationY;

    /**
     * Expression for Z rotation (degrees).
     * Example: "cos(limbSwing) * limbSwingAmount * 5"
     */
    public String rotationZ;

    /**
     * Expression for X offset (units).
     * Example: "sin(ticks * 0.2) * 0.5"
     */
    public String offsetX;

    /**
     * Expression for Y offset (units).
     * Example: "-abs(sin(limbSwing)) * 0.2"
     */
    public String offsetY;

    /**
     * Expression for Z offset (units).
     * Example: "forwardMomentum * 0.1"
     */
    public String offsetZ;

    /**
     * Checks if this template has any rotation expressions.
     */
    public boolean hasRotation()
    {
        return rotationX != null || rotationY != null || rotationZ != null;
    }

    /**
     * Checks if this template has any offset expressions.
     */
    public boolean hasOffset()
    {
        return offsetX != null || offsetY != null || offsetZ != null;
    }

    /**
     * Checks if this template has any expressions at all.
     */
    public boolean hasAnyExpression()
    {
        return hasRotation() || hasOffset();
    }
}
