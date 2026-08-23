package goblinbob.mobends.core.kumo.state;

import goblinbob.mobends.lib.data.IEntityAnimationData;

public class KumoContext implements IKumoContext
{

    public IEntityAnimationData entityData;

    public ILayerState layerState;

    public INodeState currentNode;

    @Override
    public IEntityAnimationData getEntityData()
    {
        return entityData;
    }

    @Override
    public ILayerState getLayerState()
    {
        return layerState;
    }

    @Override
    public INodeState getCurrentNode()
    {
        return currentNode;
    }

    @Override
    public void setCurrentNode(INodeState node)
    {
        currentNode = node;
    }

}
