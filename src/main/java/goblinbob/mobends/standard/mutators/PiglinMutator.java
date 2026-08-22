package goblinbob.mobends.standard.mutators;

import goblinbob.mobends.core.data.IEntityDataFactory;
import goblinbob.mobends.standard.data.PiglinData;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;

public class PiglinMutator<E extends AbstractPiglin> extends PiglinMutatorBase<PiglinData<E>, E>
{
    public PiglinMutator(IEntityDataFactory<E> dataFactory)
    {
        super(dataFactory);
    }
}
