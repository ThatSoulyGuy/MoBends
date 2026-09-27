package goblinbob.mobends.compat;

import dev.architectury.platform.Platform;
import goblinbob.mobends.core.client.event.DataUpdateHandler;
import goblinbob.mobends.standard.mutators.BipedMutator;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.LivingEntity;

import java.lang.reflect.Method;
import java.util.function.BiConsumer;

public class PalladiumCompat
{
    private static final String MOD_ID = "palladium";

    private static final int HEAD = 1;
    private static final int RIGHT_ARM = 1 << 1;
    private static final int LEFT_ARM = 1 << 2;
    private static final int FULL_BODY = 1 << 3;

    private static boolean initialized = false;
    private static Method forEachMethod = null;
    private static Object thirdPersonContext = null;

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
            final Class<?> contextClass = Class.forName(
                    "net.threetag.palladium.client.model.animation.PalladiumAnimation$FirstPersonContext");
            thirdPersonContext = contextClass.getField("NONE").get(null);
            forEachMethod = Class.forName("net.threetag.palladium.client.model.animation.PalladiumAnimationRegistry")
                    .getMethod("forEach", AbstractClientPlayer.class, HumanoidModel.class, contextClass,
                            float.class, BiConsumer.class);
        }
        catch (Throwable e)
        {
            forEachMethod = null;
        }
    }

    public static void applyPose(LivingEntity entity, BipedMutator<?, ?, ?> mutator, HumanoidModel<?> vanillaModel)
    {
        if (!initialized)
        {
            init();
        }

        if (forEachMethod == null || mutator == null || !(entity instanceof AbstractClientPlayer)
                || !(vanillaModel instanceof PlayerModel<?>))
        {
            return;
        }

        final int mask = animatedParts(entity, vanillaModel);

        if ((mask & FULL_BODY) != 0)
        {
            mutator.adoptPoseFromVanillaModel(vanillaModel, null, null);
        }
        else if (mask != 0)
        {
            mutator.adoptUpperBodyFromVanillaModel(vanillaModel,
                    (mask & HEAD) != 0, (mask & LEFT_ARM) != 0, (mask & RIGHT_ARM) != 0);
        }
    }

    private static int animatedParts(LivingEntity entity, HumanoidModel<?> vanillaModel)
    {
        final int[] mask = {0};
        final BiConsumer<Object, Object> collector = (part, data) -> mask[0] |= maskOf(((Enum<?>) part).name());

        try
        {
            forEachMethod.invoke(null, entity, vanillaModel, thirdPersonContext, DataUpdateHandler.partialTicks, collector);
        }
        catch (Throwable e)
        {
            return 0;
        }

        return mask[0];
    }

    private static int maskOf(String part)
    {
        return switch (part)
        {
            case "HEAD" -> HEAD;
            case "RIGHT_ARM" -> RIGHT_ARM;
            case "LEFT_ARM" -> LEFT_ARM;
            default -> FULL_BODY;
        };
    }
}
