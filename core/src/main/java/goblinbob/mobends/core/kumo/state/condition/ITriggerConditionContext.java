package goblinbob.mobends.core.kumo.state.condition;

import goblinbob.mobends.lib.data.IEntityAnimationData;
import goblinbob.mobends.core.kumo.state.ILayerState;
import goblinbob.mobends.core.kumo.state.INodeState;

public interface ITriggerConditionContext
{

    IEntityAnimationData getEntityData();

    ILayerState getLayerState();

    INodeState getCurrentNode();

}
