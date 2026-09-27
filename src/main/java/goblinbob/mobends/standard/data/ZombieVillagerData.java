package goblinbob.mobends.standard.data;

import goblinbob.mobends.standard.animation.controller.ZombieController;
import net.minecraft.world.entity.monster.ZombieVillager;

public class ZombieVillagerData extends ZombieDataBase<ZombieVillager>
{

	private final ZombieController controller = new ZombieController();

	public ZombieVillagerData(ZombieVillager entity)
	{
		super(entity);
	}

	@Override
	public ZombieController getController()
	{
		return controller;
	}

}
