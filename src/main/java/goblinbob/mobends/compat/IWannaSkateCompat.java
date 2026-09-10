package goblinbob.mobends.compat;

import dev.architectury.platform.Platform;
import goblinbob.mobends.standard.mutators.BipedMutator;
import goblinbob.mobends.standard.previewer.PlayerPreviewer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class IWannaSkateCompat
{
    private static final String MOD_ID = "iwannaskate";

    private static boolean initialized = false;
    private static Class<?> skateboardClass;

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
            skateboardClass = Class.forName("com.github.alexthe668.iwannaskate.server.entity.SkateboardEntity");
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
        return skateboardClass != null;
    }

    public static boolean isSkating(LivingEntity entity)
    {
        if (entity == null || !isModLoaded())
        {
            return false;
        }

        final Entity vehicle = entity.getVehicle();

        return vehicle != null && skateboardClass.isInstance(vehicle);
    }

    public static boolean isPosingModel(LivingEntity entity)
    {
        return isSkating(entity) && !PlayerPreviewer.isPreviewInProgress();
    }

    public static void applyPose(LivingEntity entity, BipedMutator<?, ?, ?> mutator, HumanoidModel<?> vanillaModel)
    {
        if (mutator == null || !(vanillaModel instanceof PlayerModel<?>))
        {
            return;
        }

        if (!isPosingModel(entity))
        {
            return;
        }

        mutator.adoptPoseFromVanillaModel(vanillaModel, null, null);
    }
}
