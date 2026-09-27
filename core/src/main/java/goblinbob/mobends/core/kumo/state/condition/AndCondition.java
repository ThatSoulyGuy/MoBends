package goblinbob.mobends.core.kumo.state.condition;

import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.kumo.state.template.TriggerConditionTemplate;

import java.util.List;

public class AndCondition implements ITriggerCondition
{

    private List<ITriggerCondition> conditions;

    public AndCondition(Template template) throws MalformedKumoTemplateException
    {
        this.conditions = TriggerConditionRegistry.instance.createAll(template.conditions);
    }

    @Override
    public boolean isConditionMet(ITriggerConditionContext context) throws MalformedKumoTemplateException
    {
        for (ITriggerCondition condition : this.conditions)
        {
            if (!condition.isConditionMet(context))
            {
                return false;
            }
        }
        return true;
    }

    public static class Template extends TriggerConditionTemplate
    {

        public List<TriggerConditionTemplate> conditions;

    }

}
