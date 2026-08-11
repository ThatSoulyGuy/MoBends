package goblinbob.mobends.standard.mutators;

import goblinbob.mobends.core.data.IEntityDataFactory;
import goblinbob.mobends.standard.data.PigZombieData;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.ZombieModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.monster.ZombifiedPiglin;

public class PigZombieMutator extends BipedMutator<PigZombieData, ZombifiedPiglin, ZombieModel<ZombifiedPiglin>>
{

	protected boolean halfTexture = false;

	public PigZombieMutator(IEntityDataFactory<ZombifiedPiglin> dataFactory)
	{
		super(dataFactory);
	}

	@Override
	public void fetchFields(LivingEntityRenderer<ZombifiedPiglin, ZombieModel<ZombifiedPiglin>> renderer)
	{
		super.fetchFields(renderer);

		this.halfTexture = false;
	}

	@Override
	public void storeVanillaModel(ZombieModel<ZombifiedPiglin> model)
	{
		this.vanillaModel = model;

		super.storeVanillaModel(model);
	}

	@Override
	public boolean shouldModelBeSkipped(EntityModel<?> model)
	{
		return !(model instanceof ZombieModel);
	}

}
