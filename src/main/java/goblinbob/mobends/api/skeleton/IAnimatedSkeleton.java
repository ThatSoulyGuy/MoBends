package goblinbob.mobends.api.skeleton;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

import javax.annotation.Nullable;

public interface IAnimatedSkeleton
{
    @Nullable
    IBoneTransform getBone(MoBendsBone bone);

    boolean hasBone(MoBendsBone bone);

    @Nullable
    default LivingEntity getEntity()
    {
        return null;
    }

    @Nullable
    default Quaternionf getBoneRotation(MoBendsBone bone)
    {
        return null;
    }

    @Nullable
    default Vec3 getBoneModelPosition(MoBendsBone bone)
    {
        return null;
    }

    @Nullable
    default Vec3 getBoneWorldPosition(MoBendsBone bone, float partialTicks)
    {
        return null;
    }
}
