package goblinbob.mobends.standard.data;

import goblinbob.mobends.standard.animation.controller.ZombieController;
import net.minecraft.world.entity.monster.Zombie;

public class ZombieData extends ZombieDataBase<Zombie>
{

    private final ZombieController controller = new ZombieController();

    public ZombieData(Zombie entity)
    {
        super(entity);
    }

    @Override
    public ZombieController getController()
    {
        return this.controller;
    }

    @Override
    public void onTicksRestart()
    {
        // No behaviour
    }

}
