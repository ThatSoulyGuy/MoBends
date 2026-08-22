package goblinbob.mobends.standard.data;

import goblinbob.mobends.standard.animation.controller.PiglinController;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;

public class PiglinData<E extends AbstractPiglin> extends BipedEntityData<E>
{
    private final PiglinController controller = new PiglinController();

    public PiglinData(E entity)
    {
        super(entity);
    }

    @Override
    public PiglinController getController()
    {
        return controller;
    }

    @Override
    public void onTicksRestart()
    {
    }
}
