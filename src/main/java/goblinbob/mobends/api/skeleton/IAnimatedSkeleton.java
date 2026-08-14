package goblinbob.mobends.api.skeleton;

public interface IAnimatedSkeleton
{
    IBoneTransform getBone(MoBendsBone bone);

    boolean hasBone(MoBendsBone bone);
}
