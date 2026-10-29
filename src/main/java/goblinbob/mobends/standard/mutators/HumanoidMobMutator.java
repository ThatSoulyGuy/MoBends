package goblinbob.mobends.standard.mutators;

import goblinbob.mobends.core.data.IEntityDataFactory;
import goblinbob.mobends.standard.client.model.adaptive.AdaptiveHumanoidGeometry;
import goblinbob.mobends.standard.data.HumanoidMobData;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

public class HumanoidMobMutator<E extends LivingEntity>
        extends BipedMutator<HumanoidMobData<E>, E, HumanoidModel<E>>
{
    private static final String[] WEAR_FIELD_NAMES = {"jacket", "leftSleeve", "rightSleeve", "leftPants", "rightPants"};
    private static final Map<Class<?>, Field[]> DECLARED_WEAR_FIELDS = new ConcurrentHashMap<>();

    private HumanoidModel<?> builtFromModel;

    private final Set<HumanoidModel<E>> builtModels = Collections.newSetFromMap(new WeakHashMap<>());
    private final Map<ModelPart, Boolean> hiddenWear = new IdentityHashMap<>();

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
        final AdaptiveHumanoidGeometry.WearParts playerWear = playerWearPartsOf(original);
        if (playerWear != null)
        {
            return playerWear;
        }

        final ModelPart[] declared = declaredWearPartsOf(original);
        return declared == null ? null
                : new AdaptiveHumanoidGeometry.WearParts(declared[0], declared[1], declared[2], declared[3], declared[4]);
    }

    private void hideDeclaredWear(HumanoidModel<E> original)
    {
        if (playerWearPartsOf(original) != null)
        {
            return;
        }

        final ModelPart[] declared = declaredWearPartsOf(original);
        if (declared == null)
        {
            return;
        }

        for (ModelPart part : declared)
        {
            hiddenWear.putIfAbsent(part, part.visible);
            part.visible = false;
        }
    }

    private static ModelPart[] declaredWearPartsOf(HumanoidModel<?> model)
    {
        final Field[] fields = DECLARED_WEAR_FIELDS.computeIfAbsent(model.getClass(), HumanoidMobMutator::findWearFields);
        if (fields.length == 0)
        {
            return null;
        }

        try
        {
            final ModelPart[] parts = new ModelPart[fields.length];
            for (int i = 0; i < fields.length; i++)
            {
                parts[i] = (ModelPart) fields[i].get(model);
                if (parts[i] == null)
                {
                    return null;
                }
            }
            return parts;
        }
        catch (Exception e)
        {
            return null;
        }
    }

    private static Field[] findWearFields(Class<?> modelClass)
    {
        final Field[] fields = new Field[WEAR_FIELD_NAMES.length];

        for (int i = 0; i < WEAR_FIELD_NAMES.length; i++)
        {
            try
            {
                fields[i] = modelClass.getDeclaredField(WEAR_FIELD_NAMES[i]);
                if (fields[i].getType() != ModelPart.class)
                {
                    return new Field[0];
                }
                fields[i].setAccessible(true);
            }
            catch (Exception e)
            {
                return new Field[0];
            }
        }

        return fields;
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

        hiddenWear.forEach((part, visible) -> part.visible = visible);
        hiddenWear.clear();
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
            hideDeclaredWear(original);
            return true;
        }

        return super.createParts(original, scaleFactor);
    }
}
