package goblinbob.mobends.compat;

import dev.architectury.platform.Platform;
import goblinbob.mobends.standard.mutators.BipedMutator;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.lang.reflect.Method;

public class ParCoolCompat
{
    private static final String MOD_ID = "parcool";

    private enum Api
    {
        NONE,
        MODERN,
        LEGACY
    }

    private static boolean initialized = false;
    private static Api api = Api.NONE;

    private static Method getAnimatorMethod;
    private static Method isIdleMethod;

    private static Method getAnimationMethod;
    private static Method hasAnimatorMethod;
    private static Method shouldCancelAnimationMethod;

    public static void init()
    {
        if (initialized)
        {
            return;
        }
        initialized = true;

        if (!Platform.isModLoaded(MOD_ID))
        {
            return;
        }

        try
        {
            initModernReflection();
            api = Api.MODERN;
            return;
        }
        catch (Exception ignored)
        {
        }

        try
        {
            initLegacyReflection();
            api = Api.LEGACY;
        }
        catch (Exception ignored)
        {
            api = Api.NONE;
        }
    }

    private static void initModernReflection() throws Exception
    {
        Class<?> animatorClass = Class.forName("com.alrex.parcool.client.animation.system.PlayerAnimator");

        getAnimatorMethod = animatorClass.getMethod("get", AbstractClientPlayer.class);
        isIdleMethod = animatorClass.getMethod("isIdle");
    }

    private static void initLegacyReflection() throws Exception
    {
        Class<?> animationClass = Class.forName("com.alrex.parcool.common.capability.Animation");

        getAnimationMethod = animationClass.getMethod("get", Player.class);
        hasAnimatorMethod = animationClass.getMethod("hasAnimator");
        shouldCancelAnimationMethod = animationClass.getMethod("shouldCancelAnimation", Player.class);
    }

    public static boolean isModLoaded()
    {
        if (!initialized)
        {
            init();
        }
        return api != Api.NONE;
    }

    public static boolean isAnimating(LivingEntity entity)
    {
        if (!isModLoaded())
        {
            return false;
        }

        return switch (api)
        {
            case MODERN -> isAnimatingModern(entity);
            case LEGACY -> isAnimatingLegacy(entity);
            default -> false;
        };
    }

    private static boolean isAnimatingModern(LivingEntity entity)
    {
        if (!(entity instanceof AbstractClientPlayer player))
        {
            return false;
        }

        try
        {
            Object animator = getAnimatorMethod.invoke(null, player);
            if (animator == null)
            {
                return false;
            }

            return !(Boolean) isIdleMethod.invoke(animator);
        }
        catch (Exception e)
        {
            api = Api.NONE;
            return false;
        }
    }

    private static boolean isAnimatingLegacy(LivingEntity entity)
    {
        if (!(entity instanceof Player player))
        {
            return false;
        }

        try
        {
            Object animation = getAnimationMethod.invoke(null, player);
            if (animation == null)
            {
                return false;
            }

            if (!(Boolean) hasAnimatorMethod.invoke(animation))
            {
                return false;
            }

            return !(Boolean) shouldCancelAnimationMethod.invoke(animation, player);
        }
        catch (Exception e)
        {
            api = Api.NONE;
            return false;
        }
    }

    public static void applyPose(LivingEntity entity, BipedMutator<?, ?, ?> mutator, HumanoidModel<?> vanillaModel)
    {
        if (mutator == null || !(vanillaModel instanceof PlayerModel<?>))
        {
            return;
        }

        if (!isAnimating(entity))
        {
            return;
        }

        mutator.adoptPoseFromVanillaModel(vanillaModel, null, null);
    }
}
