package goblinbob.mobends.standard.data;

import goblinbob.mobends.standard.animation.controller.IllagerController;
import net.minecraft.world.entity.monster.AbstractIllager;

public class IllagerData<E extends AbstractIllager> extends BipedEntityData<E>
{
    private final IllagerController controller = new IllagerController();

    public IllagerData(E entity)
    {
        super(entity);
    }

    @Override
    public IllagerController getController()
    {
        return controller;
    }

    @Override
    public void onTicksRestart()
    {
    }
}
