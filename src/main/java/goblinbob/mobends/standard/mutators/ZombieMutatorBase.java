package goblinbob.mobends.standard.mutators;

import goblinbob.mobends.core.data.IEntityDataFactory;
import goblinbob.mobends.standard.data.ZombieDataBase;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.ZombieModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.monster.Zombie;

/**
 * This base is used both by ZombieMutator and ZombieVillagerMutator, but since
 * the ZombieVillagerModel doesn't extend ZombieModel, a separate base class had
 * to be made.
 *
 * @author Iwo Plaza
 *
 * @param <D>
 * @param <E>
 * @param <M>
 */
public abstract class ZombieMutatorBase<D extends ZombieDataBase<E>,
                                        E extends Zombie,
                                        M extends HumanoidModel<E>>
                                       extends BipedMutator<D, E, M>
{

    // Should the height of the texture be 64 or 32 (half)?
    protected boolean halfTexture = false;

    public ZombieMutatorBase(IEntityDataFactory<E> dataCreationFunction)
    {
        super(dataCreationFunction);
    }

    @Override
    public void fetchFields(LivingEntityRenderer<E, M> renderer)
    {
        super.fetchFields(renderer);

        // In 1.20.1, we can check the texture height from the model if needed
        // For now, default to false (64-pixel height)
        this.halfTexture = false;
    }
}
