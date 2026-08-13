package goblinbob.mobends.standard.data;

import goblinbob.mobends.standard.animation.controller.PigZombieController;
import net.minecraft.world.entity.monster.ZombifiedPiglin;

public class PigZombieData extends BipedEntityData<ZombifiedPiglin>
{

	private final PigZombieController controller = new PigZombieController();

	public PigZombieData(ZombifiedPiglin entity)
	{
		super(entity);
	}

	@Override
	public PigZombieController getController()
	{
		return controller;
	}

	@Override
	public void onTicksRestart()
	{
	}

}
