package goblinbob.mobends.standard.mutators;

import goblinbob.mobends.core.data.IEntityDataFactory;
import goblinbob.mobends.standard.data.PigZombieData;
import net.minecraft.world.entity.monster.ZombifiedPiglin;

public class PigZombieMutator extends PiglinMutatorBase<PigZombieData, ZombifiedPiglin>
{
    public PigZombieMutator(IEntityDataFactory<ZombifiedPiglin> dataFactory)
    {
        super(dataFactory);
    }
}
