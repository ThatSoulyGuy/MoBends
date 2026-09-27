package goblinbob.mobends.standard.mutators;

import goblinbob.mobends.core.data.IEntityDataFactory;
import goblinbob.mobends.standard.data.ZombieData;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.monster.Zombie;

public class ZombieMutator extends BipedMutator<ZombieData, Zombie, HumanoidModel<Zombie>>
{

    public ZombieMutator(IEntityDataFactory<Zombie> dataFactory)
    {
        super(dataFactory);
    }
}
