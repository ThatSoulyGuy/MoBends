package goblinbob.mobends.compat;

import dev.architectury.platform.Platform;
import goblinbob.mobends.api.addon.AddonAnimationRegistry;
import goblinbob.mobends.standard.client.renderer.entity.mutated.BipedRenderer;
import goblinbob.mobends.standard.data.IllagerData;
import goblinbob.mobends.standard.mutators.IllagerMutator;
import goblinbob.mobends.standard.previewer.BipedPreviewer;
import net.minecraft.world.entity.monster.AbstractIllager;

public final class TakesAPillageCompat
{
    private static final String MOD_ID = "takesapillage";
    private static final String[][] ILLAGERS = {
            {"archer", "Archer"},
            {"legioner", "Legioner"},
            {"skirmisher", "Skirmisher"}
    };

    private TakesAPillageCompat()
    {
    }

    public static void register(AddonAnimationRegistry registry, String[] animations, String[] alterableParts)
    {
        if (!Platform.isModLoaded(MOD_ID))
        {
            return;
        }

        for (String[] illager : ILLAGERS)
        {
            try
            {
                @SuppressWarnings("unchecked")
                final Class<AbstractIllager> entityClass = (Class<AbstractIllager>) Class
                        .forName("com.izofar.takesapillage.entity." + illager[1]).asSubclass(AbstractIllager.class);
                registry.registerNewEntity(MOD_ID + ":" + illager[0], "entity." + MOD_ID + "." + illager[0], entityClass,
                        IllagerData::new, IllagerMutator::new,
                        new BipedRenderer<>(), new BipedPreviewer<>(), animations, alterableParts);
            }
            catch (Throwable ignored)
            {
            }
        }
    }
}
