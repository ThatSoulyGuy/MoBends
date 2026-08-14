package goblinbob.mobends.core.client.skeleton;

import goblinbob.mobends.api.skeleton.IAnimatedSkeleton;
import goblinbob.mobends.api.skeleton.ISkeletonProvider;
import goblinbob.mobends.api.skeleton.MoBendsAPI;
import goblinbob.mobends.core.bender.EntityBender;
import goblinbob.mobends.core.bender.EntityBenderRegistry;
import goblinbob.mobends.core.client.MoBendsRenderContext;
import goblinbob.mobends.core.util.BenderHelper;
import goblinbob.mobends.standard.mutators.BipedMutator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
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

        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.getEntityRenderDispatcher() == null)
        {
            return null;
        }

        final EntityRenderer<?> renderer = minecraft.getEntityRenderDispatcher().getRenderer(entity);
        if (!(renderer instanceof LivingEntityRenderer<?, ?> livingEntityRenderer))
        {
            return null;
        }

        final EntityBender bender = EntityBenderRegistry.instance.getForEntity(entity);
        if (bender == null)
        {
            return null;
        }

        final Object mutator = bender.getMutator(livingEntityRenderer);
        if (mutator instanceof BipedMutator<?, ?, ?> bipedMutator)
        {
            return new BipedSkeleton(bipedMutator);
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
        return new BipedSkeleton(mutator);
    }
}
