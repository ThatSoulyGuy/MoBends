package goblinbob.mobends.core.kumo.state.condition;

import goblinbob.mobends.lib.time.ITickSource;
import goblinbob.mobends.core.kumo.state.template.TriggerConditionTemplate;

public class TicksPassedCondition implements ITriggerCondition
{

    private final float ticksToPass;
    private float ticksOnStart;

    public TicksPassedCondition(Template template)
    {
        this.ticksToPass = template.ticksToPass;
    }

    @Override
    public void onNodeStarted(ITriggerConditionContext context)
    {
        this.ticksOnStart = ITickSource.Holder.getTicks();
    }

    @Override
    public boolean isConditionMet(ITriggerConditionContext context)
    {
        return ITickSource.Holder.getTicks() > this.ticksOnStart + this.ticksToPass;
    }

    public static class Template extends TriggerConditionTemplate
    {

        public int ticksToPass;

    }

}
