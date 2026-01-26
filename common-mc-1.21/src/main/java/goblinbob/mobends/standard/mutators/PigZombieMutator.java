package goblinbob.mobends.standard.mutators;

import goblinbob.mobends.core.data.IEntityDataFactory;
import goblinbob.mobends.standard.data.PigZombieData;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.ZombieModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.monster.ZombifiedPiglin;

public class PigZombieMutator extends BipedMutator<PigZombieData, ZombifiedPiglin, ZombieModel<ZombifiedPiglin>>
{

	// Should the height of the texture be 64 or 32(half)?
	protected boolean halfTexture = false;

	public PigZombieMutator(IEntityDataFactory<ZombifiedPiglin> dataFactory)
	{
		super(dataFactory);
	}

	@Override
	public void fetchFields(LivingEntityRenderer<ZombifiedPiglin, ZombieModel<ZombifiedPiglin>> renderer)
	{
		super.fetchFields(renderer);

		// In 1.20.1, the texture height is not easily accessible from the model
		// Default to full texture
		this.halfTexture = false;
	}

	@Override
	public void storeVanillaModel(ZombieModel<ZombifiedPiglin> model)
	{
		// In 1.20.1, models are created differently - using baked model definitions
		// For now, store a reference to indicate this is vanilla
		this.vanillaModel = model;

		// Calling the super method here, since it
		// requires the vanillaModel property to be
		// set.
		super.storeVanillaModel(model);
	}

	@Override
	public boolean shouldModelBeSkipped(EntityModel<?> model)
	{
		return !(model instanceof ZombieModel);
	}

}
