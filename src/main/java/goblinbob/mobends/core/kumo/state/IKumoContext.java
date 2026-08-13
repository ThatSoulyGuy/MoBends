package goblinbob.mobends.core.kumo.state;

import goblinbob.mobends.core.kumo.state.condition.ITriggerConditionContext;

public interface IKumoContext extends ITriggerConditionContext
{

    void setCurrentNode(INodeState node);

}
