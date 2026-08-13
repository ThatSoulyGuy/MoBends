package goblinbob.mobends.core.mutators;

import goblinbob.mobends.core.data.IEntityDataFactory;
import goblinbob.mobends.core.data.LivingEntityData;
import net.minecraft.world.entity.LivingEntity;

@FunctionalInterface
public interface IMutatorFactory<E extends LivingEntity>
{

    Mutator<? extends LivingEntityData<E>, ? extends E, ?> createMutator(IEntityDataFactory<E> dataFactory);

}
