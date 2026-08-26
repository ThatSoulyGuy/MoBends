package goblinbob.mobends.standard.mutators;

import goblinbob.mobends.core.data.IEntityDataFactory;
import goblinbob.mobends.standard.data.McaVillagerData;
import net.minecraft.world.entity.LivingEntity;

public class McaVillagerMutator<E extends LivingEntity> extends McaMutatorBase<McaVillagerData<E>, E>
{
    public McaVillagerMutator(IEntityDataFactory<E> dataFactory)
    {
        super(dataFactory);
    }
}
