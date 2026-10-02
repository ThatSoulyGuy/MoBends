package goblinbob.mobends.compat;

import dev.architectury.platform.Platform;
import goblinbob.mobends.api.addon.AddonAnimationRegistry;
import goblinbob.mobends.standard.client.renderer.entity.mutated.BipedRenderer;
import goblinbob.mobends.standard.data.HumanoidMobData;
import goblinbob.mobends.standard.mutators.HumanoidMobMutator;
import goblinbob.mobends.standard.previewer.BipedPreviewer;
import net.minecraft.world.entity.LivingEntity;

public final class MobzCompat
{
    private static final String[][] HUMANOIDS = {
            {"knight", "net.mobz.entity.Knight"},
            {"ender_knight", "net.mobz.entity.EnderKnight"},
            {"lord_of_darkness", "net.mobz.entity.LordOfDarkness"},
            {"warrior", "net.mobz.entity.Warrior"},
            {"charles", "net.mobz.entity.Charles"},
            {"andriu", "net.mobz.entity.Andriu"},
            {"dwarf", "net.mobz.entity.Dwarf"},
            {"archer", "net.mobz.entity.Archer"},
            {"bowman", "net.mobz.entity.Bowman"},
            {"templar", "net.mobz.entity.Templar"},
            {"iron_steve", "net.mobz.entity.IronSteve"},
            {"william", "net.mobz.entity.William"},
            {"katherine", "net.mobz.entity.FriendEntity$KatherineEntity"},
            {"fiora", "net.mobz.entity.FriendEntity$FioraEntity"}
    };

    private MobzCompat()
    {
    }

    public static void register(AddonAnimationRegistry registry, String[] animations, String[] alterableParts)
    {
        if (!Platform.isModLoaded("mobz"))
        {
            return;
        }

        for (String[] humanoid : HUMANOIDS)
        {
            final Class<LivingEntity> entityClass = ModCompatManager.livingEntityClass(humanoid[1]);
            if (entityClass == null)
            {
                continue;
            }

            try
            {
                registry.registerNewEntity("mobz:" + humanoid[0], "entity.mobz." + humanoid[0], entityClass,
                        HumanoidMobData::new, HumanoidMobMutator::new,
                        new BipedRenderer<>(), new BipedPreviewer<>(), animations, alterableParts);
            }
            catch (Throwable ignored)
            {
            }
        }
    }
}
