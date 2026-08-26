package goblinbob.mobends.standard.mutators;

import goblinbob.mobends.core.data.IEntityDataFactory;
import goblinbob.mobends.standard.data.ZombieVillagerData;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.ZombieVillagerModel;
import net.minecraft.world.entity.monster.ZombieVillager;

public class ZombieVillagerMutator extends ZombieMutatorBase<ZombieVillagerData, ZombieVillager, ZombieVillagerModel<ZombieVillager>>
{

	public ZombieVillagerMutator(IEntityDataFactory<ZombieVillager> dataFactory)
	{
		super(dataFactory);
	}

	@Override
	public void storeVanillaModel(ZombieVillagerModel<ZombieVillager> model)
	{
		this.vanillaModel = model;

		super.storeVanillaModel(model);
	}

	@Override
	public boolean shouldModelBeSkipped(EntityModel<?> model)
	{
		return !(model instanceof ZombieVillagerModel);
	}

}
