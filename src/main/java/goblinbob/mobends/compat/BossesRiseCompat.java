package goblinbob.mobends.compat;

import dev.architectury.platform.Platform;
import goblinbob.mobends.standard.mutators.BipedMutator;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.lang.reflect.Method;
import java.util.Optional;

public class BossesRiseCompat
{
    private static final String MOD_ID = "block_factorys_bosses";

    private static boolean initialized = false;
    private static boolean isLoaded = false;

    private static Method fromPlayerMethod;
    private static Method getCurrentAnimationMethod;

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
        final Class<?> animationCapClass = Class.forName(
                "net.unusual.block_factorys_bosses.capability.entity.PlayerAnimationCap");

        fromPlayerMethod = animationCapClass.getMethod("fromPlayer", Player.class);
        getCurrentAnimationMethod = animationCapClass.getMethod("getCurrentAnimation");
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
        if (!isModLoaded() || !(entity instanceof Player player) || isSkippedInFirstPerson(player))
        {
            return false;
        }

        try
        {
            if (!(fromPlayerMethod.invoke(null, player) instanceof Optional<?> capability))
            {
                return false;
            }

            final Object animator = capability.orElse(null);
            return animator != null && getCurrentAnimationMethod.invoke(animator) != null;
        }
        catch (Exception e)
        {
            isLoaded = false;
            return false;
        }
    }

    private static boolean isSkippedInFirstPerson(Player player)
    {
        final Minecraft minecraft = Minecraft.getInstance();
        return player == minecraft.player
                && minecraft.options.getCameraType() == CameraType.FIRST_PERSON;
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
