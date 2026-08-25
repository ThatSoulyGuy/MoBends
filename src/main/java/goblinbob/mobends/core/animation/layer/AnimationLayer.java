package goblinbob.mobends.core.animation.layer;

import goblinbob.mobends.core.data.EntityData;


public abstract class AnimationLayer<T extends EntityData<?>>
{
	public abstract void perform(T entityData);
}
