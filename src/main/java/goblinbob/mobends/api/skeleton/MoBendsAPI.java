package goblinbob.mobends.api.skeleton;

import net.minecraft.world.entity.LivingEntity;

public final class MoBendsAPI
{
    private static ISkeletonProvider provider;

    private MoBendsAPI()
    {
    }

    public static void setProvider(ISkeletonProvider skeletonProvider)
    {
        provider = skeletonProvider;
    }

    public static boolean isAvailable()
    {
        return provider != null;
    }

    public static boolean isAnimated(LivingEntity entity)
    {
        return provider != null && provider.isAnimated(entity);
    }

    public static IAnimatedSkeleton getSkeleton(LivingEntity entity)
    {
        return provider != null ? provider.getSkeleton(entity) : null;
    }

    public static IAnimatedSkeleton getRenderingSkeleton()
    {
        return provider != null ? provider.getRenderingSkeleton() : null;
    }
}
