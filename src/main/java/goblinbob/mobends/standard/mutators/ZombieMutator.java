package goblinbob.mobends.standard.mutators;

import goblinbob.mobends.core.data.IEntityDataFactory;
import goblinbob.mobends.standard.data.ZombieData;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.monster.Zombie;

public class ZombieMutator extends ZombieMutatorBase<ZombieData, Zombie, HumanoidModel<Zombie>>
{

    public ZombieMutator(IEntityDataFactory<Zombie> dataFactory)
    {
        super(dataFactory);
    }

    @Override
    public void storeVanillaModel(HumanoidModel<Zombie> model)
    {
        super.storeVanillaModel(model);
    }

    @Override
    public boolean shouldModelBeSkipped(EntityModel<?> model)
    {
        return !(model instanceof HumanoidModel);
    }
}
