package goblinbob.mobends.standard.animation.controller;

import goblinbob.mobends.core.animation.controller.IAnimationController;
import goblinbob.mobends.core.animation.keyframe.AnimationLoader;
import goblinbob.mobends.core.client.event.DataUpdateHandler;
import goblinbob.mobends.core.kumo.state.KumoAnimatorState;
import goblinbob.mobends.core.kumo.state.template.AnimatorTemplate;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.util.GsonResources;
import goblinbob.mobends.lib.util.GUtil;
import goblinbob.mobends.core.util.ResourceLocationFactory;
import goblinbob.mobends.standard.data.WolfData;
import goblinbob.mobends.standard.main.ModStatics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Wolf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;

public class WolfController implements IAnimationController<WolfData>
{
    private static final Logger LOGGER = LoggerFactory.getLogger(WolfController.class);

    protected static final String WOLF_ANIMATOR_PATH = "bends/animators/wolf.json";

    protected AnimatorTemplate animatorTemplate;
    protected KumoAnimatorState<WolfData> kumoAnimatorState;

    public WolfController()
    {
        try
        {
            final ResourceLocation animatorLocation =
                    ResourceLocationFactory.create(ModStatics.MODID, WOLF_ANIMATOR_PATH);

            animatorTemplate = GsonResources.get(animatorLocation, AnimatorTemplate.class);
            kumoAnimatorState = new KumoAnimatorState<>(animatorTemplate, key -> {
                try
                {
                    return AnimationLoader.loadFromPath(key);
                }
                catch (IOException e)
                {
                    LOGGER.error("Failed to load wolf animation '{}'", key, e);
                    return null;
                }
            });
        }
        catch (IOException | MalformedKumoTemplateException e)
        {
            LOGGER.error("Failed to initialize the wolf animator", e);
        }
    }

    @Override
    public Collection<String> perform(WolfData data)
    {
        final Wolf wolf = data.getEntity();
        final float partialTicks = DataUpdateHandler.partialTicks;
        final float ticks = wolf.tickCount + partialTicks;

        if (kumoAnimatorState != null)
        {
            try
            {
                kumoAnimatorState.update(data, DataUpdateHandler.ticksPerFrame);
            }
            catch (MalformedKumoTemplateException e)
            {
                LOGGER.error("Malformed wolf animator template", e);
            }
        }

        data.head.offsetScale = 1.0F;
        data.head.globalOffset.set(0.0F, 0.0F, 0.0F);
        data.head.position.set(0.0F, -0.5F, -13.0F);

        data.head.rotation.localRotateY(data.headYaw.get()).finish();
        data.head.rotation.localRotateX(data.headPitch.get()).finish();

        data.head.rotation.localRotateZ((wolf.getHeadRollAngle(partialTicks)
                + wolf.getBodyRollAngle(partialTicks, 0.0F)) * GUtil.RAD_TO_DEG).finish();
        data.mane.rotation.localRotateZ(wolf.getBodyRollAngle(partialTicks, -0.08F) * GUtil.RAD_TO_DEG).finish();
        data.tail.rotation.localRotateZ(wolf.getBodyRollAngle(partialTicks, -0.2F) * GUtil.RAD_TO_DEG).finish();

        data.tail.rotation.localRotateZ(wolf.getHeadRollAngle(partialTicks) * Mth.sin(ticks) * 20.0F).finish();

        data.tail.rotation.localRotateX(wolf.getTailAngle() * GUtil.RAD_TO_DEG - 90.0F).finish();

        data.head.offset.set(0, 0, 0);

        return null;
    }

}
