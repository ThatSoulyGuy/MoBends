package goblinbob.mobends.compat;

import goblinbob.mobends.api.animation.MoBendsAnimationControl;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

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
            MoBendsAnimationControl.registerSelfPosingMod(modId);
        }

        MoBendsAnimationControl.registerAnimationDeferral("mobends", ThirdPartyPoseCompat::shouldYieldToHeldItem);
    }

    public static boolean shouldYieldToHeldItem(LivingEntity entity)
    {
        if (entity == null)
        {
            return false;
        }

        return isSelfPosed(entity.getItemInHand(InteractionHand.MAIN_HAND))
                || isSelfPosed(entity.getItemInHand(InteractionHand.OFF_HAND));
    }

    private static boolean isSelfPosed(ItemStack stack)
    {
        if (stack == null || stack.isEmpty())
        {
            return false;
        }

        final ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return id != null && MoBendsAnimationControl.isSelfPosingMod(id.getNamespace());
    }
}
