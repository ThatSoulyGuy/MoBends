package goblinbob.mobends.compat;

public class ModCompatManager
{
    private static boolean initialized = false;

    public static void init()
    {
        if (initialized)
        {
            return;
        }
        initialized = true;

        goblinbob.mobends.core.client.skeleton.MoBendsSkeletonProvider.register();

        PlayerAnimationLibCompat.init();

        CuriosCompat.init();

        BetterBloodOverlayCompat.init();

        PhysicsModCompat.init();

        ArmourersWorkshopCompat.init();

        FirstPersonModelCompat.init();

        CarryOnCompat.init();
    }

    public static boolean shouldDeferAnimation(net.minecraft.world.entity.LivingEntity entity)
    {
        if (PhysicsModCompat.hasActivePhysics(entity))
            return true;

        if (ThirdPartyPoseCompat.shouldYieldToHeldItem(entity))
            return true;

        return false;
    }

    public static boolean hasExternalAnimation(net.minecraft.world.entity.LivingEntity entity)
    {
        return PlayerAnimationLibCompat.hasActiveAnimation(entity);
    }
}
