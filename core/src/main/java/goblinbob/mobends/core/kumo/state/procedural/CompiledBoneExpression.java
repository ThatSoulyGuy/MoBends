package goblinbob.mobends.core.kumo.state.procedural;

import goblinbob.mobends.core.expression.Expression;
import goblinbob.mobends.core.expression.ExpressionCache;
import goblinbob.mobends.core.expression.ExpressionContext;
import goblinbob.mobends.core.kumo.state.template.procedural.ProceduralBoneTemplate;

import javax.annotation.Nullable;

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

    public float evaluateRotationX(ExpressionContext context)
    {
        return rotationX != null ? rotationX.evaluateFloat(context) : 0.0f;
    }

    public float evaluateRotationY(ExpressionContext context)
    {
        return rotationY != null ? rotationY.evaluateFloat(context) : 0.0f;
    }

    public float evaluateRotationZ(ExpressionContext context)
    {
        return rotationZ != null ? rotationZ.evaluateFloat(context) : 0.0f;
    }

    public float evaluateOffsetX(ExpressionContext context)
    {
        return offsetX != null ? offsetX.evaluateFloat(context) : 0.0f;
    }

    public float evaluateOffsetY(ExpressionContext context)
    {
        return offsetY != null ? offsetY.evaluateFloat(context) : 0.0f;
    }

    public float evaluateOffsetZ(ExpressionContext context)
    {
        return offsetZ != null ? offsetZ.evaluateFloat(context) : 0.0f;
    }

    public boolean hasRotation()
    {
        return hasRotation;
    }

    public boolean hasOffset()
    {
        return hasOffset;
    }

    public boolean hasRotationX()
    {
        return rotationX != null;
    }

    public boolean hasRotationY()
    {
        return rotationY != null;
    }

    public boolean hasRotationZ()
    {
        return rotationZ != null;
    }
}
