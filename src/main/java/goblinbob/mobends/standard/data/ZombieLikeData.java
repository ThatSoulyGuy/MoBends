package goblinbob.mobends.standard.data;

import goblinbob.mobends.standard.animation.controller.ZombieController;
import net.minecraft.world.entity.Mob;

public class ZombieLikeData extends ZombieDataBase<Mob>
{
    private final ZombieController controller = new ZombieController();

    public ZombieLikeData(Mob entity)
    {
        super(entity);
    }

    @Override
    public ZombieController getController()
    {
        return this.controller;
    }
}
