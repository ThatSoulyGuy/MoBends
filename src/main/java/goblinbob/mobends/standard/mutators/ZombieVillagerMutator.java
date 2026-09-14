package goblinbob.mobends.standard.mutators;

import goblinbob.mobends.core.data.IEntityDataFactory;
import goblinbob.mobends.standard.data.ZombieVillagerData;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.ZombieVillagerModel;
import net.minecraft.world.entity.monster.ZombieVillager;

public class ZombieVillagerMutator extends ZombieMutatorBase<ZombieVillagerData, ZombieVillager, ZombieVillagerModel<ZombieVillager>>
{

	private static final float SKIRT_FOLLOW = 1.0F;
	private static final float SKIRT_MAX_FOLD = 90.0F;
	private static final float SKIRT_MAX_LIFT = 20.0F;

	public ZombieVillagerMutator(IEntityDataFactory<ZombieVillager> dataFactory)
	{
		super(dataFactory);
	}

	@Override
	protected boolean usesAdaptiveSkirt()
	{
		return true;
	}

	@Override
	public void storeVanillaModel(ZombieVillagerModel<ZombieVillager> model)
	{
		this.vanillaModel = model;

		super.storeVanillaModel(model);
	}

	@Override
	public void syncUpWithData(ZombieVillagerData data)
	{
		super.syncUpWithData(data);

		if (skirt == null)
		{
			return;
		}

		final float fold = skirtFold(data, SKIRT_FOLLOW, SKIRT_MAX_FOLD, SKIRT_MAX_LIFT);

		skirt.rotation.orientInstantX(fold);
	}

	@Override
	public boolean shouldModelBeSkipped(EntityModel<?> model)
	{
		return !(model instanceof ZombieVillagerModel);
	}

}
