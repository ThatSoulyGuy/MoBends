package goblinbob.mobends.core.kumo.state.keyframe;

import goblinbob.mobends.lib.animation.keyframe.Bone;
import goblinbob.mobends.lib.animation.keyframe.KeyframeAnimation;
import goblinbob.mobends.core.kumo.state.ConnectionState;
import goblinbob.mobends.core.kumo.state.IKumoContext;
import goblinbob.mobends.core.kumo.state.template.IKumoInstancingContext;
import goblinbob.mobends.core.kumo.state.INodeState;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.kumo.state.template.keyframe.ConnectionTemplate;
import goblinbob.mobends.core.kumo.state.template.keyframe.KeyframeNodeTemplate;
import goblinbob.mobends.core.kumo.state.template.keyframe.StandardKeyframeNodeTemplate;

import java.util.ArrayList;
import java.util.List;

public class StandardKeyframeNode implements INodeState
{

    public final KeyframeAnimation animation;
    private int animationDuration;
    private final int startFrame;
    private final float playbackSpeed;
    private final boolean looping;
    List<ConnectionState> connections = new ArrayList<>();

    private float progress;

    public StandardKeyframeNode(IKumoInstancingContext context, StandardKeyframeNodeTemplate nodeTemplate)
    {
        this(nodeTemplate.animationKey != null ? context.getAnimation(nodeTemplate.animationKey) : null,
                nodeTemplate.startFrame,
                nodeTemplate.playbackSpeed,
                nodeTemplate.looping);
    }

    public StandardKeyframeNode(KeyframeAnimation animation, int startFrame, float playbackSpeed, boolean looping)
    {
        this.animation = animation;
        this.startFrame = startFrame;
        this.playbackSpeed = playbackSpeed;
        this.looping = looping;

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
    public void update(IKumoContext context, float deltaTime)
    {
        if (animation != null)
        {
            if (this.looping)
            {
                // A single-keyframe animation has a zero-length loop, and subtracting zero
                // forever would hang the client with no crash log. Nothing to advance anyway.
                if (this.animationDuration <= 1)
                {
                    return;
                }

                this.progress += this.playbackSpeed * deltaTime;

                while (this.progress >= this.animationDuration - 1)
                {
                    this.progress -= this.animationDuration - 1;
                }
            }
            else
            {
                // Clamps at the LAST keyframe (duration - 1), not one short of it. The previous
                // bound of duration - 2 existed so that frameB = frameA + 1 stayed in range;
                // KeyframeLayerState.frameAt now clamps past-the-end reads to the final keyframe
                // instead, so the animation holds its authored final pose rather than freezing a
                // frame early.
                final int lastFrame = this.animationDuration - 1;

                if (this.progress < lastFrame)
                {
                    this.progress = Math.min(this.progress + this.playbackSpeed * deltaTime, lastFrame);
                }
            }
        }
    }

    @Override
    public KeyframeAnimation getAnimation()
    {
        return animation;
    }

    @Override
    public boolean isAnimationFinished()
    {
        // Must match the clamp in update(), or the node either never reports finished (soft-
        // locking any state machine waiting on core:animation_finished) or reports finished a
        // frame before it actually stops moving.
        return this.animation == null || !this.looping && this.progress >= animationDuration - 1;
    }

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
