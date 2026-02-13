package goblinbob.mobends.core.kumo.state.condition;

import goblinbob.mobends.core.expression.Expression;
import goblinbob.mobends.core.expression.ExpressionCache;
import goblinbob.mobends.core.expression.ExpressionException;
import goblinbob.mobends.core.kumo.KumoExpressionContext;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.kumo.state.template.TriggerConditionTemplate;

/**
 * A trigger condition that evaluates a mathematical expression.
 * The expression can use any variables available in KumoExpressionContext.
 *
 * Example usage in JSON:
 * {
 *   "type": "core:expression",
 *   "expression": "ticksAfterPunch < 10 && onGround"
 * }
 *
 * @author MoBends
 */
public class ExpressionCondition implements ITriggerCondition
{
    private final Expression expression;
    private final String expressionSource;

    public ExpressionCondition(Template template) throws MalformedKumoTemplateException
    {
        if (template.expression == null || template.expression.isBlank())
        {
            throw new MalformedKumoTemplateException("No 'expression' property given for expression condition.");
        }

        this.expressionSource = template.expression;

        try
        {
            this.expression = ExpressionCache.getInstance().get(template.expression);
        }
        catch (ExpressionException e)
        {
            throw new MalformedKumoTemplateException("Failed to parse expression: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean isConditionMet(ITriggerConditionContext context)
    {
        KumoExpressionContext expressionContext = new KumoExpressionContext(context);
        return expression.evaluateBoolean(expressionContext);
    }

    /**
     * Returns the source expression string.
     */
    public String getExpressionSource()
    {
        return expressionSource;
    }

    public static class Template extends TriggerConditionTemplate
    {
        /**
         * The expression to evaluate. The condition is met when the expression
         * evaluates to a non-zero value.
         *
         * Available variables:
         * - Time: ticks, partialTicks
         * - Constants: PI, E
         * - Motion: motionX/Y/Z, motionMagnitude, xzMotionMagnitude
         * - State: onGround, isStill, isStrafing, isUnderwater
         * - Health: health, maxHealth, healthPercent
         * - Timing: ticksInAir, ticksAfterTouchdown, ticksAfterPunch, ticksFalling
         * - Animation: limbSwing, limbSwingAmount, swingProgress
         * - Head: headYaw, headPitch
         * - Climbing: isClimbing, climbingCycle
         * - Node: nodeProgress, nodeAnimationProgress, nodeAnimationFinished
         *
         * Available operators: +, -, *, /, %, ^, <, >, <=, >=, ==, !=, &&, ||, !
         * Available functions: sin, cos, tan, abs, min, max, clamp, lerp, etc.
         *
         * Example: "ticksAfterPunch < 10 && onGround"
         */
        public String expression;
    }
}
