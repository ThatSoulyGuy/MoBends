package goblinbob.mobends.compat;

import dev.architectury.platform.Platform;
import goblinbob.mobends.core.util.ResourceLocationFactory;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

public class ZiplineCompat
{
    private static final String MOD_ID = "zipline";

    private static boolean initialized = false;
    private static boolean isLoaded = false;

    private static TagKey<Item> attachmentTag;

    public static void init()
    {
        if (initialized)
        {
            return;
        }
        initialized = true;

        isLoaded = Platform.isModLoaded(MOD_ID);

        if (isLoaded)
        {
            attachmentTag = TagKey.create(Registries.ITEM, ResourceLocationFactory.create(MOD_ID, "attachment"));
        }
    }

    public static boolean isModLoaded()
    {
        if (!initialized)
        {
            init();
        }
        return isLoaded;
    }

    public static boolean isZiplining(LivingEntity entity)
    {
        return getZipliningArm(entity) != null;
    }

    public static HumanoidArm getZipliningArm(LivingEntity entity)
    {
        if (!isModLoaded() || !(entity instanceof Player player) || !player.isUsingItem())
        {
            return null;
        }

        if (!player.getUseItem().is(attachmentTag))
        {
            return null;
        }

        final HumanoidArm mainArm = player.getMainArm();
        return player.getUsedItemHand() == InteractionHand.MAIN_HAND ? mainArm : mainArm.getOpposite();
    }
}
