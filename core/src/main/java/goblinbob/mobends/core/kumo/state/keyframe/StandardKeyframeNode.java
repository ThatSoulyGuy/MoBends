package goblinbob.mobends.core.kumo.state.keyframe;

import goblinbob.mobends.lib.animation.keyframe.KeyframeAnimation;
import goblinbob.mobends.core.kumo.state.IKumoContext;
import goblinbob.mobends.core.kumo.state.template.IKumoInstancingContext;
import goblinbob.mobends.core.kumo.state.template.keyframe.StandardKeyframeNodeTemplate;

public class StandardKeyframeNode extends KeyframeNode
{

    private final boolean looping;

    public StandardKeyframeNode(IKumoInstancingContext context, StandardKeyframeNodeTemplate nodeTemplate)
    {
        this(nodeTemplate.animationKey != null ? context.getAnimation(nodeTemplate.animationKey) : null,
                nodeTemplate.startFrame,
                nodeTemplate.playbackSpeed,
                nodeTemplate.looping);
    }

    public StandardKeyframeNode(KeyframeAnimation animation, int startFrame, float playbackSpeed, boolean looping)
    {
        super(animation, startFrame, playbackSpeed);
        this.looping = looping;
    }

    @Override
    public void update(IKumoContext context, float deltaTime)
    {
        if (animation != null)
        {
            if (this.looping)
            {
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
                final int lastFrame = this.animationDuration - 1;

                if (this.progress < lastFrame)
                {
                    this.progress = Math.min(this.progress + this.playbackSpeed * deltaTime, lastFrame);
                }
            }
        }
    }

    @Override
    public boolean isAnimationFinished()
    {
        return this.animation == null || !this.looping && this.progress >= animationDuration - 1;
    }

}
