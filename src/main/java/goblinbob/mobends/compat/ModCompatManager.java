package goblinbob.mobends.compat;

import goblinbob.mobends.api.animation.MoBendsAnimationControl;
import net.minecraft.world.entity.LivingEntity;

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

        ArtifactsCompat.init();

        NotEnoughAnimationsCompat.init();

        WatutCompat.init();

        EssentialCompat.init();

        ParCoolCompat.init();

        MonsterExpansionCompat.init();

        CrawlCompat.init();

        OffHandCombatCompat.init();

        WearableBackpacksCompat.init();

        UmapyoiCompat.init();

        ThirdPartyPoseCompat.init();

        registerBuiltInAnimationControl();
    }

    private static void registerBuiltInAnimationControl()
    {
        MoBendsAnimationControl.registerPoseOverride("essential", EssentialCompat::isPlayingEmote);
        MoBendsAnimationControl.registerPoseOverride("parcool", ParCoolCompat::isAnimating);
        MoBendsAnimationControl.registerPoseOverride("monsterexpansion", MonsterExpansionCompat::isAnimating);
        MoBendsAnimationControl.registerPoseOverride("crawl", CrawlCompat::isPosingModel);

        MoBendsAnimationControl.registerAnimationDeferral("physicsmod", PhysicsModCompat::hasActivePhysics);

        MoBendsAnimationControl.registerExternalAnimation("playeranimator", PlayerAnimationLibCompat::hasActiveAnimation);
    }

    public static boolean isExternallyPosed(LivingEntity entity)
    {
        return MoBendsAnimationControl.isPoseOverridden(entity);
    }

    public static boolean shouldDeferAnimation(LivingEntity entity)
    {
        return MoBendsAnimationControl.isAnimationDeferred(entity);
    }

    public static boolean hasExternalAnimation(LivingEntity entity)
    {
        return MoBendsAnimationControl.hasExternalAnimation(entity);
    }
}
