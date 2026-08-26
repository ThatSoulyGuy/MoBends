package goblinbob.mobends.core.client.skeleton;

import goblinbob.mobends.api.skeleton.IAnimatedSkeleton;
import goblinbob.mobends.api.skeleton.ISkeletonProvider;
import goblinbob.mobends.api.skeleton.MoBendsAPI;
import goblinbob.mobends.core.bender.EntityBender;
import goblinbob.mobends.core.bender.EntityBenderRegistry;
import goblinbob.mobends.core.client.MoBendsRenderContext;
import goblinbob.mobends.core.data.EntityDatabase;
import goblinbob.mobends.core.data.LivingEntityData;
import goblinbob.mobends.core.util.BenderHelper;
import goblinbob.mobends.standard.data.BipedEntityData;
import goblinbob.mobends.standard.mutators.BipedMutator;
import net.minecraft.world.entity.LivingEntity;

public class MoBendsSkeletonProvider implements ISkeletonProvider
{
    private static boolean registered = false;

    public static void register()
    {
        if (registered)
        {
            return;
        }
        registered = true;
        MoBendsAPI.setProvider(new MoBendsSkeletonProvider());
    }

    @Override
    public boolean isAnimated(LivingEntity entity)
    {
        if (entity == null)
        {
            return false;
        }
        final EntityBender bender = EntityBenderRegistry.instance.getForEntity(entity);
        return bender != null && bender.isAnimated() && BenderHelper.isEntityAnimated(entity);
    }

    @Override
    public IAnimatedSkeleton getSkeleton(LivingEntity entity)
    {
        if (!isAnimated(entity))
        {
            return null;
        }

        final LivingEntityData<?> data = EntityDatabase.instance.get(entity);
        if (data instanceof BipedEntityData<?> bipedData)
        {
            return BipedSkeleton.of(bipedData);
        }
        return null;
    }

    @Override
    public IAnimatedSkeleton getRenderingSkeleton()
    {
        final BipedMutator<?, ?, ?> mutator = MoBendsRenderContext.getCurrentBipedMutator();
        if (mutator == null)
        {
            return null;
        }
        return BipedSkeleton.of(mutator, MoBendsRenderContext.getCurrentEntity());
    }
}
