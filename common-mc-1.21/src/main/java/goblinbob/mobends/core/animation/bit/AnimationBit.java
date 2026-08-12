package goblinbob.mobends.core.animation.bit;

import goblinbob.mobends.core.animation.layer.AnimationLayer;
import goblinbob.mobends.core.data.EntityData;

public abstract class AnimationBit<T extends EntityData<?>>
{
    protected AnimationLayer<? extends T> layer;

    public void setupForPlay(AnimationLayer<? extends T> layer, T entityData)
    {
        this.layer = layer;
        this.onPlay(entityData);
    }

    public String[] getActions(T entityData) { return new String[] {}; }

    public void onPlay(T entityData) {}

    public abstract void perform(T entityData);
}
