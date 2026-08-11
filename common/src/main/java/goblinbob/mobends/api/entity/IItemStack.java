package goblinbob.mobends.api.entity;

import goblinbob.mobends.api.resource.IResourcePath;

import javax.annotation.Nullable;

public interface IItemStack
{
    boolean isEmpty();

    int getCount();

    @Nullable
    IResourcePath getItemId();

    boolean isTool();

    boolean isBow();

    boolean isCrossbow();

    boolean isShield();

    boolean isFood();

    boolean isTrident();

    boolean isSpyglass();

    Object getNative();
}
