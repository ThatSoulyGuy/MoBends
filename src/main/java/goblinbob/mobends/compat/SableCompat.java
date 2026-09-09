package goblinbob.mobends.compat;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.lang.reflect.Method;

public class SableCompat
{
    private static boolean initialized = false;
    private static boolean isLoaded = false;

    private static Object companion;
    private static Method getTrackingSubLevel;
    private static Method logicalPose;
    private static Method lastPose;
    private static Method transformPositionInverse;

    public static void init()
    {
        if (initialized)
        {
            return;
        }
        initialized = true;

        try
        {
            Class<?> companionClass = Class.forName("dev.ryanhcode.sable.companion.SableCompanion");
            Class<?> subLevelClass = Class.forName("dev.ryanhcode.sable.companion.SubLevelAccess");
            Class<?> poseClass = Class.forName("dev.ryanhcode.sable.companion.math.Pose3dc");

            companion = companionClass.getField("INSTANCE").get(null);
            getTrackingSubLevel = companionClass.getMethod("getTrackingSubLevel", Entity.class);
            logicalPose = subLevelClass.getMethod("logicalPose");
            lastPose = subLevelClass.getMethod("lastPose");
            transformPositionInverse = poseClass.getMethod("transformPositionInverse", Vec3.class);

            isLoaded = companion != null;
        }
        catch (Throwable ignored)
        {
        }
    }

    public static boolean isModLoaded()
    {
        if (!initialized)
        {
            init();
        }
        return isLoaded;
    }

    @Nullable
    private static Object trackingSubLevelOf(Entity entity)
    {
        if (entity == null || !isModLoaded())
        {
            return null;
        }

        try
        {
            return getTrackingSubLevel.invoke(companion, entity);
        }
        catch (Throwable ignored)
        {
            return null;
        }
    }

    public static boolean isStandingOnSubLevel(Entity entity)
    {
        return entity != null && entity.onGround() && trackingSubLevelOf(entity) != null;
    }

    @Nullable
    public static Vec3 getSubLevelLocalMotion(Entity entity, double previousX, double previousY, double previousZ)
    {
        final Object subLevel = trackingSubLevelOf(entity);

        if (subLevel == null)
        {
            return null;
        }

        try
        {
            final Object currentPose = logicalPose.invoke(subLevel);
            final Object previousPose = lastPose.invoke(subLevel);

            if (currentPose == null || previousPose == null)
            {
                return null;
            }

            final Vec3 current = (Vec3) transformPositionInverse.invoke(currentPose, entity.position());
            final Vec3 previous = (Vec3) transformPositionInverse.invoke(previousPose,
                    new Vec3(previousX, previousY, previousZ));

            return current.subtract(previous);
        }
        catch (Throwable ignored)
        {
            return null;
        }
    }
}
