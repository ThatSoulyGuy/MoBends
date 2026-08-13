package goblinbob.mobends.core.data;

import net.minecraft.world.entity.Entity;

@FunctionalInterface
public interface IEntityDataFactory<E extends Entity>
{
    EntityData<E> createEntityData(E entity);
}
