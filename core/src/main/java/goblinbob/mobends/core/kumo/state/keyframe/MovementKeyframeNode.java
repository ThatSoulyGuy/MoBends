package goblinbob.mobends.core.kumo.state.keyframe;

import goblinbob.mobends.lib.animation.keyframe.KeyframeAnimation;
import goblinbob.mobends.lib.data.ILivingEntityAnimationData;
import goblinbob.mobends.core.kumo.state.IKumoContext;
import goblinbob.mobends.core.kumo.state.template.IKumoInstancingContext;
import goblinbob.mobends.core.kumo.state.template.keyframe.MovementKeyframeNodeTemplate;
import org.slf4j.LoggerFactory;

public class MovementKeyframeNode extends KeyframeNode
{

    private static volatile boolean warnedAboutNonLivingTarget = false;

    public MovementKeyframeNode(IKumoInstancingContext context, MovementKeyframeNodeTemplate nodeTemplate)
    {
        this(nodeTemplate.animationKey != null ? context.getAnimation(nodeTemplate.animationKey) : null,
                nodeTemplate.startFrame,
                nodeTemplate.playbackSpeed);
    }

    public MovementKeyframeNode(KeyframeAnimation animation, int startFrame, float playbackSpeed)
    {
        super(animation, startFrame, playbackSpeed);
    }

    @Override
    public boolean isAnimationFinished()
    {
        return this.animation == null;
    }

    @Override
    public void update(IKumoContext context, float deltaTime)
    {
        if (animation == null)
        {
            return;
        }

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
