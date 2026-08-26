package goblinbob.mobends.standard.mutators;

import goblinbob.mobends.core.data.IEntityDataFactory;
import goblinbob.mobends.standard.data.HumanoidMobData;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.LivingEntity;

public class HumanoidMobMutator<E extends LivingEntity>
        extends BipedMutator<HumanoidMobData<E>, E, HumanoidModel<E>>
{
    public HumanoidMobMutator(IEntityDataFactory<E> dataFactory)
    {
        super(dataFactory);
    }
}
