package goblinbob.mobends.api.event;

import goblinbob.mobends.api.skeleton.IAnimatedSkeleton;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;

@FunctionalInterface
public interface IPoseListener
{
    void onPosed(LivingEntity entity, @Nullable IAnimatedSkeleton skeleton, float partialTicks);
}
