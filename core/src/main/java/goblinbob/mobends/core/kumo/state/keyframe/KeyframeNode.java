package goblinbob.mobends.core.kumo.state.keyframe;

import goblinbob.mobends.lib.animation.keyframe.Bone;
import goblinbob.mobends.lib.animation.keyframe.KeyframeAnimation;
import goblinbob.mobends.core.kumo.state.ConnectionState;
import goblinbob.mobends.core.kumo.state.IKumoContext;
import goblinbob.mobends.core.kumo.state.INodeState;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.kumo.state.template.keyframe.ConnectionTemplate;
import goblinbob.mobends.core.kumo.state.template.keyframe.KeyframeNodeTemplate;

import java.util.ArrayList;
import java.util.List;

public abstract class KeyframeNode implements INodeState
{

    public final KeyframeAnimation animation;
    protected int animationDuration;
    protected final int startFrame;
    protected final float playbackSpeed;
    List<ConnectionState> connections = new ArrayList<>();

    protected float progress;

    protected KeyframeNode(KeyframeAnimation animation, int startFrame, float playbackSpeed)
    {
        this.animation = animation;
        this.startFrame = startFrame;
        this.playbackSpeed = playbackSpeed;

        if (animation != null)
        {
            this.animationDuration = 0;
            for (Bone bone : animation.bones.values())
            {
                if (bone.keyframes.size() > this.animationDuration)
                    this.animationDuration = bone.keyframes.size();
            }
        }

        this.progress = this.startFrame;
    }

    @Override
    public void parseConnections(List<INodeState> nodeStates, KeyframeNodeTemplate template) throws MalformedKumoTemplateException
    {
        if (template.connections != null)
        {
            for (ConnectionTemplate connectionTemplate : template.connections)
            {
                this.connections.add(ConnectionState.createFromTemplate(nodeStates, connectionTemplate));
            }
        }
    }

    @Override
    public void start(IKumoContext context)
    {
        this.progress = this.startFrame;
        for (ConnectionState connection : connections)
        {
            connection.triggerCondition.onNodeStarted(context);
        }
    }

    @Override
    public KeyframeAnimation getAnimation()
    {
        return animation;
    }

    @Override
    public float getProgress()
    {
        return progress;
    }

    @Override
    public Iterable<ConnectionState> getConnections()
    {
        return connections;
    }

}
