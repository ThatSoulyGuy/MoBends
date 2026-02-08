package goblinbob.mobends.core.kumo.state.procedural;

import goblinbob.mobends.core.expression.Expression;
import goblinbob.mobends.core.expression.ExpressionCache;
import goblinbob.mobends.core.expression.ExpressionContext;
import goblinbob.mobends.core.kumo.state.template.procedural.ProceduralBoneTemplate;

import javax.annotation.Nullable;

/**
 * Holds compiled expressions for a single bone's procedural transforms.
 */
public class CompiledBoneExpression
{
    @Nullable
    private final Expression rotationX;
    @Nullable
    private final Expression rotationY;
    @Nullable
    private final Expression rotationZ;
    @Nullable
    private final Expression offsetX;
    @Nullable
    private final Expression offsetY;
    @Nullable
    private final Expression offsetZ;

    private final boolean hasRotation;
    private final boolean hasOffset;

    public CompiledBoneExpression(ProceduralBoneTemplate template)
    {
        ExpressionCache cache = ExpressionCache.getInstance();

        this.rotationX = template.rotationX != null ? cache.get(template.rotationX) : null;
        this.rotationY = template.rotationY != null ? cache.get(template.rotationY) : null;
        this.rotationZ = template.rotationZ != null ? cache.get(template.rotationZ) : null;
        this.offsetX = template.offsetX != null ? cache.get(template.offsetX) : null;
        this.offsetY = template.offsetY != null ? cache.get(template.offsetY) : null;
        this.offsetZ = template.offsetZ != null ? cache.get(template.offsetZ) : null;

        this.hasRotation = rotationX != null || rotationY != null || rotationZ != null;
        this.hasOffset = offsetX != null || offsetY != null || offsetZ != null;
    }

    /**
     * Evaluates the rotation X expression.
     * @return Rotation in degrees, or 0 if no expression defined.
     */
    public float evaluateRotationX(ExpressionContext context)
    {
        return rotationX != null ? rotationX.evaluateFloat(context) : 0.0f;
    }

    /**
     * Evaluates the rotation Y expression.
     * @return Rotation in degrees, or 0 if no expression defined.
     */
    public float evaluateRotationY(ExpressionContext context)
    {
        return rotationY != null ? rotationY.evaluateFloat(context) : 0.0f;
    }

    /**
     * Evaluates the rotation Z expression.
     * @return Rotation in degrees, or 0 if no expression defined.
     */
    public float evaluateRotationZ(ExpressionContext context)
    {
        return rotationZ != null ? rotationZ.evaluateFloat(context) : 0.0f;
    }

    /**
     * Evaluates the offset X expression.
     * @return Offset in units, or 0 if no expression defined.
     */
    public float evaluateOffsetX(ExpressionContext context)
    {
        return offsetX != null ? offsetX.evaluateFloat(context) : 0.0f;
    }

    /**
     * Evaluates the offset Y expression.
     * @return Offset in units, or 0 if no expression defined.
     */
    public float evaluateOffsetY(ExpressionContext context)
    {
        return offsetY != null ? offsetY.evaluateFloat(context) : 0.0f;
    }

    /**
     * Evaluates the offset Z expression.
     * @return Offset in units, or 0 if no expression defined.
     */
    public float evaluateOffsetZ(ExpressionContext context)
    {
        return offsetZ != null ? offsetZ.evaluateFloat(context) : 0.0f;
    }

    /**
     * Checks if this bone has any rotation expressions.
     */
    public boolean hasRotation()
    {
        return hasRotation;
    }

    /**
     * Checks if this bone has any offset expressions.
     */
    public boolean hasOffset()
    {
        return hasOffset;
    }

    /**
     * Checks if rotation X is defined.
     */
    public boolean hasRotationX()
    {
        return rotationX != null;
    }

    /**
     * Checks if rotation Y is defined.
     */
    public boolean hasRotationY()
    {
        return rotationY != null;
    }

    /**
     * Checks if rotation Z is defined.
     */
    public boolean hasRotationZ()
    {
        return rotationZ != null;
    }
}
