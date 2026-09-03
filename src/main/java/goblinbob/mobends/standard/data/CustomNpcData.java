package goblinbob.mobends.standard.data;

import goblinbob.mobends.standard.animation.controller.CustomNpcController;
import net.minecraft.world.entity.LivingEntity;

public class CustomNpcData<E extends LivingEntity> extends BipedEntityData<E>
{
    private final CustomNpcController controller = new CustomNpcController();

    public CustomNpcData(E entity)
    {
        super(entity);
    }

    @Override
    public CustomNpcController getController()
    {
        return controller;
    }

    @Override
    public void onTicksRestart()
    {
    }
}
