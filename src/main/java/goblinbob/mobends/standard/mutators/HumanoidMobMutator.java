package goblinbob.mobends.standard.mutators;

import goblinbob.mobends.core.data.IEntityDataFactory;
import goblinbob.mobends.standard.client.model.adaptive.AdaptiveHumanoidGeometry;
import goblinbob.mobends.standard.data.HumanoidMobData;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

public class HumanoidMobMutator<E extends LivingEntity>
        extends BipedMutator<HumanoidMobData<E>, E, HumanoidModel<E>>
{
    private HumanoidModel<?> builtFromModel;

    private final Set<HumanoidModel<E>> builtModels = Collections.newSetFromMap(new WeakHashMap<>());

    public HumanoidMobMutator(IEntityDataFactory<E> dataFactory)
    {
        super(dataFactory);
    }

    @Override
    public void updateModel(E entity, LivingEntityRenderer<E, HumanoidModel<E>> renderer, float partialTicks)
    {
        final HumanoidModel<E> model = renderer.getModel();

        if (model != null && model != builtFromModel && body != null && !shouldModelBeSkipped(model))
        {
            restoreVanillaPivots(model);
            alignPlayerWear(model);
            createParts(model, 0.0F);
        }

        super.updateModel(entity, renderer, partialTicks);
    }

    @Override
    protected AdaptiveHumanoidGeometry.WearParts adaptiveWearParts(HumanoidModel<E> original)
    {
        return playerWearPartsOf(original);
    }

    @Override
    protected AdaptiveHumanoidGeometry.CaptureMode adaptiveLimbCaptureMode()
    {
        return AdaptiveHumanoidGeometry.CaptureMode.SUBTREE;
    }

    @Override
    protected void createAdaptiveWearParts(AdaptiveHumanoidGeometry geometry)
    {
        attachAdaptiveWear(geometry);
    }

    @Override
    public void demutate(LivingEntityRenderer<E, HumanoidModel<E>> renderer)
    {
        super.demutate(renderer);

        final HumanoidModel<E> current = renderer.getModel();
        for (HumanoidModel<E> model : builtModels)
        {
            if (model != current && !shouldModelBeSkipped(model))
            {
                applyVanillaModel(model);
            }
        }
        builtModels.clear();
        builtFromModel = null;
    }

    @Override
    public boolean createParts(HumanoidModel<E> original, float scaleFactor)
    {
        builtFromModel = original;
        if (original != null)
        {
            builtModels.add(original);
        }

        if (tryCreateAdaptiveParts(original))
        {
            return true;
        }

        return super.createParts(original, scaleFactor);
    }
}
