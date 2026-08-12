package goblinbob.mobends.core.kumo.state.template;

import goblinbob.mobends.lib.animation.keyframe.KeyframeAnimation;

public interface IKumoInstancingContext
{

    KeyframeAnimation getAnimation(String key);

}
