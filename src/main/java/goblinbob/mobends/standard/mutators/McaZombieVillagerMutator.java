package goblinbob.mobends.standard.mutators;

import goblinbob.mobends.core.data.IEntityDataFactory;
import goblinbob.mobends.standard.data.ZombieData;
import net.minecraft.world.entity.monster.Zombie;

public class McaZombieVillagerMutator extends McaMutatorBase<ZombieData, Zombie>
{
    public McaZombieVillagerMutator(IEntityDataFactory<Zombie> dataFactory)
    {
        super(dataFactory);
    }
}
