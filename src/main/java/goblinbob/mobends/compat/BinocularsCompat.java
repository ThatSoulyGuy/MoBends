package goblinbob.mobends.compat;

import dev.architectury.platform.Platform;
import goblinbob.mobends.core.util.ResourceLocationFactory;
import goblinbob.mobends.standard.mutators.BipedMutator;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

public final class BinocularsCompat
{
    private static boolean initialized = false;
    private static Item binoculars;

    private BinocularsCompat()
    {
    }

    public static void applyPose(LivingEntity entity, BipedMutator<?, ?, ?> mutator, HumanoidModel<?> vanillaModel)
    {
        if (!initialized)
        {
            initialized = true;

            if (Platform.isModLoaded("binocularsmod"))
            {
                binoculars = BuiltInRegistries.ITEM.get(ResourceLocationFactory.create("binocularsmod", "binoculars"));
            }
        }

        if (binoculars == null || !(entity instanceof Player) || mutator == null || vanillaModel == null
                || !entity.getUseItem().is(binoculars))
        {
            return;
        }

        mutator.adoptUpperBodyFromVanillaModel(vanillaModel, true, true, true, true);
    }
}
