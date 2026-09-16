package goblinbob.mobends.compat;

import goblinbob.mobends.api.animation.MoBendsAnimationControl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;

import java.util.Map;

public final class ThirdPartyPoseCompat
{
    private static final String[] DEFAULT_SELF_POSING_MODS = {
            "tacz",
            "cgm",
            "cgs",
            "gunscraft",
            "greenboys_legendary_guns",
            "apexguns",
            "pointblank",
            "superbwarfare",
            "jeg",
            "mteg",
            "stabxmodernguns",
            "scguns",
            "lrtactical"
    };

    private static final String[] NO_ITEM_TYPES = {};

    private static final Map<String, String[]> POSING_ITEM_TYPES = Map.of(
            "tacz", new String[]{"com.tacz.guns.api.item.IGun"},
            "cgm", new String[]{"com.mrcrayfish.guns.item.GunItem"}
    );

    private static boolean initialized = false;

    private ThirdPartyPoseCompat()
    {
    }

    public static void init()
    {
        if (initialized)
        {
            return;
        }
        initialized = true;

        for (final String modId : DEFAULT_SELF_POSING_MODS)
        {
            MoBendsAnimationControl.registerSelfPosingMod(modId, POSING_ITEM_TYPES.getOrDefault(modId, NO_ITEM_TYPES));
        }

        MoBendsAnimationControl.registerAnimationDeferral("mobends", ThirdPartyPoseCompat::shouldYieldToHeldItem);
    }

    public static boolean shouldYieldToHeldItem(LivingEntity entity)
    {
        if (entity == null)
        {
            return false;
        }

        return MoBendsAnimationControl.isSelfPosingItem(entity.getItemInHand(InteractionHand.MAIN_HAND))
                || MoBendsAnimationControl.isSelfPosingItem(entity.getItemInHand(InteractionHand.OFF_HAND));
    }
}
