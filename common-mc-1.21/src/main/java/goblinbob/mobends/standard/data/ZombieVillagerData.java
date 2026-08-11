package goblinbob.mobends.standard.data;

import goblinbob.mobends.standard.animation.controller.ZombieVillagerController;
import net.minecraft.world.entity.monster.ZombieVillager;

public class ZombieVillagerData extends ZombieDataBase<ZombieVillager>
{

	private final ZombieVillagerController controller = new ZombieVillagerController();

	public ZombieVillagerData(ZombieVillager entity)
	{
		super(entity);
	}

	@Override
	public ZombieVillagerController getController()
	{
		return controller;
	}

	@Override
	public void onTicksRestart()
	{
	}

}
