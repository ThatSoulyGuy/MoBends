package goblinbob.mobends.standard.mutators;

import goblinbob.mobends.core.client.model.BendsModelPart;
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
		// In 1.20.1, models are created from LayerDefinitions
		// Store reference to indicate this is vanilla
		this.vanillaModel = model;

		// Calling the super method here, since it
		// requires the vanillaModel property to be
		// set.
		super.storeVanillaModel(model);
	}

	@Override
	public boolean createParts(ZombieVillagerModel<ZombieVillager> original, float scaleFactor)
	{
		boolean success = super.createParts(original, scaleFactor);

		// In 1.20.1, we create our own head model part using BendsModelPart
		this.head = new BendsModelPart(0, 0);
		this.head.setParent(body);
		this.head.position.set(0.0F, -12.0F, 0.0F);

		// Main head box
		this.head.addCube(-4.0F, -10.0F, -4.0F, 8, 10, 8, scaleFactor);
		// Nose
		this.head.setTextureOffset(24, 0);
		this.head.addCube(-1.0F, -3.0F, -6.0F, 2, 4, 2, scaleFactor);

		return success;
	}

	@Override
	public boolean shouldModelBeSkipped(EntityModel<?> model)
	{
		return !(model instanceof ZombieVillagerModel);
	}

}
