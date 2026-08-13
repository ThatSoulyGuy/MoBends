package goblinbob.mobends.standard.mutators;

import goblinbob.mobends.core.data.IEntityDataFactory;
import goblinbob.mobends.standard.data.ZombieDataBase;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.monster.Zombie;

public abstract class ZombieMutatorBase<D extends ZombieDataBase<E>,
                                        E extends Zombie,
                                        M extends HumanoidModel<E>>
                                       extends BipedMutator<D, E, M>
{

    protected boolean halfTexture = false;

    public ZombieMutatorBase(IEntityDataFactory<E> dataCreationFunction)
    {
        super(dataCreationFunction);
    }

    @Override
    public void fetchFields(LivingEntityRenderer<E, M> renderer)
    {
        super.fetchFields(renderer);

        this.halfTexture = false;
    }
}
