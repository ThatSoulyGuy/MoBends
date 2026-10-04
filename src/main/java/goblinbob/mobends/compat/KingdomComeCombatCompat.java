package goblinbob.mobends.compat;

import dev.architectury.platform.Platform;
import goblinbob.mobends.standard.mutators.BipedMutator;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.lang.reflect.Method;

public final class KingdomComeCombatCompat
{
    private static boolean initialized = false;
    private static Method hasActiveCombatLayerMethod;

    private KingdomComeCombatCompat()
    {
    }

    private static void init()
    {
        initialized = true;

        if (!Platform.isModLoaded("kingdom_come_combat"))
        {
            return;
        }

        try
        {
            hasActiveCombatLayerMethod = Class.forName(
                            "com.kingdomcomecombat.client.animation.ClientEntityGeckoAnimationState")
                    .getMethod("hasActiveCombatLayer", int.class);
        }
        catch (Throwable e)
        {
            hasActiveCombatLayerMethod = null;
        }
    }

    public static void applyMobPose(LivingEntity entity, BipedMutator<?, ?, ?> mutator, HumanoidModel<?> vanillaModel)
    {
        if (!initialized)
        {
            init();
        }

        if (hasActiveCombatLayerMethod == null || entity == null || entity instanceof Player
                || mutator == null || vanillaModel == null)
        {
            return;
        }

        try
        {
            if (Boolean.TRUE.equals(hasActiveCombatLayerMethod.invoke(null, entity.getId())))
            {
                mutator.adoptPoseFromVanillaModel(vanillaModel, null, null);
            }
        }
        catch (Throwable ignored)
        {
        }
    }
}
