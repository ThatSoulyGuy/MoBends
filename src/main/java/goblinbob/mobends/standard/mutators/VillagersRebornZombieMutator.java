package goblinbob.mobends.standard.mutators;

import goblinbob.mobends.core.data.IEntityDataFactory;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.monster.Zombie;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

public class VillagersRebornZombieMutator extends ZombieMutator
{
    private HumanoidModel<?> builtFromModel;

    private final Set<HumanoidModel<Zombie>> builtModels = Collections.newSetFromMap(new WeakHashMap<>());

    public VillagersRebornZombieMutator(IEntityDataFactory<Zombie> dataFactory)
    {
        super(dataFactory);
    }

    @Override
    public void updateModel(Zombie entity, LivingEntityRenderer<Zombie, HumanoidModel<Zombie>> renderer, float partialTicks)
    {
        final HumanoidModel<Zombie> model = renderer.getModel();

        if (model != null && model != builtFromModel && body != null && !shouldModelBeSkipped(model))
        {
            restoreVanillaPivots(model);
            createParts(model, 0.0F);
        }

        super.updateModel(entity, renderer, partialTicks);
    }

    @Override
    public void demutate(LivingEntityRenderer<Zombie, HumanoidModel<Zombie>> renderer)
    {
        super.demutate(renderer);

        final HumanoidModel<Zombie> current = renderer.getModel();
        for (HumanoidModel<Zombie> model : builtModels)
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
    public boolean createParts(HumanoidModel<Zombie> original, float scaleFactor)
    {
        builtFromModel = original;
        if (original != null)
        {
            builtModels.add(original);
        }

        return super.createParts(original, scaleFactor);
    }
}
