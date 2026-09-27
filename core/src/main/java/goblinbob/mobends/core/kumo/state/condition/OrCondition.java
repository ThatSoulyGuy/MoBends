package goblinbob.mobends.core.kumo.state.condition;

import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.kumo.state.template.TriggerConditionTemplate;

import java.util.List;

public class OrCondition implements ITriggerCondition
{

    private List<ITriggerCondition> conditions;

    public OrCondition(Template template) throws MalformedKumoTemplateException
    {
        this.conditions = TriggerConditionRegistry.instance.createAll(template.conditions);
    }

    @Override
    public boolean isConditionMet(ITriggerConditionContext context) throws MalformedKumoTemplateException
    {
        for (ITriggerCondition condition : this.conditions)
        {
            if (condition.isConditionMet(context))
            {
                return true;
            }
        }
        return false;
    }

    public static class Template extends TriggerConditionTemplate
    {

        public List<TriggerConditionTemplate> conditions;

    }

}
