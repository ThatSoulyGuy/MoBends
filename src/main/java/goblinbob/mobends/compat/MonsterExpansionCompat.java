package goblinbob.mobends.compat;

import dev.architectury.platform.Platform;
import goblinbob.mobends.standard.mutators.BipedMutator;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.LivingEntity;

import java.lang.reflect.Method;

public class MonsterExpansionCompat
{
    private static final String MOD_ID = "monsterexpansion";

    private static boolean initialized = false;
    private static boolean isLoaded = false;

    private static Object actionAnimatorCapability;
    private static Method getCapabilityMethod;
    private static Method orElseMethod;
    private static Method isPlayingMethod;

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
            initReflection();
            isLoaded = true;
        }
        catch (Throwable e)
        {
            isLoaded = false;
        }
    }

    private static void initReflection() throws Exception
    {
        final Class<?> capabilityClass = Class.forName("net.minecraftforge.common.capabilities.Capability");
        final Class<?> providerClass = Class.forName("net.minecraftforge.common.capabilities.ICapabilityProvider");
        final Class<?> lazyOptionalClass = Class.forName("net.minecraftforge.common.util.LazyOptional");
        final Class<?> capabilitiesClass = Class.forName("net.saksolm.monsterexpansion.capabilities.ModCapabilities");
        final Class<?> animatorClass = Class.forName("net.saksolm.monsterexpansion.capabilities.IActionAnimator");

        actionAnimatorCapability = capabilitiesClass.getField("ACTION_ANIMATOR").get(null);

        if (actionAnimatorCapability == null)
        {
            throw new IllegalStateException("ModCapabilities.ACTION_ANIMATOR is null");
        }

        getCapabilityMethod = providerClass.getMethod("getCapability", capabilityClass);
        orElseMethod = lazyOptionalClass.getMethod("orElse", Object.class);
        isPlayingMethod = animatorClass.getMethod("isPlaying");
    }

    public static boolean isModLoaded()
    {
        if (!initialized)
        {
            init();
        }
        return isLoaded;
    }

    public static boolean isAnimating(LivingEntity entity)
    {
        if (!isModLoaded() || !(entity instanceof AbstractClientPlayer player))
        {
            return false;
        }

        try
        {
            final Object lazyOptional = getCapabilityMethod.invoke(player, actionAnimatorCapability);
            if (lazyOptional == null)
            {
                return false;
            }

            final Object animator = orElseMethod.invoke(lazyOptional, new Object[]{null});
            if (animator == null)
            {
                return false;
            }

            return (Boolean) isPlayingMethod.invoke(animator);
        }
        catch (Exception e)
        {
            isLoaded = false;
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
