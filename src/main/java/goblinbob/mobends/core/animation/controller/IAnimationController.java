package goblinbob.mobends.core.animation.controller;

import goblinbob.mobends.core.data.EntityData;

import javax.annotation.Nullable;
import java.util.Collection;

public interface IAnimationController<T extends EntityData<?>>
{

    @Nullable
    void perform(T entityData);

}
