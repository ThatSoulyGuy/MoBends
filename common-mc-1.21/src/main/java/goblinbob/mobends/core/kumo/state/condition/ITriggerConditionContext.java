package goblinbob.mobends.core.kumo.state.condition;

import goblinbob.mobends.core.data.EntityData;
import goblinbob.mobends.core.kumo.state.ILayerState;
import goblinbob.mobends.core.kumo.state.INodeState;

public interface ITriggerConditionContext
{

    EntityData<?> getEntityData();

    ILayerState getLayerState();

    INodeState getCurrentNode();

}
