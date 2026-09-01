package goblinbob.mobends.core.animation.controller;

import goblinbob.mobends.core.data.EntityData;

import javax.annotation.Nullable;

public interface IAnimationController<T extends EntityData<?>>
{

    @Nullable
    void perform(T entityData);

}
