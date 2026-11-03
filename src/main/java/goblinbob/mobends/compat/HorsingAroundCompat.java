package goblinbob.mobends.compat;

import dev.architectury.platform.Platform;
import goblinbob.mobends.core.client.model.BendsModelPart;
import goblinbob.mobends.core.client.model.ModelPartTransform;
import goblinbob.mobends.lib.math.Quaternion;
import goblinbob.mobends.lib.math.SmoothOrientation;
import goblinbob.mobends.standard.data.BipedEntityData;
import goblinbob.mobends.standard.mutators.BipedMutator;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.lang.reflect.Method;

public class HorsingAroundCompat
{
    private static final String MOD_ID = "horsingaround";
    private static final float ATTACK_TICKS = 10.0F;
    private static final float RETURN_TICKS = 8.0F;

    private static final Quaternion[] CAPTURED = {
            new Quaternion(), new Quaternion(), new Quaternion(), new Quaternion(), new Quaternion() };
    private static final SmoothOrientation[] SAVED = {
            new SmoothOrientation(), new SmoothOrientation(), new SmoothOrientation(),
            new SmoothOrientation(), new SmoothOrientation() };
    private static final Quaternion BLENDED = new Quaternion();

    private static boolean initialized = false;
    private static Method holderMethod;

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
            holderMethod = Class.forName("dev.horsingaround.client.render.RidePoser")
                    .getMethod("holder", LivingEntity.class);
        }
        catch (Throwable e)
        {
            holderMethod = null;
        }
    }

    public static boolean isPosingModel(LivingEntity entity)
    {
        if (!initialized)
        {
            init();
        }
        if (holderMethod == null || !(entity instanceof Player) || !entity.isPassenger())
        {
            return false;
        }

        try
        {
            return holderMethod.invoke(null, entity) != null;
        }
        catch (Throwable e)
        {
            holderMethod = null;
            return false;
        }
    }

    public static boolean isAttacking(BipedEntityData<?> data)
    {
        return data.getTicksAfterAnyAttack() < ATTACK_TICKS + RETURN_TICKS && !data.getEntity().isUsingItem();
    }

    public static void applyPose(LivingEntity entity, BipedMutator<?, ?, ?> mutator, HumanoidModel<?> model)
    {
        if (mutator == null || !(model instanceof PlayerModel<?>) || !isPosingModel(entity))
        {
            return;
        }

        final BipedEntityData<?> data = mutator.getRenderData();
        if (data == null || !isAttacking(data))
        {
            mutator.adoptPoseFromVanillaModel(model, null, null);
            return;
        }

        final BendsModelPart[] parts = {
                mutator.getBody(), mutator.getLeftArm(), mutator.getRightArm(),
                mutator.getLeftForeArm(), mutator.getRightForeArm() };
        final ModelPartTransform[] dataParts = {
                data.body, data.leftArm, data.rightArm, data.leftForeArm, data.rightForeArm };

        for (int i = 0; i < parts.length; i++)
        {
            CAPTURED[i].set(parts[i].getRotation().getSmooth());
            SAVED[i].set(dataParts[i].getRotation());
        }

        mutator.adoptPoseFromVanillaModel(model, null, null);

        final float back = Mth.clamp((data.getTicksAfterAnyAttack() - ATTACK_TICKS) / RETURN_TICKS, 0.0F, 1.0F);
        for (int i = 0; i < parts.length; i++)
        {
            PlayerAnimationLibCompat.nlerp(CAPTURED[i], parts[i].getRotation().getSmooth(), back, BLENDED);
            if (back > 0.0F)
            {
                BipedMutator.applyAdoptedRotation(parts[i], dataParts[i], BLENDED);
            }
            else
            {
                BipedMutator.applyAdoptedRotation(parts[i], null, BLENDED);
                dataParts[i].getRotation().set(SAVED[i]);
            }
        }

        mutator.adoptUpperBodyFromVanillaModel(model, true, false, false);
    }
}
