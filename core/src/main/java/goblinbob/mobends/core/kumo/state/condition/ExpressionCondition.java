package goblinbob.mobends.core.kumo.state.condition;

import goblinbob.mobends.core.expression.Expression;
import goblinbob.mobends.core.expression.ExpressionCache;
import goblinbob.mobends.core.expression.ExpressionException;
import goblinbob.mobends.core.kumo.KumoExpressionContext;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.kumo.state.template.TriggerConditionTemplate;

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

    public String getExpressionSource()
    {
        return expressionSource;
    }

    public static class Template extends TriggerConditionTemplate
    {
        public String expression;
    }
}
