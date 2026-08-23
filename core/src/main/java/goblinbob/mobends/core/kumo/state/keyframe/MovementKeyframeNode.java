package goblinbob.mobends.core.kumo.state.keyframe;

import goblinbob.mobends.lib.animation.keyframe.Bone;
import goblinbob.mobends.lib.animation.keyframe.KeyframeAnimation;
import goblinbob.mobends.lib.data.ILivingEntityAnimationData;
import goblinbob.mobends.core.kumo.state.ConnectionState;
import goblinbob.mobends.core.kumo.state.IKumoContext;
import goblinbob.mobends.core.kumo.state.template.IKumoInstancingContext;
import goblinbob.mobends.core.kumo.state.INodeState;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.kumo.state.template.keyframe.ConnectionTemplate;
import goblinbob.mobends.core.kumo.state.template.keyframe.KeyframeNodeTemplate;
import goblinbob.mobends.core.kumo.state.template.keyframe.MovementKeyframeNodeTemplate;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class MovementKeyframeNode implements INodeState
{

    public final KeyframeAnimation animation;
    private int animationDuration;
    private final int startFrame;
    private final float playbackSpeed;
    List<ConnectionState> connections = new ArrayList<>();

    private float progress;

    /** One warning per JVM, not one per frame per entity. */
    private static volatile boolean warnedAboutNonLivingTarget = false;

    public MovementKeyframeNode(IKumoInstancingContext context, MovementKeyframeNodeTemplate nodeTemplate)
    {
        this(nodeTemplate.animationKey != null ? context.getAnimation(nodeTemplate.animationKey) : null,
                nodeTemplate.startFrame,
                nodeTemplate.playbackSpeed);
    }

    public MovementKeyframeNode(KeyframeAnimation animation, int startFrame, float playbackSpeed)
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
    public Iterable<ConnectionState> getConnections()
    {
        return connections;
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

    /**
     * A movement node has no natural end, so this is only ever true when there is nothing to play.
     *
     * <p>Progress here is derived entirely from the entity's limb swing rather than from elapsed
     * time — the animation is a loop indexed by how far the legs have travelled — so there is no
     * final frame to arrive at. A {@code core:animation_finished} transition out of a
     * {@code core:movement} node therefore never fires, and that is correct rather than an
     * oversight: author the exit with a state condition instead.
     *
     * <p>The null case matches {@code StandardKeyframeNode}, so a node whose animation failed to
     * resolve reports finished and lets a waiting state machine move on rather than stalling.
     */
    @Override
    public boolean isAnimationFinished()
    {
        return this.animation == null;
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
    public void update(IKumoContext context, float deltaTime)
    {
        if (animation == null)
        {
            return;
        }

        // Limb swing only exists on a living entity. This used to be an unchecked cast, which
        // would have thrown from inside the render loop where nothing catches it. Freezing the
        // node is the milder failure: the animation holds its current frame instead of taking the
        // client down, and the warning says why.
        if (!(context.getEntityData() instanceof ILivingEntityAnimationData data))
        {
            if (!warnedAboutNonLivingTarget)
            {
                warnedAboutNonLivingTarget = true;
                LoggerFactory.getLogger(MovementKeyframeNode.class).warn(
                        "A core:movement node is animating a non-living entity, which has no limb "
                                + "swing to drive it. The node will hold its current frame. Use a "
                                + "core:standard node for non-living entities.");
            }
            return;
        }

        final float limbSwing = data.getLimbSwing() * 0.6662F;
        final float span = this.animationDuration - 1;

        if (span > 0)
        {
            this.progress = (this.playbackSpeed * limbSwing) % span;
            if (this.progress < 0)
            {
                this.progress += span;
            }
        }
        else
        {
            this.progress = 0;
        }
    }

}
