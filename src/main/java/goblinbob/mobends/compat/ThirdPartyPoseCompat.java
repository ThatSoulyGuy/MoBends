package goblinbob.mobends.compat;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class ThirdPartyPoseCompat
{
    private static final Set<String> SELF_POSING_MODS = Collections.synchronizedSet(new HashSet<>(Set.of(
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
    )));

    private ThirdPartyPoseCompat()
    {
    }

    public static void register(String modId)
    {
        if (modId != null && !modId.isEmpty())
        {
            SELF_POSING_MODS.add(modId);
        }
    }

    public static boolean isSelfPosingMod(String modId)
    {
        return SELF_POSING_MODS.contains(modId);
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
        return id != null && SELF_POSING_MODS.contains(id.getNamespace());
    }
}
