package goblinbob.mobends.api.skeleton;

import net.minecraft.world.entity.LivingEntity;

public interface ISkeletonProvider
{
    boolean isAnimated(LivingEntity entity);

    IAnimatedSkeleton getSkeleton(LivingEntity entity);

    IAnimatedSkeleton getRenderingSkeleton();
}
