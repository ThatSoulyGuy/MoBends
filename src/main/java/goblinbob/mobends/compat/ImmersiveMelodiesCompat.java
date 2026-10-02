package goblinbob.mobends.compat;

import dev.architectury.platform.Platform;
import goblinbob.mobends.standard.mutators.BipedMutator;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;

import java.lang.reflect.Method;

public final class ImmersiveMelodiesCompat
{
    private static boolean initialized = false;
    private static Method getInstrumentMethod;
    private static Method getAnimatorMethod;

    private ImmersiveMelodiesCompat()
    {
    }

    private static void init()
    {
        initialized = true;

        if (!Platform.isModLoaded("immersive_melodies"))
        {
            return;
        }

        try
        {
            getInstrumentMethod = Class.forName("immersive_melodies.client.animation.EntityModelAnimator")
                    .getMethod("getInstrument", Entity.class);
            getAnimatorMethod = Class.forName("immersive_melodies.client.animation.ItemAnimators")
                    .getMethod("get", ResourceLocation.class);
        }
        catch (Throwable e)
        {
            getInstrumentMethod = null;
        }
    }

    public static void applyPose(LivingEntity entity, BipedMutator<?, ?, ?> mutator, HumanoidModel<?> vanillaModel)
    {
        if (!initialized)
        {
            init();
        }

        if (getInstrumentMethod == null || entity == null || mutator == null || vanillaModel == null)
        {
            return;
        }

        final boolean posesHead;

        try
        {
            final Object instrument = getInstrumentMethod.invoke(null, entity);
            if (!(instrument instanceof Item item))
            {
                return;
            }

            final Object animator = getAnimatorMethod.invoke(null, BuiltInRegistries.ITEM.getKey(item));
            final String animatorName = animator == null ? "" : animator.getClass().getSimpleName();
            posesHead = animatorName.equals("FluteAnimator") || animatorName.equals("BagpipeAnimator");
        }
        catch (Throwable e)
        {
            return;
        }

        mutator.adoptUpperBodyFromVanillaModel(vanillaModel, posesHead, true, true, true);
    }
}
