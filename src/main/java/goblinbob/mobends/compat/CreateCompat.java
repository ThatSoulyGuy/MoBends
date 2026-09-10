package goblinbob.mobends.compat;

import dev.architectury.platform.Platform;
import goblinbob.mobends.core.util.ResourceLocationFactory;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

import java.lang.reflect.Field;
import java.util.Set;

public class CreateCompat
{
    private static final String MOD_ID = "create";

    private static boolean initialized = false;
    private static boolean isLoaded = false;

    private static Set<?> hangingPlayers;
    private static TagKey<Item> chainRideableTag;

    public static void init()
    {
        if (initialized)
        {
            return;
        }
        initialized = true;

        isLoaded = Platform.isModLoaded(MOD_ID);

        if (!isLoaded)
        {
            return;
        }

        chainRideableTag = TagKey.create(Registries.ITEM, ResourceLocationFactory.create(MOD_ID, "chain_rideable"));

        try
        {
            Class<?> skyhookClass = Class.forName("com.simibubi.create.foundation.render.PlayerSkyhookRenderer");
            Field field = skyhookClass.getDeclaredField("hangingPlayers");
            field.setAccessible(true);

            Object players = field.get(null);
            hangingPlayers = players instanceof Set<?> set ? set : null;
        }
        catch (Throwable ignored)
        {
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

    public static boolean isHangingOnChain(LivingEntity entity)
    {
        if (!isModLoaded() || hangingPlayers == null || !(entity instanceof Player))
        {
            return false;
        }

        try
        {
            return hangingPlayers.contains(entity.getUUID());
        }
        catch (Throwable ignored)
        {
            return false;
        }
    }

    public static HumanoidArm getChainGripArm(LivingEntity entity)
    {
        final HumanoidArm mainArm = entity.getMainArm();

        if (chainRideableTag == null)
        {
            return mainArm;
        }

        final boolean leftIsGripping = (mainArm == HumanoidArm.LEFT)
                ^ !entity.getMainHandItem().is(chainRideableTag);

        return leftIsGripping ? HumanoidArm.LEFT : HumanoidArm.RIGHT;
    }
}
